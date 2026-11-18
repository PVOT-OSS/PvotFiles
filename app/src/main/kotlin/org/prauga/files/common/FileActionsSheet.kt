// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.annotation.DrawableRes
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
fun FileActionsSheet(
    entry: FileEntry?,
    launcher: FileLauncher,
    onDismiss: () -> Unit,
    onRename: ((FileEntry) -> Unit)? = null,
    onDelete: ((FileEntry) -> Unit)? = null,
) {
    var shown by remember { mutableStateOf(entry) }
    if (entry != null) shown = entry
    val target = shown
    PvotBottomSheet(visible = entry != null, onDismissRequest = onDismiss, title = target?.name) {
        if (target is FileEntry.File) {
            Action(R.string.action_open_with, R.drawable.ic_open_with) {
                launcher.openWith(target)
                onDismiss()
            }
            Action(R.string.action_share, R.drawable.ic_share) {
                launcher.share(target)
                onDismiss()
            }
        }
        if (target != null && onRename != null) {
            Action(R.string.action_rename, R.drawable.ic_rename) {
                onDismiss()
                onRename(target)
            }
        }
        if (target != null && onDelete != null) {
            Action(R.string.action_delete, R.drawable.ic_delete, destructive = true) {
                onDismiss()
                onDelete(target)
            }
        }
    }
}

@Composable
private fun Action(
    title: Int,
    @DrawableRes icon: Int,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    PvotSheetItem(
        title = stringResource(title),
        onClick = onClick,
        icon = { Icon(painterResource(icon), contentDescription = null) },
        destructive = destructive,
    )
}
