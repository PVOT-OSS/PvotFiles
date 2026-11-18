// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.prauga.files.core.data.TagsRepository
import org.prauga.files.core.model.FileEntry

@HiltViewModel(assistedFactory = TaggedViewModel.Factory::class)
class TaggedViewModel
@AssistedInject
constructor(@Assisted private val tagName: String, private val repository: TagsRepository) :
    ViewModel() {
    private val mutableState = MutableStateFlow<UiState<List<FileEntry>>>(UiState.Loading)
    val state: StateFlow<UiState<List<FileEntry>>> = mutableState.asStateFlow()

    fun onEvent(event: TaggedEvent) {
        when (event) {
            TaggedEvent.Refresh ->
                viewModelScope.launch {
                    mutableState.value = UiState.Success(repository.entriesTagged(tagName))
                }
            TaggedEvent.DeleteTag -> viewModelScope.launch { repository.deleteTag(tagName) }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(tagName: String): TaggedViewModel
    }
}
