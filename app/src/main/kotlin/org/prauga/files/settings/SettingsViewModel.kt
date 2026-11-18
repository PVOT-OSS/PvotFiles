// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.files.core.data.DisplayOptionsRepository
import org.prauga.files.core.model.DisplayOptions

@HiltViewModel
class SettingsViewModel @Inject constructor(private val options: DisplayOptionsRepository) :
    ViewModel() {
    val state: StateFlow<DisplayOptions> =
        options.options.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DisplayOptions(),
        )

    fun setShowHidden(show: Boolean) {
        viewModelScope.launch { options.update { it.copy(showHidden = show) } }
    }
}
