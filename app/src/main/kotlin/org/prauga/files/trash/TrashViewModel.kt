// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.files.core.data.TrashRepository
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.TrashedItem

@HiltViewModel
class TrashViewModel @Inject constructor(private val repository: TrashRepository) : ViewModel() {
    private val attempt = MutableStateFlow(0)
    private val effectChannel = Channel<TrashEffect>(Channel.BUFFERED)
    val effects: Flow<TrashEffect> = effectChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<UiState<List<TrashedItem>>> =
        attempt
            .flatMapLatest {
                flow {
                    emit(UiState.Loading)
                    emit(
                        repository.items()?.let { UiState.Success(it) }
                            ?: UiState.Error(message = "")
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun onEvent(event: TrashEvent) {
        when (event) {
            is TrashEvent.Restore -> {
                val name = itemName(event.id)
                perform({ repository.restore(event.id) }) { name?.let { TrashEffect.Restored(it) } }
            }
            is TrashEvent.DeleteForever -> perform({ repository.deleteForever(event.id) }) { null }
            TrashEvent.Retry -> attempt.value++
            TrashEvent.EmptyAll -> {
                val ids = (state.value as? UiState.Success)?.data.orEmpty().map { it.id }
                perform({
                    ids.map { repository.deleteForever(it) }
                        .firstOrNull { it != FileOperationResult.Done } ?: FileOperationResult.Done
                }) {
                    null
                }
            }
        }
    }

    private fun itemName(id: String): String? =
        (state.value as? UiState.Success)?.data?.firstOrNull { it.id == id }?.name

    private fun perform(operation: suspend () -> FileOperationResult, onDone: () -> TrashEffect?) {
        viewModelScope.launch {
            when (val result = operation()) {
                FileOperationResult.Done -> {
                    attempt.value++
                    onDone()?.let { effectChannel.send(it) }
                }
                else -> effectChannel.send(TrashEffect.OperationFailed(result))
            }
        }
    }
}
