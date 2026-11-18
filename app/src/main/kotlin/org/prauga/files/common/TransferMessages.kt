// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.prauga.files.R
import org.prauga.files.core.model.TransferKind
import org.prauga.files.transfer.TransferOutcome

@Composable
fun transferMessages(): Map<Pair<TransferKind, TransferOutcome>, String> =
    TransferKind.entries
        .flatMap { kind -> TransferOutcome.entries.map { kind to it } }
        .associateWith { (kind, outcome) ->
            val copy = kind == TransferKind.Copy
            stringResource(
                when (outcome) {
                    TransferOutcome.Done ->
                        if (copy) R.string.transfer_copied else R.string.transfer_moved
                    TransferOutcome.IntoItself -> R.string.transfer_into_itself
                    TransferOutcome.Cancelled ->
                        if (copy) R.string.transfer_copy_cancelled
                        else R.string.transfer_move_cancelled
                    TransferOutcome.Failed ->
                        if (copy) R.string.transfer_copy_failed else R.string.transfer_move_failed
                }
            )
        }
