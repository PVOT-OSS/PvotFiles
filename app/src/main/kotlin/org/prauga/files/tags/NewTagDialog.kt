// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.tags

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.prauga.pvot.designsystem.components.PvotDialog
import com.prauga.pvot.designsystem.components.PvotDialogAction
import com.prauga.pvot.designsystem.components.PvotDialogActionStyle
import com.prauga.pvot.designsystem.components.textfield.PvotTextField
import org.prauga.files.R
import org.prauga.files.common.TagDot
import org.prauga.files.common.labelRes
import org.prauga.files.core.model.FileNames
import org.prauga.files.core.model.Tag
import org.prauga.files.core.model.TagColor

@Composable
fun NewTagDialog(visible: Boolean, onCreate: (Tag) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable(visible) { mutableStateOf("") }
    var color by rememberSaveable(visible) { mutableStateOf(TagColor.Blue) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(visible) { if (visible) focus.requestFocus() }
    PvotDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.tag_new),
        content = {
            PvotTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                label = stringResource(R.string.name_label),
                singleLine = true,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TagColor.entries.forEach { option ->
                    val label = stringResource(option.labelRes)
                    val selected = option == color
                    Box(
                        Modifier.size(48.dp)
                            .selectable(selected = selected, role = Role.RadioButton) {
                                color = option
                            }
                            .semantics { contentDescription = label },
                        contentAlignment = Alignment.Center,
                    ) {
                        TagDot(
                            option,
                            size = 32.dp,
                            modifier =
                                if (selected) {
                                    Modifier.border(
                                        3.dp,
                                        MaterialTheme.colorScheme.onSurface,
                                        CircleShape,
                                    )
                                } else {
                                    Modifier
                                },
                        )
                    }
                }
            }
        },
    ) {
        PvotDialogAction(
            label = stringResource(R.string.create),
            onClick = { onCreate(Tag(name.trim(), color)) },
            style = PvotDialogActionStyle.Primary,
            enabled = FileNames.isValid(name.trim()),
        )
        PvotDialogAction(label = stringResource(R.string.cancel), onClick = onDismiss)
    }
}
