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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.files.core.data.DirectoryRepository
import org.prauga.files.core.data.DisplayOptionsRepository
import org.prauga.files.core.data.StorageRepository
import org.prauga.files.core.model.DirectoryListing
import org.prauga.files.core.model.DisplayOptions
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.sortedFor

@HiltViewModel
class BrowseViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    storage: StorageRepository,
    private val directories: DirectoryRepository,
    private val displayOptions: DisplayOptionsRepository,
) : ViewModel() {
    private val root: String? = storage.primaryRootPath()
    private val path: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_PATH, root)
    private val attempt = MutableStateFlow(0)
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
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                stateFor(path.value, options.value, UiState.Loading),
            )

    fun onEvent(event: BrowseEvent) {
        when (event) {
            is BrowseEvent.Open -> savedStateHandle[KEY_PATH] = event.directory.path
            BrowseEvent.Up -> {
                val current = path.value
                if (current != null && current != root) {
                    savedStateHandle[KEY_PATH] = File(current).parent
                }
            }
            BrowseEvent.Retry -> attempt.value++
            is BrowseEvent.SetSortOrder -> updateOptions { it.copy(sortOrder = event.order) }
            is BrowseEvent.SetLayout -> updateOptions { it.copy(layout = event.layout) }
            is BrowseEvent.SetShowHidden -> updateOptions { it.copy(showHidden = event.show) }
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
                is DirectoryListing.Entries -> UiState.Success(listing.entries)
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
            folderName = if (current == null || current == root) null else File(current).name,
            canGoUp = current != null && current != root,
            options = options,
            content =
                if (content is UiState.Success)
                    UiState.Success(content.data.sortedFor(options.sortOrder))
                else content,
        )

    private companion object {
        const val KEY_PATH = "path"
    }
}
