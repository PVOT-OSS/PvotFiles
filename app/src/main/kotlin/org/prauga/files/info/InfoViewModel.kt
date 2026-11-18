// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.info

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
import org.prauga.files.core.data.FileInfoRepository
import org.prauga.files.core.model.FileInfo

@HiltViewModel
class InfoViewModel @Inject constructor(private val repository: FileInfoRepository) : ViewModel() {
    private val mutableState = MutableStateFlow<UiState<FileInfo>>(UiState.Loading)
    val state: StateFlow<UiState<FileInfo>> = mutableState.asStateFlow()
    private var loading: Job? = null

    fun load(path: String) {
        loading?.cancel()
        mutableState.value = UiState.Loading
        loading =
            viewModelScope.launch {
                mutableState.value =
                    repository.info(path)?.let { UiState.Success(it) }
                        ?: UiState.Error(message = "")
            }
    }
}
