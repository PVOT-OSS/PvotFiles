// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.home

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
import org.prauga.files.core.data.StorageUsageRepository
import org.prauga.files.core.model.StorageUsage

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: StorageUsageRepository) :
    ViewModel() {
    private val mutableState = MutableStateFlow<UiState<StorageUsage>>(UiState.Loading)
    val state: StateFlow<UiState<StorageUsage>> = mutableState.asStateFlow()
    private var measuring: Job? = null

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        measuring?.cancel()
        if (mutableState.value !is UiState.Success) mutableState.value = UiState.Loading
        measuring =
            viewModelScope.launch {
                mutableState.value =
                    repository.measure()?.let { UiState.Success(it) } ?: UiState.Error(message = "")
            }
    }
}
