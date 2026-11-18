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
    private val paths: StateFlow<List<String>> =
        savedStateHandle.getStateFlow(KEY_PATHS, emptyList())
    private val failureChannel = Channel<FileOperationResult>(Channel.BUFFERED)
    val failures: Flow<FileOperationResult> = failureChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<TagPickerUiState> =
        combine(
                paths,
                repository.tags,
                paths.flatMapLatest { current ->
                    if (current.isEmpty()) {
                        flowOf(emptySet())
                    } else {
                        combine(current.map(repository::tagsOf)) { sets ->
                            sets.reduce { all, next -> all intersect next }
                        }
                    }
                },
            ) { current, tags, selected ->
                TagPickerUiState(current, tags, selected)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                TagPickerUiState(emptyList(), emptyList(), emptySet()),
            )

    fun onEvent(event: TagPickerEvent) {
        when (event) {
            is TagPickerEvent.Show -> savedStateHandle[KEY_PATHS] = event.paths
            is TagPickerEvent.Toggle -> {
                val current = paths.value
                val tagged = event.tagName !in state.value.selected
                viewModelScope.launch {
                    current.forEach { repository.setTagged(it, event.tagName, tagged) }
                }
            }
            is TagPickerEvent.Create ->
                viewModelScope.launch {
                    when (val result = repository.createTag(event.tag)) {
                        FileOperationResult.Done ->
                            paths.value.forEach { repository.setTagged(it, event.tag.name, true) }
                        else -> failureChannel.send(result)
                    }
                }
        }
    }

    private companion object {
        const val KEY_PATHS = "tag_paths"
    }
}
