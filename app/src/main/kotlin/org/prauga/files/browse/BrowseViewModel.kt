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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.prauga.files.core.data.DirectoryRepository
import org.prauga.files.core.data.StorageRepository
import org.prauga.files.core.model.DirectoryListing
import org.prauga.files.core.model.FileEntry

@HiltViewModel
class BrowseViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    storage: StorageRepository,
    private val directories: DirectoryRepository,
) : ViewModel() {
    private val root: String? = storage.primaryRootPath()
    private val path: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_PATH, root)
    private val attempt = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<BrowseUiState> =
        combine(path, attempt) { current, _ -> current }
            .flatMapLatest { current ->
                flow {
                    emit(stateFor(current, UiState.Loading))
                    emit(stateFor(current, load(current)))
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                stateFor(path.value, UiState.Loading),
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
        }
    }

    private suspend fun load(current: String?) =
        if (current == null) {
            UiState.Error(message = "")
        } else {
            when (val listing = directories.list(current, includeHidden = false)) {
                is DirectoryListing.Entries -> UiState.Success(listing.entries)
                DirectoryListing.NotFound,
                DirectoryListing.Unreadable -> UiState.Error(message = "")
            }
        }

    private fun stateFor(current: String?, content: UiState<List<FileEntry>>) =
        BrowseUiState(
            folderName = if (current == null || current == root) null else File(current).name,
            canGoUp = current != null && current != root,
            content = content,
        )

    private companion object {
        const val KEY_PATH = "path"
    }
}
