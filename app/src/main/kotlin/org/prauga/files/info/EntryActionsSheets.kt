// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.info

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import org.prauga.files.common.FileActionsSheet
import org.prauga.files.common.FileLauncher
import org.prauga.files.core.model.FileEntry

@Composable
fun EntryActionsSheets(entry: FileEntry?, launcher: FileLauncher, onDismiss: () -> Unit) {
    var infoPath by rememberSaveable { mutableStateOf<String?>(null) }
    FileActionsSheet(entry, launcher, onDismiss = onDismiss, onInfo = { infoPath = it.path })
    InfoSheet(path = infoPath, onDismiss = { infoPath = null })
}
