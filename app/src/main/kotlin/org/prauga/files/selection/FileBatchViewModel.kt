// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.prauga.files.core.data.SafeFolderRepository
import org.prauga.files.core.data.TrashRepository
import org.prauga.files.core.model.FileOperationResult

@HiltViewModel
class FileBatchViewModel
@Inject
constructor(private val trash: TrashRepository, private val safeFolder: SafeFolderRepository) :
    ViewModel() {
    private val effectChannel = Channel<BatchEffect>(Channel.BUFFERED)
    val effects: Flow<BatchEffect> = effectChannel.receiveAsFlow()

    fun delete(paths: List<String>) {
        if (paths.isEmpty()) return
        viewModelScope.launch {
            val ids = paths.mapNotNull { trash.moveToTrash(it) }
            if (ids.isNotEmpty()) effectChannel.send(BatchEffect.Trashed(ids))
            if (ids.size < paths.size) effectChannel.send(BatchEffect.Failed)
            effectChannel.send(BatchEffect.Changed)
        }
    }

    fun restore(ids: List<String>) {
        viewModelScope.launch {
            ids.forEach { trash.restore(it) }
            effectChannel.send(BatchEffect.Changed)
        }
    }

    fun moveToSafe(paths: List<String>) {
        if (paths.isEmpty()) return
        viewModelScope.launch {
            if (!safeFolder.hasPin.first()) {
                effectChannel.send(BatchEffect.SafeFolderNeedsSetup)
                return@launch
            }
            val result = safeFolder.add(paths)
            effectChannel.send(
                if (result == FileOperationResult.Done) BatchEffect.MovedToSafe
                else BatchEffect.Failed
            )
            effectChannel.send(BatchEffect.Changed)
        }
    }
}
