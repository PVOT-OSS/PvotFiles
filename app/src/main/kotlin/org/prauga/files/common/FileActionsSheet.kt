// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotBottomSheet
import com.prauga.pvot.designsystem.components.PvotSheetItem
import org.prauga.files.R
import org.prauga.files.core.model.FileEntry

@Composable
fun FileActionsSheet(file: FileEntry.File?, launcher: FileLauncher, onDismiss: () -> Unit) {
    var shown by remember { mutableStateOf(file) }
    if (file != null) shown = file
    val target = shown
    PvotBottomSheet(visible = file != null, onDismissRequest = onDismiss, title = target?.name) {
        if (target != null) {
            PvotSheetItem(
                title = stringResource(R.string.action_open_with),
                onClick = {
                    launcher.openWith(target)
                    onDismiss()
                },
                icon = { Icon(painterResource(R.drawable.ic_open_with), contentDescription = null) },
            )
            PvotSheetItem(
                title = stringResource(R.string.action_share),
                onClick = {
                    launcher.share(target)
                    onDismiss()
                },
                icon = { Icon(painterResource(R.drawable.ic_share), contentDescription = null) },
            )
        }
    }
}
