// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.trash

import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotErrorContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotDialog
import com.prauga.pvot.designsystem.components.PvotDialogAction
import com.prauga.pvot.designsystem.components.PvotDialogActionStyle
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSheetItem
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import com.prauga.pvot.designsystem.theme.PvotSpacing
import java.io.File
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.common.FilesBottomSheet
import org.prauga.files.common.operationMessages
import org.prauga.files.core.model.TrashedItem

@Composable
fun TrashScreen(
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onEvent = viewModel::onEvent
    BackHandler(onBack = onBack)
    val snackbar = remember { SnackbarHostState() }
    val messages = operationMessages()
    val restored = stringResource(R.string.trash_restored)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is TrashEffect.Restored -> snackbar.showSnackbar(restored.format(effect.name))
                is TrashEffect.OperationFailed ->
                    snackbar.showSnackbar(messages.getValue(effect.result))
            }
        }
    }
    val items = (state as? UiState.Success)?.data.orEmpty()
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmId by rememberSaveable { mutableStateOf<String?>(null) }
    val selected = items.firstOrNull { it.id == selectedId }
    val confirm = items.firstOrNull { it.id == confirmId }
    val listState = rememberLazyListState()
    var confirmEmpty by rememberSaveable { mutableStateOf(false) }
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = stringResource(R.string.trash_title),
                onBack = onBack,
                compact = rememberPvotTopBarCompact(listState),
            ) {
                if (items.isNotEmpty()) {
                    PvotTopBarAction(
                        painterResource(R.drawable.ic_delete),
                        stringResource(R.string.trash_empty_action),
                        onClick = { confirmEmpty = true },
                    )
                }
            }
        },
        bottomBar = bottomBar,
        snackbarHost = { PvotSnackbarHost(snackbar) },
        overlay = {
            ItemSheet(
                item = selected,
                onDismiss = { selectedId = null },
                onRestore = { onEvent(TrashEvent.Restore(it.id)) },
                onDeleteForever = { confirmId = it.id },
            )
            PvotDialog(
                visible = confirmEmpty,
                onDismissRequest = { confirmEmpty = false },
                title = stringResource(R.string.trash_empty_title),
                message =
                    pluralStringResource(
                        R.plurals.trash_empty_message,
                        items.size,
                        items.size,
                        Formatter.formatShortFileSize(
                            LocalContext.current,
                            items.sumOf { it.sizeBytes },
                        ),
                    ),
                destructive = true,
            ) {
                PvotDialogAction(
                    label = stringResource(R.string.trash_empty_action),
                    onClick = {
                        confirmEmpty = false
                        onEvent(TrashEvent.EmptyAll)
                    },
                )
            }
            PvotDialog(
                visible = confirm != null,
                onDismissRequest = { confirmId = null },
                title =
                    stringResource(R.string.trash_delete_forever_title, confirm?.name.orEmpty()),
                message = stringResource(R.string.trash_delete_forever_message),
                destructive = true,
            ) {
                PvotDialogAction(
                    label = stringResource(R.string.trash_delete_forever),
                    onClick = {
                        confirm?.let { onEvent(TrashEvent.DeleteForever(it.id)) }
                        confirmId = null
                    },
                    style = PvotDialogActionStyle.Destructive,
                )
                PvotDialogAction(
                    label = stringResource(R.string.cancel),
                    onClick = { confirmId = null },
                )
            }
        },
    ) { padding ->
        when (val content = state) {
            UiState.Loading -> CenteredContent(padding) { PvotLoadingContent() }
            is UiState.Error ->
                CenteredContent(padding) {
                    PvotErrorContent(
                        title = stringResource(R.string.trash_error_title),
                        message = stringResource(R.string.trash_error_message),
                        onRetry = { onEvent(TrashEvent.Retry) },
                    )
                }
            is UiState.Success ->
                if (content.data.isEmpty()) {
                    CenteredContent(padding) {
                        PvotEmptyContent(stringResource(R.string.trash_empty))
                    }
                } else {
                    PvotScreen(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding =
                            PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
                    ) {
                        item {
                            Text(
                                stringResource(R.string.trash_note),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier =
                                    Modifier.fillMaxWidth().padding(horizontal = PvotSpacing.M),
                            )
                        }
                        items(content.data, key = TrashedItem::id) { item ->
                            TrashedRow(item, onClick = { selectedId = item.id })
                        }
                    }
                }
        }
    }
}

@Composable
private fun TrashedRow(item: TrashedItem, onClick: () -> Unit) {
    val deleted =
        DateUtils.getRelativeTimeSpanString(
            item.deletedAtMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
        )
    PvotListRow(
        title = item.name,
        modifier = Modifier.fillMaxWidth(),
        supportingText =
            stringResource(
                R.string.trash_item_details,
                deleted,
                File(item.originalPath).parentFile?.name.orEmpty(),
            ),
        leading = {
            Icon(
                painterResource(if (item.isDirectory) R.drawable.ic_folder else R.drawable.ic_file),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun ItemSheet(
    item: TrashedItem?,
    onDismiss: () -> Unit,
    onRestore: (TrashedItem) -> Unit,
    onDeleteForever: (TrashedItem) -> Unit,
) {
    var shown by remember { mutableStateOf(item) }
    if (item != null) shown = item
    val target = shown
    FilesBottomSheet(visible = item != null, onDismissRequest = onDismiss, title = target?.name) {
        if (target != null) {
            PvotSheetItem(
                title = stringResource(R.string.trash_restore),
                onClick = {
                    onDismiss()
                    onRestore(target)
                },
                icon = { Icon(painterResource(R.drawable.ic_restore), contentDescription = null) },
            )
            PvotSheetItem(
                title = stringResource(R.string.trash_delete_forever),
                onClick = {
                    onDismiss()
                    onDeleteForever(target)
                },
                icon = { Icon(painterResource(R.drawable.ic_delete), contentDescription = null) },
                destructive = true,
            )
        }
    }
}
