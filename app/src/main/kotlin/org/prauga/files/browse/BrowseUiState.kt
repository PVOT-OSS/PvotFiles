// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import com.prauga.pvot.coreui.UiState
import org.prauga.files.core.model.DisplayOptions
import org.prauga.files.core.model.FileEntry

data class BrowseUiState(
    val path: String?,
    val folderName: String?,
    val canGoUp: Boolean,
    val options: DisplayOptions,
    val clipboard: Clipboard? = null,
    val transferring: Boolean = false,
    val favourite: Boolean = false,
    val content: UiState<List<FileEntry>>,
)
