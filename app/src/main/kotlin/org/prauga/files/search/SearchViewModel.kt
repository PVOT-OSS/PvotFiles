// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.files.core.data.SearchHistoryRepository
import org.prauga.files.core.data.SearchRepository
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.ModifiedWithin
import org.prauga.files.core.model.SearchFilters
import org.prauga.files.core.model.SizeRange

@HiltViewModel
class SearchViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: SearchRepository,
    private val history: SearchHistoryRepository,
) : ViewModel() {
    private val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")
    private val category = savedStateHandle.getStateFlow<String?>(KEY_CATEGORY, null)
    private val modified = savedStateHandle.getStateFlow(KEY_MODIFIED, ModifiedWithin.Any.name)
    private val size = savedStateHandle.getStateFlow(KEY_SIZE, SizeRange.Any.name)
    private val folder = savedStateHandle.getStateFlow<String?>(KEY_FOLDER, null)
    private val attempt = MutableStateFlow(0)

    private val filters =
        combine(category, modified, size, folder) { category, modified, size, folder ->
            SearchFilters(
                category =
                    category?.let { name -> FileCategory.entries.firstOrNull { it.name == name } },
                modified =
                    ModifiedWithin.entries.firstOrNull { it.name == modified }
                        ?: ModifiedWithin.Any,
                size = SizeRange.entries.firstOrNull { it.name == size } ?: SizeRange.Any,
                folder = folder,
            )
        }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val results =
        combine(
                query.map { it.trim() }.distinctUntilChanged().debounce(DEBOUNCE_MILLIS),
                filters,
                attempt,
            ) { trimmed, current, _ ->
                trimmed to current
            }
            .flatMapLatest { (trimmed, current) ->
                if (trimmed.isEmpty() && current.isDefault) {
                    flowOf(null)
                } else {
                    flow<UiState<List<FileEntry.File>>?> {
                        emit(UiState.Loading)
                        emit(
                            repository.search(trimmed, current)?.let { UiState.Success(it) }
                                ?: UiState.Error(message = "")
                        )
                    }
                }
            }

    val state: StateFlow<SearchUiState> =
        combine(query, results, filters, history.queries) { text, found, current, past ->
                SearchUiState(
                    text,
                    found.takeIf { text.isNotBlank() || !current.isDefault },
                    current,
                    past,
                )
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                SearchUiState(query.value, null),
            )

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.QueryChanged -> savedStateHandle[KEY_QUERY] = event.query
            SearchEvent.Clear -> savedStateHandle[KEY_QUERY] = ""
            SearchEvent.Retry -> attempt.value++
            is SearchEvent.CategoryChanged -> savedStateHandle[KEY_CATEGORY] = event.category?.name
            is SearchEvent.ModifiedChanged -> savedStateHandle[KEY_MODIFIED] = event.modified.name
            is SearchEvent.SizeChanged -> savedStateHandle[KEY_SIZE] = event.size.name
            SearchEvent.ResultOpened -> viewModelScope.launch { history.add(query.value) }
            is SearchEvent.HistoryPicked -> {
                savedStateHandle[KEY_QUERY] = event.query
                viewModelScope.launch { history.add(event.query) }
            }
            SearchEvent.ClearHistory -> viewModelScope.launch { history.clear() }
            is SearchEvent.FolderChanged -> savedStateHandle[KEY_FOLDER] = event.folder
        }
    }

    private companion object {
        const val KEY_QUERY = "query"
        const val KEY_CATEGORY = "search_category"
        const val KEY_MODIFIED = "search_modified"
        const val KEY_SIZE = "search_size"
        const val KEY_FOLDER = "search_folder"
        const val DEBOUNCE_MILLIS = 300L
    }
}
