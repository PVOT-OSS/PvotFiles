// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.files.core.data.ArchiveRepository
import org.prauga.files.core.data.DirectoryRepository
import org.prauga.files.core.data.DisplayOptionsRepository
import org.prauga.files.core.data.FavouritesRepository
import org.prauga.files.core.data.FileOperationsRepository
import org.prauga.files.core.data.StorageRepository
import org.prauga.files.core.data.TrashRepository
import org.prauga.files.core.model.DirectoryListing
import org.prauga.files.core.model.DisplayOptions
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.TransferKind
import org.prauga.files.core.model.Volume
import org.prauga.files.core.model.sortedFor
import org.prauga.files.core.model.volumeOf
import org.prauga.files.transfer.TransferQueue

@HiltViewModel
class BrowseViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val storage: StorageRepository,
    private val directories: DirectoryRepository,
    private val displayOptions: DisplayOptionsRepository,
    private val operations: FileOperationsRepository,
    private val trash: TrashRepository,
    private val transfers: TransferQueue,
    private val favourites: FavouritesRepository,
    private val archives: ArchiveRepository,
) : ViewModel() {
    private val effectChannel = Channel<BrowseEffect>(Channel.BUFFERED)
    val effects: Flow<BrowseEffect> = effectChannel.receiveAsFlow()
    private var volumes: List<Volume> = storage.volumes()
    private val path: StateFlow<String?> =
        savedStateHandle.getStateFlow(KEY_PATH, storage.primaryRootPath())
    private val attempt = MutableStateFlow(0)
    private val clipboardPaths =
        savedStateHandle.getStateFlow<List<String>>(KEY_CLIP_PATHS, emptyList())
    private val clipboardKind = savedStateHandle.getStateFlow<String?>(KEY_CLIP_KIND, null)
    private val selection = savedStateHandle.getStateFlow<List<String>>(KEY_SELECTION, emptyList())
    private val clipboard: StateFlow<Clipboard?> =
        combine(clipboardPaths, clipboardKind) { paths, kind ->
                TransferKind.entries
                    .firstOrNull { it.name == kind }
                    ?.takeIf { paths.isNotEmpty() }
                    ?.let { Clipboard(paths, it) }
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val transferring = MutableStateFlow(false)
    private val reportedTransfers = mutableSetOf<String>()
    private val options: StateFlow<DisplayOptions> =
        displayOptions.options.stateIn(viewModelScope, SharingStarted.Eagerly, DisplayOptions())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<BrowseUiState> =
        combine(path, attempt, options.map { it.showHidden }.distinctUntilChanged()) {
                current,
                _,
                hidden ->
                current to hidden
            }
            .flatMapLatest { (current, hidden) ->
                flow {
                    emit(current to UiState.Loading)
                    emit(current to load(current, hidden))
                }
            }
            .combine(options) { (current, content), options -> stateFor(current, options, content) }
            .combine(clipboard) { state, clipboard -> state.copy(clipboard = clipboard) }
            .combine(transferring) { state, running -> state.copy(transferring = running) }
            .combine(favourites.favourites) { state, saved ->
                state.copy(favourite = state.path in saved)
            }
            .combine(selection) { state, selected -> state.copy(selection = selected.toSet()) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                stateFor(path.value, options.value, UiState.Loading),
            )

    init {
        viewModelScope.launch {
            var seeded = false
            transfers.updates.collect { updates ->
                transferring.value = updates.any { it.running }
                val finished = updates.mapNotNull { update -> update.outcome?.let { update to it } }
                if (!seeded) {
                    seeded = true
                    reportedTransfers += finished.map { it.first.id }
                } else {
                    finished
                        .filter { (update, _) -> reportedTransfers.add(update.id) }
                        .forEach { (update, outcome) ->
                            attempt.value++
                            effectChannel.send(BrowseEffect.TransferFinished(update.kind, outcome))
                        }
                }
            }
        }
    }

    fun onEvent(event: BrowseEvent) {
        when (event) {
            is BrowseEvent.Open -> {
                select(emptyList())
                savedStateHandle[KEY_PATH] = event.directory.path
            }
            BrowseEvent.Up -> {
                val current = path.value
                if (current != null && current != volumes.volumeOf(current)?.path) {
                    select(emptyList())
                    savedStateHandle[KEY_PATH] = File(current).parent
                }
            }
            BrowseEvent.Retry -> {
                volumes = storage.volumes()
                attempt.value++
            }
            is BrowseEvent.SetSortOrder -> updateOptions { it.copy(sortOrder = event.order) }
            is BrowseEvent.SetLayout -> updateOptions { it.copy(layout = event.layout) }
            is BrowseEvent.SetShowHidden -> updateOptions { it.copy(showHidden = event.show) }
            is BrowseEvent.CreateFolder -> {
                val parent = path.value ?: return
                perform { operations.createFolder(parent, event.name) }
            }
            is BrowseEvent.Rename -> perform { operations.rename(event.path, event.newName) }
            is BrowseEvent.Delete -> delete(listOf(event.path))
            is BrowseEvent.Restore -> perform { restoreAll(event.trashIds) }
            is BrowseEvent.ToggleSelection ->
                select(
                    if (event.path in selection.value) selection.value - event.path
                    else selection.value + event.path
                )
            BrowseEvent.SelectAll -> select(currentEntries().map { it.path })
            BrowseEvent.ClearSelection -> select(emptyList())
            BrowseEvent.CopySelection -> {
                setClipboard(selection.value, TransferKind.Copy)
                select(emptyList())
            }
            BrowseEvent.MoveSelection -> {
                setClipboard(selection.value, TransferKind.Move)
                select(emptyList())
            }
            is BrowseEvent.Compress -> compress(event.paths)
            BrowseEvent.CompressSelection -> {
                val targets = selection.value
                select(emptyList())
                compress(targets)
            }
            is BrowseEvent.Extract -> perform { archives.extract(event.path) }
            BrowseEvent.DeleteSelection -> {
                val targets = selection.value
                select(emptyList())
                delete(targets)
            }
            is BrowseEvent.Copy -> setClipboard(listOf(event.path), TransferKind.Copy)
            is BrowseEvent.Move -> setClipboard(listOf(event.path), TransferKind.Move)
            BrowseEvent.Paste -> paste()
            BrowseEvent.ClearClipboard -> setClipboard(emptyList(), null)
            is BrowseEvent.GoTo -> {
                volumes = storage.volumes()
                if (volumes.volumeOf(event.path) != null) {
                    select(emptyList())
                    savedStateHandle[KEY_PATH] = event.path
                }
            }
            BrowseEvent.ToggleFavourite -> {
                val current = path.value ?: return
                val favourite = !state.value.favourite
                viewModelScope.launch { favourites.setFavourite(current, favourite) }
            }
        }
    }

    private fun folderName(current: String?): String? {
        if (current == null) return null
        val volume = volumes.volumeOf(current)
        return when {
            volume == null -> File(current).name
            current != volume.path -> File(current).name
            volume.primary -> null
            else -> volume.label
        }
    }

    private fun setClipboard(paths: List<String>, kind: TransferKind?) {
        savedStateHandle[KEY_CLIP_PATHS] = paths
        savedStateHandle[KEY_CLIP_KIND] = kind?.name
    }

    private fun paste() {
        val destination = path.value ?: return
        val pending = clipboard.value ?: return
        transfers.start(pending.paths, destination, pending.kind)
        setClipboard(emptyList(), null)
    }

    private fun delete(targets: List<String>) {
        if (targets.isEmpty()) return
        viewModelScope.launch {
            val trashed =
                targets.mapNotNull { target ->
                    trash.moveToTrash(target)?.let { it to File(target).name }
                }
            if (trashed.isNotEmpty()) {
                attempt.value++
                effectChannel.send(
                    BrowseEffect.Trashed(trashed.map { it.first }, trashed.map { it.second })
                )
            }
            if (trashed.size < targets.size) {
                effectChannel.send(BrowseEffect.OperationFailed(FileOperationResult.Failed))
            }
        }
    }

    private suspend fun restoreAll(ids: List<String>): FileOperationResult =
        ids.map { trash.restore(it) }.firstOrNull { it != FileOperationResult.Done }
            ?: FileOperationResult.Done

    private fun compress(targets: List<String>) {
        val folder = path.value ?: return
        if (targets.isNotEmpty()) perform { archives.compress(targets, folder) }
    }

    private fun select(paths: List<String>) {
        savedStateHandle[KEY_SELECTION] = paths
    }

    private fun currentEntries(): List<FileEntry> =
        (state.value.content as? UiState.Success)?.data.orEmpty()

    private fun perform(operation: suspend () -> FileOperationResult) {
        viewModelScope.launch {
            when (val result = operation()) {
                FileOperationResult.Done -> attempt.value++
                else -> effectChannel.send(BrowseEffect.OperationFailed(result))
            }
        }
    }

    private fun updateOptions(transform: (DisplayOptions) -> DisplayOptions) {
        viewModelScope.launch { displayOptions.update(transform) }
    }

    private suspend fun load(current: String?, includeHidden: Boolean): UiState<List<FileEntry>> =
        if (current == null) {
            UiState.Error(message = "")
        } else {
            when (val listing = directories.list(current, includeHidden)) {
                is DirectoryListing.Entries ->
                    UiState.Success(listing.entries.filterNot { trash.isTrash(it.path) })
                DirectoryListing.NotFound,
                DirectoryListing.Unreadable -> UiState.Error(message = "")
            }
        }

    private fun stateFor(
        current: String?,
        options: DisplayOptions,
        content: UiState<List<FileEntry>>,
    ) =
        BrowseUiState(
            path = current,
            folderName = folderName(current),
            canGoUp = current != null && current != volumes.volumeOf(current)?.path,
            options = options,
            content =
                if (content is UiState.Success)
                    UiState.Success(content.data.sortedFor(options.sortOrder))
                else content,
        )

    private companion object {
        const val KEY_PATH = "path"
        const val KEY_CLIP_PATHS = "clipboard_paths"
        const val KEY_CLIP_KIND = "clipboard_kind"
        const val KEY_SELECTION = "selection"
    }
}
