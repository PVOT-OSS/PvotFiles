// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.access

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class AccessViewModel @Inject constructor(private val checker: StorageAccessChecker) : ViewModel() {
    private val mutableState = MutableStateFlow(currentState())
    val state: StateFlow<AccessUiState> = mutableState.asStateFlow()

    fun onEvent(event: AccessEvent) {
        when (event) {
            AccessEvent.Refresh -> mutableState.value = currentState()
        }
    }

    private fun currentState(): AccessUiState =
        if (checker.isGranted()) AccessUiState.Granted else AccessUiState.Required
}
