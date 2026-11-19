// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.clean

import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotErrorContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.components.button.PvotButton
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.common.FileEntryRow
import org.prauga.files.common.rememberFileLauncher
import org.prauga.files.core.model.CleanCategory
import org.prauga.files.core.model.FileEntry
import org.prauga.files.info.EntryActionsSheets

@Composable
fun CleanListScreen(
    category: CleanCategory,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: CleanListViewModel =
        hiltViewModel<CleanListViewModel, CleanListViewModel.Factory>(
            key = "clean:${category.name}"
        ) {
            it.create(category)
        },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onEvent = viewModel::onEvent
    BackHandler(onBack = onBack)
    val snackbar = remember { SnackbarHostState() }
    val launcher = rememberFileLauncher(snackbar)
    val resources = LocalResources.current
    val undo = stringResource(R.string.undo)
    val failed = stringResource(R.string.operation_failed)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is CleanListEffect.Trashed -> {
                    val message =
                        resources.getQuantityString(
                            R.plurals.moved_to_trash_count,
                            effect.count,
                            effect.count,
                        )
                    if (
                        snackbar.showSnackbar(message, actionLabel = undo) ==
                            SnackbarResult.ActionPerformed
                    ) {
                        onEvent(CleanListEvent.Restore(effect.trashIds))
                    }
                }
                CleanListEffect.Failed -> snackbar.showSnackbar(failed)
            }
        }
    }
    var actionsPath by rememberSaveable { mutableStateOf<String?>(null) }
    val entries = (state.groups as? UiState.Success)?.data.orEmpty()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = stringResource(category.titleRes),
                onBack = onBack,
                compact = rememberPvotTopBarCompact(listState),
            ) {
                if (entries.isNotEmpty()) {
                    PvotTopBarAction(
                        painterResource(R.drawable.ic_select_all),
                        stringResource(R.string.select_all),
                        onClick = {
                            onEvent(
                                if (state.selection.isEmpty()) CleanListEvent.SelectAll
                                else CleanListEvent.ClearSelection
                            )
                        },
                    )
                }
            }
        },
        bottomBar = bottomBar,
        snackbarHost = { PvotSnackbarHost(snackbar) },
        floatingAction = {
            if (state.selection.isNotEmpty()) {
                PvotButton(
                    text =
                        pluralStringResource(
                            R.plurals.clean_move,
                            state.selection.size,
                            state.selection.size,
                            Formatter.formatShortFileSize(context, state.selectedBytes),
                        ),
                    onClick = { onEvent(CleanListEvent.DeleteSelection) },
                )
            }
        },
        overlay = {
            EntryActionsSheets(
                entries.flatten().firstOrNull { it.path == actionsPath },
                launcher,
                onDismiss = { actionsPath = null },
            )
        },
    ) { padding ->
        when (val loaded = state.groups) {
            UiState.Loading -> CenteredContent(padding) { PvotLoadingContent() }
            is UiState.Error ->
                CenteredContent(padding) {
                    PvotErrorContent(
                        title = stringResource(R.string.clean_error_title),
                        message = stringResource(R.string.clean_error_message),
                        onRetry = { onEvent(CleanListEvent.Refresh) },
                    )
                }
            is UiState.Success ->
                if (loaded.data.isEmpty()) {
                    CenteredContent(padding) {
                        PvotEmptyContent(stringResource(R.string.clean_nothing))
                    }
                } else {
                    PvotScreen(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding =
                            PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
                    ) {
                        loaded.data.forEach { group ->
                            item {
                                PvotListGroup(
                                    Modifier.fillMaxWidth(),
                                    title =
                                        if (category == CleanCategory.Duplicates) group.first().name
                                        else null,
                                ) {
                                    group.forEach { entry ->
                                        item {
                                            Row(entry, entry.path in state.selection, onEvent) {
                                                actionsPath = it.path
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun Row(
    entry: FileEntry,
    selected: Boolean,
    onEvent: (CleanListEvent) -> Unit,
    onMore: (FileEntry) -> Unit,
) {
    FileEntryRow(
        entry,
        onClick = { onEvent(CleanListEvent.Toggle(it.path)) },
        onLongClick = { onEvent(CleanListEvent.Toggle(it.path)) },
        modifier = Modifier.fillMaxWidth(),
        selected = selected,
        onMore = onMore,
        showFolder = true,
    )
}
