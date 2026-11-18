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
import org.prauga.files.core.data.SearchRepository
import org.prauga.files.core.model.FileEntry

@HiltViewModel
class SearchViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: SearchRepository,
) : ViewModel() {
    private val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")
    private val attempt = MutableStateFlow(0)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val results =
        combine(
                query.map { it.trim() }.distinctUntilChanged().debounce(DEBOUNCE_MILLIS),
                attempt,
            ) { trimmed, _ ->
                trimmed
            }
            .flatMapLatest { trimmed ->
                if (trimmed.isEmpty()) {
                    flowOf(null)
                } else {
                    flow<UiState<List<FileEntry.File>>?> {
                        emit(UiState.Loading)
                        emit(
                            repository.search(trimmed)?.let { UiState.Success(it) }
                                ?: UiState.Error(message = "")
                        )
                    }
                }
            }

    val state: StateFlow<SearchUiState> =
        combine(query, results) { text, found ->
                SearchUiState(text, found.takeIf { text.isNotBlank() })
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
        }
    }

    private companion object {
        const val KEY_QUERY = "query"
        const val DEBOUNCE_MILLIS = 300L
    }
}
