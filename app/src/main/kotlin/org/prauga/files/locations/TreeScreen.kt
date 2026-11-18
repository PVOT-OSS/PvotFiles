// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.locations

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
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
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.common.FilesBottomSheet
import org.prauga.files.common.rememberFileLauncher
import org.prauga.files.core.model.DocumentEntry
import org.prauga.files.core.model.SavedLocation

@Composable
fun TreeScreen(
    location: SavedLocation,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: TreeViewModel =
        hiltViewModel<TreeViewModel, TreeViewModel.Factory>(key = "tree:${location.uri}") {
            it.create(location)
        },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler { if (state.canGoUp) viewModel.onEvent(TreeEvent.Up) else onBack() }
    val snackbar = remember { SnackbarHostState() }
    val launcher = rememberFileLauncher(snackbar)
    var actionsId by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmRemove by rememberSaveable { mutableStateOf(false) }
    val entries = (state.content as? UiState.Success)?.data.orEmpty()
    val actions = entries.firstOrNull { it.documentId == actionsId }
    val listState = rememberLazyListState()
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = state.title,
                onBack = { if (state.canGoUp) viewModel.onEvent(TreeEvent.Up) else onBack() },
                compact = rememberPvotTopBarCompact(listState),
            ) {
                if (!state.canGoUp) {
                    PvotTopBarAction(
                        icon = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.location_remove),
                        onClick = { confirmRemove = true },
                    )
                }
            }
        },
        bottomBar = bottomBar,
        snackbarHost = { PvotSnackbarHost(snackbar) },
        overlay = {
            FilesBottomSheet(
                visible = actions != null,
                onDismissRequest = { actionsId = null },
                title = actions?.name,
            ) {
                if (actions != null) {
                    PvotSheetItem(
                        title = stringResource(R.string.action_share),
                        onClick = {
                            actionsId = null
                            launcher.shareUri(
                                viewModel.documentUri(actions).toUri(),
                                actions.mimeType,
                            )
                        },
                        icon = {
                            Icon(painterResource(R.drawable.ic_share), contentDescription = null)
                        },
                    )
                }
            }
            PvotDialog(
                visible = confirmRemove,
                onDismissRequest = { confirmRemove = false },
                title = stringResource(R.string.location_remove_title, location.label),
                message = stringResource(R.string.location_remove_message),
            ) {
                PvotDialogAction(
                    label = stringResource(R.string.location_remove),
                    onClick = {
                        confirmRemove = false
                        viewModel.onEvent(TreeEvent.RemoveLocation)
                        onBack()
                    },
                    style = PvotDialogActionStyle.Destructive,
                )
                PvotDialogAction(
                    label = stringResource(R.string.cancel),
                    onClick = { confirmRemove = false },
                )
            }
        },
    ) { padding ->
        when (val content = state.content) {
            UiState.Loading -> CenteredContent(padding) { PvotLoadingContent() }
            is UiState.Error ->
                CenteredContent(padding) {
                    PvotErrorContent(
                        title = stringResource(R.string.location_error_title),
                        message = stringResource(R.string.location_error_message),
                        onRetry = { viewModel.onEvent(TreeEvent.Retry) },
                    )
                }
            is UiState.Success ->
                if (content.data.isEmpty()) {
                    CenteredContent(padding) {
                        PvotEmptyContent(stringResource(R.string.browse_empty))
                    }
                } else {
                    PvotScreen(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding =
                            PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
                    ) {
                        items(content.data, key = DocumentEntry::documentId) { entry ->
                            DocumentRow(
                                entry,
                                onClick = {
                                    if (entry.isDirectory) {
                                        viewModel.onEvent(TreeEvent.Open(entry))
                                    } else {
                                        launcher.openUri(
                                            viewModel.documentUri(entry).toUri(),
                                            entry.mimeType,
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (!entry.isDirectory) actionsId = entry.documentId
                                },
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun DocumentRow(entry: DocumentEntry, onClick: () -> Unit, onLongClick: () -> Unit) {
    val context = LocalContext.current
    PvotListRow(
        title = entry.name,
        modifier = Modifier.fillMaxWidth(),
        supportingText =
            if (entry.isDirectory) {
                stringResource(R.string.location_folder)
            } else {
                Formatter.formatShortFileSize(context, entry.sizeBytes)
            },
        leading = {
            Icon(
                painterResource(
                    if (entry.isDirectory) R.drawable.ic_folder else R.drawable.ic_file
                ),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        onClick = onClick,
        onLongClick = onLongClick,
        onClickLabel = stringResource(R.string.file_entry_open),
    )
}
