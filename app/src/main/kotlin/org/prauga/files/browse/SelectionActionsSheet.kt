// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSheetItem
import org.prauga.files.R
import org.prauga.files.common.FilesBottomSheet

@Composable
fun SelectionActionsSheet(
    visible: Boolean,
    count: Int,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onTags: () -> Unit,
    onCompress: () -> Unit,
    onMoveToSafe: () -> Unit,
    onInfo: (() -> Unit)?,
) {
    FilesBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = pluralStringResource(R.plurals.selected_count, count, count),
    ) {
        Item(R.string.action_copy, R.drawable.ic_copy, onDismiss, onCopy)
        Item(R.string.action_move, R.drawable.ic_move, onDismiss, onMove)
        Item(R.string.action_tags, R.drawable.ic_tag, onDismiss, onTags)
        Item(R.string.action_compress, R.drawable.ic_archive, onDismiss, onCompress)
        Item(R.string.action_move_to_safe, R.drawable.ic_lock, onDismiss, onMoveToSafe)
        if (onInfo != null) Item(R.string.action_info, R.drawable.ic_info, onDismiss, onInfo)
    }
}

@Composable
private fun Item(title: Int, icon: Int, onDismiss: () -> Unit, action: () -> Unit) {
    PvotSheetItem(
        title = stringResource(title),
        onClick = {
            onDismiss()
            action()
        },
        icon = { Icon(painterResource(icon), contentDescription = null) },
    )
}
