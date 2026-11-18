// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.search

import com.prauga.pvot.coreui.UiState
import org.prauga.files.core.model.FileEntry

data class SearchUiState(val query: String, val results: UiState<List<FileEntry.File>>?)
