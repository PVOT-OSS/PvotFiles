// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.safe

import com.prauga.pvot.coreui.UiState
import org.prauga.files.core.model.SafeItem

sealed interface SafeUiState {
    data object Loading : SafeUiState

    data class Setup(val mismatch: Boolean = false, val tooShort: Boolean = false) : SafeUiState

    data class Locked(val attemptsLeft: Int? = null, val lockedUntilMillis: Long? = null) :
        SafeUiState

    data class Unlocked(val items: UiState<List<SafeItem>>) : SafeUiState
}
