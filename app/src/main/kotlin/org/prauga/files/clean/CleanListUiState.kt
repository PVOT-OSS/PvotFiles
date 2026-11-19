// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.clean

import com.prauga.pvot.coreui.UiState
import org.prauga.files.core.model.CleanCategory
import org.prauga.files.core.model.FileEntry

data class CleanListUiState(
    val category: CleanCategory,
    val groups: UiState<List<List<FileEntry>>>,
    val selection: Set<String> = emptySet(),
) {
    val selectedBytes: Long
        get() =
            (groups as? UiState.Success)
                ?.data
                ?.flatten()
                ?.filter { it.path in selection }
                ?.sumOf { (it as? FileEntry.File)?.sizeBytes ?: 0L } ?: 0L
}
