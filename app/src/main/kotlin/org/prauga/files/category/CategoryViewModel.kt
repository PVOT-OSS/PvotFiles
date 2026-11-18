// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.prauga.files.core.data.CategoryFilesRepository
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.FileEntry

@HiltViewModel(assistedFactory = CategoryViewModel.Factory::class)
class CategoryViewModel
@AssistedInject
constructor(
    @Assisted private val category: FileCategory,
    private val repository: CategoryFilesRepository,
) : ViewModel() {
    private val attempt = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<UiState<List<FileEntry.File>>> =
        attempt
            .flatMapLatest {
                flow {
                    emit(UiState.Loading)
                    emit(
                        repository.files(category)?.let { UiState.Success(it) }
                            ?: UiState.Error(message = "")
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    fun onEvent(event: CategoryEvent) {
        when (event) {
            CategoryEvent.Retry -> attempt.value++
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(category: FileCategory): CategoryViewModel
    }
}
