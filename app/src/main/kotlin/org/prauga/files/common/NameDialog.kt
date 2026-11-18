// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotDialog
import com.prauga.pvot.designsystem.components.PvotDialogAction
import com.prauga.pvot.designsystem.components.PvotDialogActionStyle
import com.prauga.pvot.designsystem.components.textfield.PvotTextField
import org.prauga.files.R
import org.prauga.files.core.model.FileNames

@Composable
fun NameDialog(
    visible: Boolean,
    title: String,
    confirmLabel: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable(visible, initialName) { mutableStateOf(initialName) }
    val valid = FileNames.isValid(name)
    val focus = remember { FocusRequester() }
    LaunchedEffect(visible) { if (visible) focus.requestFocus() }
    PvotDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = title,
        content = {
            PvotTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                label = stringResource(R.string.name_label),
                supportingText =
                    if (valid || name.isEmpty()) null else stringResource(R.string.name_invalid),
                isError = !valid && name.isNotEmpty(),
                singleLine = true,
            )
        },
    ) {
        PvotDialogAction(
            label = confirmLabel,
            onClick = { onConfirm(name) },
            style = PvotDialogActionStyle.Primary,
            enabled = valid,
        )
        PvotDialogAction(label = stringResource(R.string.cancel), onClick = onDismiss)
    }
}
