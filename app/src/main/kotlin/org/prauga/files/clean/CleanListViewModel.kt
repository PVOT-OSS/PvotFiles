// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.clean

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.files.core.data.CleanRepository
import org.prauga.files.core.data.TrashRepository
import org.prauga.files.core.model.CleanCategory
import org.prauga.files.core.model.FileEntry

@HiltViewModel(assistedFactory = CleanListViewModel.Factory::class)
class CleanListViewModel
@AssistedInject
constructor(
    @Assisted private val category: CleanCategory,
    private val savedStateHandle: SavedStateHandle,
    private val repository: CleanRepository,
    private val trash: TrashRepository,
) : ViewModel() {
    private val groups = MutableStateFlow<UiState<List<List<FileEntry>>>>(UiState.Loading)
    private val selection = savedStateHandle.getStateFlow(KEY_SELECTION, emptyList<String>())
    private val effectChannel = Channel<CleanListEffect>(Channel.BUFFERED)
    val effects: Flow<CleanListEffect> = effectChannel.receiveAsFlow()
    private var loading: Job? = null

    val state: StateFlow<CleanListUiState> =
        combine(groups, selection) { loaded, selected ->
                CleanListUiState(category, loaded, selected.toSet())
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                CleanListUiState(category, UiState.Loading),
            )

    init {
        refresh()
    }

    fun onEvent(event: CleanListEvent) {
        when (event) {
            CleanListEvent.Refresh -> refresh()
            is CleanListEvent.Toggle ->
                select(
                    if (event.path in selection.value) selection.value - event.path
                    else selection.value + event.path
                )
            CleanListEvent.SelectAll -> select(removable().map { it.path })
            CleanListEvent.ClearSelection -> select(emptyList())
            CleanListEvent.DeleteSelection -> delete()
            is CleanListEvent.Restore ->
                viewModelScope.launch {
                    event.trashIds.forEach { trash.restore(it) }
                    refresh()
                }
        }
    }

    private fun removable(): List<FileEntry> {
        val loaded = (groups.value as? UiState.Success)?.data.orEmpty()
        return if (category == CleanCategory.Duplicates) loaded.flatMap { it.drop(1) }
        else loaded.flatten()
    }

    private fun delete() {
        val targets = selection.value
        if (targets.isEmpty()) return
        select(emptyList())
        viewModelScope.launch {
            val ids = targets.mapNotNull { trash.moveToTrash(it) }
            if (ids.isNotEmpty()) effectChannel.send(CleanListEffect.Trashed(ids, ids.size))
            if (ids.size < targets.size) effectChannel.send(CleanListEffect.Failed)
            refresh()
        }
    }

    private fun refresh() {
        loading?.cancel()
        loading =
            viewModelScope.launch {
                groups.value =
                    repository.groups(category)?.let { UiState.Success(it) }
                        ?: UiState.Error(message = "")
                val present =
                    (groups.value as? UiState.Success)
                        ?.data
                        ?.flatten()
                        ?.map { it.path }
                        ?.toSet()
                        .orEmpty()
                select(selection.value.filter { it in present })
            }
    }

    private fun select(paths: List<String>) {
        savedStateHandle[KEY_SELECTION] = paths
    }

    @AssistedFactory
    interface Factory {
        fun create(category: CleanCategory): CleanListViewModel
    }

    private companion object {
        const val KEY_SELECTION = "clean_selection"
    }
}
