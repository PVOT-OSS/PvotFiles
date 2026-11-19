// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.clean

import android.graphics.Bitmap
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
import org.prauga.files.core.data.AppsRepository
import org.prauga.files.core.model.UnusedApp

@HiltViewModel
class UnusedAppsViewModel @Inject constructor(private val repository: AppsRepository) :
    ViewModel() {
    private val mutableState = MutableStateFlow<UiState<List<UnusedApp>>?>(UiState.Loading)

    val state: StateFlow<UiState<List<UnusedApp>>?> = mutableState.asStateFlow()
    private var loading: Job? = null

    fun refresh() {
        loading?.cancel()
        if (!repository.hasUsageAccess()) {
            mutableState.value = null
            return
        }
        if (mutableState.value !is UiState.Success) mutableState.value = UiState.Loading
        loading =
            viewModelScope.launch {
                mutableState.value =
                    repository.unusedApps()?.let { UiState.Success(it) }
                        ?: UiState.Error(message = "")
            }
    }

    suspend fun icon(packageName: String, sizePx: Int): Bitmap? =
        repository.icon(packageName, sizePx)
}
