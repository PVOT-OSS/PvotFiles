// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotCapsule
import com.prauga.pvot.designsystem.components.button.PvotButton
import com.prauga.pvot.designsystem.components.button.PvotButtonStyle
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.files.R
import org.prauga.files.core.model.TransferKind

@Composable
fun PasteBar(clipboard: Clipboard, canPaste: Boolean, onPaste: () -> Unit, onCancel: () -> Unit) {
    PvotCapsule {
        Text(
            pluralStringResource(
                if (clipboard.kind == TransferKind.Copy) R.plurals.paste_copy_count
                else R.plurals.paste_move_count,
                clipboard.paths.size,
                clipboard.paths.size,
            ),
            style = MaterialTheme.typography.labelLarge,
            modifier =
                Modifier.padding(horizontal = PvotSpacing.S).align(Alignment.CenterVertically),
        )
        PvotButton(
            text = stringResource(R.string.cancel),
            onClick = onCancel,
            style = PvotButtonStyle.Text,
        )
        PvotButton(
            text = stringResource(R.string.paste_here),
            onClick = onPaste,
            enabled = canPaste,
        )
    }
}
