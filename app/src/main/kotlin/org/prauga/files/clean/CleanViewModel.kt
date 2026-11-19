// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.clean

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.prauga.files.core.data.CleanRepository
import org.prauga.files.core.model.CleanSummary

@HiltViewModel
class CleanViewModel @Inject constructor(private val repository: CleanRepository) : ViewModel() {
    private val mutableState = MutableStateFlow<UiState<List<CleanSummary>>>(UiState.Loading)
    val state: StateFlow<UiState<List<CleanSummary>>> = mutableState.asStateFlow()
    private var loading: Job? = null

    fun onEvent(event: CleanEvent) {
        when (event) {
            CleanEvent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        loading?.cancel()
        if (mutableState.value !is UiState.Success) mutableState.value = UiState.Loading
        loading =
            viewModelScope.launch {
                mutableState.value =
                    repository.summary()?.let { UiState.Success(it) } ?: UiState.Error(message = "")
            }
    }
}
