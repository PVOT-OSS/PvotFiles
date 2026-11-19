// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.selection

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import org.prauga.files.R

@Composable
fun SelectionTopBar(
    count: Int,
    compact: Boolean,
    onClear: () -> Unit,
    onSelectAll: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit,
) {
    PvotTopBar(
        title = pluralStringResource(R.plurals.selected_count, count, count),
        onBack = onClear,
        compact = compact,
    ) {
        PvotTopBarAction(
            painterResource(R.drawable.ic_select_all),
            stringResource(R.string.select_all),
            onSelectAll,
        )
        PvotTopBarAction(
            painterResource(R.drawable.ic_share),
            stringResource(R.string.action_share),
            onShare,
        )
        PvotTopBarAction(
            painterResource(R.drawable.ic_delete),
            stringResource(R.string.action_delete),
            onDelete,
        )
        PvotTopBarAction(
            painterResource(R.drawable.ic_more),
            stringResource(R.string.more_actions),
            onMore,
        )
    }
}
