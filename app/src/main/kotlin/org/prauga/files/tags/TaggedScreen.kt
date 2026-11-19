// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.tags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotDialog
import com.prauga.pvot.designsystem.components.PvotDialogAction
import com.prauga.pvot.designsystem.components.PvotDialogActionStyle
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.common.FileEntryRow
import org.prauga.files.common.rememberFileLauncher
import org.prauga.files.core.model.FileEntry
import org.prauga.files.info.EntryActionsSheets
import org.prauga.files.selection.BatchOverlay
import org.prauga.files.selection.BatchTopBar
import org.prauga.files.selection.rememberBatchSelection

@Composable
fun TaggedScreen(
    tagName: String,
    onBack: () -> Unit,
    onOpenFolder: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: TaggedViewModel =
        hiltViewModel<TaggedViewModel, TaggedViewModel.Factory>(key = "tag:$tagName") {
            it.create(tagName)
        },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack)
    LifecycleResumeEffect(viewModel) {
        viewModel.onEvent(TaggedEvent.Refresh)
        onPauseOrDispose {}
    }
    val snackbar = remember { SnackbarHostState() }
    val launcher = rememberFileLauncher(snackbar)
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var actionsPath by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val batch =
        rememberBatchSelection((state as? UiState.Success)?.data.orEmpty(), launcher, snackbar) {
            viewModel.onEvent(TaggedEvent.Refresh)
        }
    BackHandler(enabled = batch.active) { batch.clear() }
    PvotScreenScaffold(
        topBar = {
            if (batch.active) {
                BatchTopBar(batch, rememberPvotTopBarCompact(listState))
            } else {
                PvotTopBar(
                    title = tagName,
                    onBack = onBack,
                    compact = rememberPvotTopBarCompact(listState),
                ) {
                    PvotTopBarAction(
                        icon = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.tag_delete),
                        onClick = { confirmDelete = true },
                    )
                }
            }
        },
        bottomBar = bottomBar,
        snackbarHost = { PvotSnackbarHost(snackbar) },
        overlay = {
            EntryActionsSheets(
                (state as? UiState.Success)?.data?.firstOrNull { it.path == actionsPath },
                launcher,
                onDismiss = { actionsPath = null },
            )
            BatchOverlay(batch)
            PvotDialog(
                visible = confirmDelete,
                onDismissRequest = { confirmDelete = false },
                title = stringResource(R.string.tag_delete_title, tagName),
                message = stringResource(R.string.tag_delete_message),
                destructive = true,
            ) {
                PvotDialogAction(
                    label = stringResource(R.string.tag_delete),
                    onClick = {
                        confirmDelete = false
                        viewModel.onEvent(TaggedEvent.DeleteTag)
                        onBack()
                    },
                    style = PvotDialogActionStyle.Destructive,
                )
                PvotDialogAction(
                    label = stringResource(R.string.cancel),
                    onClick = { confirmDelete = false },
                )
            }
        },
    ) { padding ->
        when (val content = state) {
            UiState.Loading,
            is UiState.Error -> CenteredContent(padding) { PvotLoadingContent() }
            is UiState.Success ->
                if (content.data.isEmpty()) {
                    CenteredContent(padding) {
                        PvotEmptyContent(stringResource(R.string.tag_empty))
                    }
                } else {
                    PvotScreen(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding =
                            PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
                    ) {
                        items(content.data, key = FileEntry::path) { entry ->
                            FileEntryRow(
                                entry,
                                onClick = {
                                    when {
                                        batch.active -> batch.toggle(it.path)
                                        it is FileEntry.Directory -> onOpenFolder(it.path)
                                        it is FileEntry.File -> launcher.open(it)
                                    }
                                },
                                onLongClick = { batch.toggle(it.path) },
                                modifier = Modifier.fillMaxWidth(),
                                selected = entry.path in batch.selected,
                                onMore = if (batch.active) null else ({ actionsPath = it.path }),
                            )
                        }
                    }
                }
        }
    }
}
