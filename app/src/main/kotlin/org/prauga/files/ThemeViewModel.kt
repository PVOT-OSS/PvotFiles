// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.prauga.files.core.data.DisplayOptionsRepository
import org.prauga.files.core.model.DisplayOptions

@HiltViewModel
class ThemeViewModel @Inject constructor(repository: DisplayOptionsRepository) : ViewModel() {
    val options: StateFlow<DisplayOptions> =
        repository.options.stateIn(viewModelScope, SharingStarted.Eagerly, DisplayOptions())
}
