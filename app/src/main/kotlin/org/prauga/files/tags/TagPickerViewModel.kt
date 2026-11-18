// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.tags

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.files.core.data.TagsRepository
import org.prauga.files.core.model.FileOperationResult

@HiltViewModel
class TagPickerViewModel
@Inject
constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: TagsRepository,
) : ViewModel() {
    private val path: StateFlow<String?> = savedStateHandle.getStateFlow(KEY_PATH, null)
    private val failureChannel = Channel<FileOperationResult>(Channel.BUFFERED)
    val failures: Flow<FileOperationResult> = failureChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TagPickerUiState> =
        combine(
                path,
                repository.tags,
                path.flatMapLatest { current ->
                    current?.let(repository::tagsOf) ?: flowOf(emptySet())
                },
            ) { current, tags, selected ->
                TagPickerUiState(current, tags, selected)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                TagPickerUiState(null, emptyList(), emptySet()),
            )

    fun onEvent(event: TagPickerEvent) {
        when (event) {
            is TagPickerEvent.Show -> savedStateHandle[KEY_PATH] = event.path
            is TagPickerEvent.Toggle -> {
                val current = path.value ?: return
                val tagged = event.tagName !in state.value.selected
                viewModelScope.launch { repository.setTagged(current, event.tagName, tagged) }
            }
            is TagPickerEvent.Create ->
                viewModelScope.launch {
                    when (val result = repository.createTag(event.tag)) {
                        FileOperationResult.Done ->
                            path.value?.let { repository.setTagged(it, event.tag.name, true) }
                        else -> failureChannel.send(result)
                    }
                }
        }
    }

    private companion object {
        const val KEY_PATH = "tag_path"
    }
}
