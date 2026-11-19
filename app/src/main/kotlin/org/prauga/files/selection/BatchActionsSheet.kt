// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.selection

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotSheetItem
import org.prauga.files.R
import org.prauga.files.common.FilesBottomSheet

@Composable
fun BatchActionsSheet(
    visible: Boolean,
    count: Int,
    onDismiss: () -> Unit,
    onTags: () -> Unit,
    onMoveToSafe: () -> Unit,
) {
    FilesBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = pluralStringResource(R.plurals.selected_count, count, count),
    ) {
        Item(R.string.action_tags, R.drawable.ic_tag, onDismiss, onTags)
        Item(R.string.action_move_to_safe, R.drawable.ic_lock, onDismiss, onMoveToSafe)
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
