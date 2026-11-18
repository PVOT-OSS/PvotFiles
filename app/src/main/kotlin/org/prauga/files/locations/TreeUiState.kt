// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.locations

import com.prauga.pvot.coreui.UiState
import org.prauga.files.core.model.DocumentEntry

data class TreeUiState(
    val title: String,
    val canGoUp: Boolean,
    val content: UiState<List<DocumentEntry>>,
)
