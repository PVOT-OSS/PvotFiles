// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.safe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.prauga.files.core.data.SafeFolderRepository
import org.prauga.files.core.data.StorageRepository
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.UnlockResult

@HiltViewModel
class SafeFolderViewModel
@Inject
constructor(
    private val repository: SafeFolderRepository,
    private val storage: StorageRepository,
    private val clock: Clock,
) : ViewModel() {
    private val mutableState = MutableStateFlow<SafeUiState>(SafeUiState.Loading)
    val state: StateFlow<SafeUiState> = mutableState.asStateFlow()
    private val effectChannel = Channel<SafeEffect>(Channel.BUFFERED)
    val effects: Flow<SafeEffect> = effectChannel.receiveAsFlow()
    private var stoppedAtMillis: Long? = null

    init {
        viewModelScope.launch {
            repository.clearOpened()
            mutableState.value =
                if (repository.hasPin.first()) SafeUiState.Locked() else SafeUiState.Setup()
        }
    }

    fun onEvent(event: SafeEvent) {
        when (event) {
            is SafeEvent.CreatePin -> createPin(event.pin, event.confirmation)
            is SafeEvent.Unlock -> unlock(event.pin)
            SafeEvent.Lock -> lock()
            is SafeEvent.Open ->
                viewModelScope.launch {
                    val path = repository.open(event.id)
                    effectChannel.send(
                        if (path != null) SafeEffect.OpenFile(path) else SafeEffect.Failed
                    )
                }
            is SafeEvent.MoveOut -> moveOut(event.id)
            is SafeEvent.Delete -> change { repository.delete(event.id) }
            SafeEvent.Stopped ->
                if (mutableState.value is SafeUiState.Unlocked) stoppedAtMillis = clock.millis()
            SafeEvent.Started -> {
                val stopped = stoppedAtMillis
                stoppedAtMillis = null
                when {
                    stopped != null && clock.millis() - stopped > GRACE_MILLIS -> lock()
                    mutableState.value is SafeUiState.Unlocked ->
                        viewModelScope.launch { showItems() }
                }
            }
        }
    }

    private fun createPin(pin: String, confirmation: String) {
        when {
            pin != confirmation -> mutableState.value = SafeUiState.Setup(mismatch = true)
            pin.length < MIN_PIN_LENGTH || !pin.all { it.isDigit() } ->
                mutableState.value = SafeUiState.Setup(tooShort = true)
            else ->
                viewModelScope.launch {
                    if (repository.setPin(pin)) showItems()
                    else mutableState.value = SafeUiState.Setup(tooShort = true)
                }
        }
    }

    private fun unlock(pin: String) {
        viewModelScope.launch {
            mutableState.value =
                when (val result = repository.unlock(pin)) {
                    UnlockResult.Unlocked -> {
                        showItems()
                        return@launch
                    }
                    is UnlockResult.Wrong -> SafeUiState.Locked(attemptsLeft = result.attemptsLeft)
                    is UnlockResult.LockedOut ->
                        SafeUiState.Locked(lockedUntilMillis = result.untilMillis)
                }
        }
    }

    private fun lock() {
        if (mutableState.value !is SafeUiState.Unlocked) return
        mutableState.value = SafeUiState.Locked()
        viewModelScope.launch { repository.clearOpened() }
    }

    private fun moveOut(id: String) {
        val name =
            ((mutableState.value as? SafeUiState.Unlocked)?.items as? UiState.Success)
                ?.data
                ?.firstOrNull { it.id == id }
                ?.name ?: return
        val downloads = storage.primaryRootPath()?.let { "$it/$DOWNLOADS_FOLDER" } ?: return
        change(onDone = { effectChannel.send(SafeEffect.MovedOut(name)) }) {
            repository.moveOut(id, downloads)
        }
    }

    private fun change(
        onDone: suspend () -> Unit = {},
        operation: suspend () -> FileOperationResult,
    ) {
        viewModelScope.launch {
            if (operation() == FileOperationResult.Done) onDone()
            else effectChannel.send(SafeEffect.Failed)
            showItems()
        }
    }

    private suspend fun showItems() {
        mutableState.value =
            SafeUiState.Unlocked(
                repository.items()?.let { UiState.Success(it) } ?: UiState.Error(message = "")
            )
    }

    private companion object {
        const val GRACE_MILLIS = 60_000L
        const val MIN_PIN_LENGTH = 4
        const val DOWNLOADS_FOLDER = "Download"
    }
}
