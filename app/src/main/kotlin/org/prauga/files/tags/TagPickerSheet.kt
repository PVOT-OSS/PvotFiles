// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.tags

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.designsystem.components.PvotSheetItem
import com.prauga.pvot.designsystem.components.list.PvotListRow
import org.prauga.files.R
import org.prauga.files.common.FilesBottomSheet
import org.prauga.files.common.TagDot
import org.prauga.files.common.operationMessages

@Composable
fun TagPickerSheet(
    path: String?,
    name: String?,
    snackbar: SnackbarHostState,
    onDismiss: () -> Unit,
    viewModel: TagPickerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var creating by rememberSaveable { mutableStateOf(false) }
    val messages = operationMessages()
    LaunchedEffect(path) { if (path != null) viewModel.onEvent(TagPickerEvent.Show(path)) }
    LaunchedEffect(viewModel) {
        viewModel.failures.collect { snackbar.showSnackbar(messages.getValue(it)) }
    }
    FilesBottomSheet(visible = path != null, onDismissRequest = onDismiss, title = name) {
        state.tags.forEach { tag ->
            val selected = tag.name in state.selected
            PvotListRow(
                title = tag.name,
                modifier = Modifier.fillMaxWidth(),
                leading = { TagDot(tag.color) },
                trailing =
                    if (selected) {
                        {
                            Icon(
                                painterResource(R.drawable.ic_check),
                                contentDescription = stringResource(R.string.tag_applied),
                            )
                        }
                    } else {
                        null
                    },
                onClick = { viewModel.onEvent(TagPickerEvent.Toggle(tag.name)) },
                selected = selected,
            )
        }
        PvotSheetItem(
            title = stringResource(R.string.tag_new),
            onClick = { creating = true },
            icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
        )
    }
    NewTagDialog(
        visible = creating,
        onCreate = {
            creating = false
            viewModel.onEvent(TagPickerEvent.Create(it))
        },
        onDismiss = { creating = false },
    )
}
