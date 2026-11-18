// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.locations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.files.core.data.DocumentTreeRepository
import org.prauga.files.core.model.DocumentEntry
import org.prauga.files.core.model.SavedLocation

@HiltViewModel(assistedFactory = TreeViewModel.Factory::class)
class TreeViewModel
@AssistedInject
constructor(
    @Assisted private val location: SavedLocation,
    private val savedStateHandle: SavedStateHandle,
    private val repository: DocumentTreeRepository,
) : ViewModel() {
    private val ids = savedStateHandle.getStateFlow<List<String>>(KEY_IDS, emptyList())
    private val names = savedStateHandle.getStateFlow<List<String>>(KEY_NAMES, emptyList())
    private val attempt = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TreeUiState> =
        combine(ids, names, attempt) { stack, titles, _ -> stack to titles }
            .flatMapLatest { (stack, titles) ->
                val title = titles.lastOrNull() ?: location.label
                flow {
                    emit(TreeUiState(title, stack.isNotEmpty(), UiState.Loading))
                    val children = repository.children(location.uri, stack.lastOrNull())
                    emit(
                        TreeUiState(
                            title,
                            stack.isNotEmpty(),
                            children?.let { UiState.Success(it) } ?: UiState.Error(message = ""),
                        )
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                TreeUiState(location.label, false, UiState.Loading),
            )

    fun onEvent(event: TreeEvent) {
        when (event) {
            is TreeEvent.Open -> {
                savedStateHandle[KEY_IDS] = ids.value + event.folder.documentId
                savedStateHandle[KEY_NAMES] = names.value + event.folder.name
            }
            TreeEvent.Up -> {
                savedStateHandle[KEY_IDS] = ids.value.dropLast(1)
                savedStateHandle[KEY_NAMES] = names.value.dropLast(1)
            }
            TreeEvent.Retry -> attempt.value++
            TreeEvent.RemoveLocation -> viewModelScope.launch { repository.remove(location.uri) }
        }
    }

    fun documentUri(entry: DocumentEntry): String =
        repository.documentUri(location.uri, entry.documentId)

    @AssistedFactory
    interface Factory {
        fun create(location: SavedLocation): TreeViewModel
    }

    private companion object {
        const val KEY_IDS = "tree_ids"
        const val KEY_NAMES = "tree_names"
    }
}
