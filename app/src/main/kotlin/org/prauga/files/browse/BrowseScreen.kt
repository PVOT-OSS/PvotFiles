// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotErrorContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotDialog
import com.prauga.pvot.designsystem.components.PvotDialogAction
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import com.prauga.pvot.designsystem.theme.PvotSpacing
import java.io.File
import kotlinx.coroutines.flow.Flow
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.common.FileActionsSheet
import org.prauga.files.common.FileEntryRow
import org.prauga.files.common.FileEntryTile
import org.prauga.files.common.NameDialog
import org.prauga.files.common.operationMessages
import org.prauga.files.common.rememberFileLauncher
import org.prauga.files.common.rememberGridTopBarCompact
import org.prauga.files.common.transferMessages
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.ViewLayout
import org.prauga.files.info.InfoSheet
import org.prauga.files.selection.SelectionTopBar
import org.prauga.files.tags.TagPickerSheet

@Composable
fun BrowseScreen(
    bottomBar: @Composable () -> Unit,
    openPath: String? = null,
    onOpened: () -> Unit = {},
    onSearchIn: (String) -> Unit = {},
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(enabled = state.canGoUp) { viewModel.onEvent(BrowseEvent.Up) }
    BackHandler(enabled = state.selection.isNotEmpty()) {
        viewModel.onEvent(BrowseEvent.ClearSelection)
    }
    LaunchedEffect(openPath) {
        if (openPath != null) {
            viewModel.onEvent(BrowseEvent.GoTo(openPath))
            onOpened()
        }
    }
    BrowseContent(state, viewModel.effects, viewModel::onEvent, bottomBar, onSearchIn)
}

@Composable
private fun BrowseContent(
    state: BrowseUiState,
    effects: Flow<BrowseEffect>,
    onEvent: (BrowseEvent) -> Unit,
    bottomBar: @Composable () -> Unit,
    onSearchIn: (String) -> Unit,
) {
    var optionsVisible by rememberSaveable { mutableStateOf(false) }
    val listState = rememberSaveable(state.path, saver = LazyListState.Saver) { LazyListState() }
    val gridState = rememberSaveable(state.path, saver = LazyGridState.Saver) { LazyGridState() }
    val listCompact = rememberPvotTopBarCompact(listState)
    val gridCompact = rememberGridTopBarCompact(gridState)
    val compact =
        when (state.options.layout) {
            ViewLayout.List -> listCompact
            ViewLayout.Grid -> gridCompact
        }
    val snackbar = remember { SnackbarHostState() }
    val launcher = rememberFileLauncher(snackbar)
    var newFolderVisible by rememberSaveable { mutableStateOf(false) }
    val messages = operationMessages()
    val trashedMessage = stringResource(R.string.moved_to_trash)
    val undo = stringResource(R.string.undo)
    val transferMessages = transferMessages()
    val resources = LocalResources.current
    val movedToSafe = stringResource(R.string.safe_moved_in)
    val safeNeedsSetup = stringResource(R.string.safe_needs_setup)
    val context = LocalContext.current
    val requestNotifications =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            onEvent(BrowseEvent.Paste)
        }
    var askedNotifications by rememberSaveable { mutableStateOf(false) }
    val paste = {
        val ask =
            !askedNotifications &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
        if (ask) {
            askedNotifications = true
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onEvent(BrowseEvent.Paste)
        }
    }
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is BrowseEffect.OperationFailed ->
                    snackbar.showSnackbar(messages.getValue(effect.result))
                is BrowseEffect.TransferFinished ->
                    snackbar.showSnackbar(transferMessages.getValue(effect.kind to effect.outcome))
                BrowseEffect.MovedToSafe -> snackbar.showSnackbar(movedToSafe)
                BrowseEffect.SafeFolderNeedsSetup -> snackbar.showSnackbar(safeNeedsSetup)
                is BrowseEffect.Trashed -> {
                    val message =
                        if (effect.names.size == 1) {
                            trashedMessage.format(effect.names.first())
                        } else {
                            resources.getQuantityString(
                                R.plurals.moved_to_trash_count,
                                effect.names.size,
                                effect.names.size,
                            )
                        }
                    val result = snackbar.showSnackbar(message, actionLabel = undo)
                    if (result == SnackbarResult.ActionPerformed) {
                        onEvent(BrowseEvent.Restore(effect.trashIds))
                    }
                }
            }
        }
    }
    val entries = (state.content as? UiState.Success)?.data.orEmpty()
    var actionsPath by rememberSaveable { mutableStateOf<String?>(null) }
    var renamePath by rememberSaveable { mutableStateOf<String?>(null) }
    var tagsPath by rememberSaveable { mutableStateOf<String?>(null) }
    var infoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingRename by rememberSaveable { mutableStateOf<List<String>?>(null) }
    val actionsEntry = entries.firstOrNull { it.path == actionsPath }
    val renameEntry = entries.firstOrNull { it.path == renamePath }
    val selecting = state.selection.isNotEmpty()
    var selectionSheet by rememberSaveable { mutableStateOf(false) }
    var tagSelection by rememberSaveable { mutableStateOf(false) }
    val onLongClick: (FileEntry) -> Unit = { onEvent(BrowseEvent.ToggleSelection(it.path)) }
    val onMore: (FileEntry) -> Unit = { actionsPath = it.path }
    val onClick: (FileEntry) -> Unit = { entry ->
        when {
            selecting -> onEvent(BrowseEvent.ToggleSelection(entry.path))
            entry is FileEntry.Directory -> onEvent(BrowseEvent.Open(entry))
            entry is FileEntry.File -> launcher.open(entry)
        }
    }
    PvotScreenScaffold(
        topBar = {
            if (selecting) {
                SelectionTopBar(
                    count = state.selection.size,
                    compact = compact,
                    onClear = { onEvent(BrowseEvent.ClearSelection) },
                    onSelectAll = { onEvent(BrowseEvent.SelectAll) },
                    onShare = {
                        launcher.shareAll(
                            entries.filterIsInstance<FileEntry.File>().filter {
                                it.path in state.selection
                            }
                        )
                    },
                    onDelete = { onEvent(BrowseEvent.DeleteSelection) },
                    onMore = { selectionSheet = true },
                )
            } else {
                PvotTopBar(
                    title = state.folderName ?: stringResource(R.string.browse_root_title),
                    onBack = if (state.canGoUp) ({ onEvent(BrowseEvent.Up) }) else null,
                    compact = compact,
                ) {
                    if (state.path != null) {
                        PvotTopBarAction(
                            icon =
                                painterResource(
                                    if (state.favourite) R.drawable.ic_star
                                    else R.drawable.ic_star_border
                                ),
                            contentDescription =
                                stringResource(
                                    if (state.favourite) R.string.favourite_remove
                                    else R.string.favourite_add
                                ),
                            onClick = { onEvent(BrowseEvent.ToggleFavourite) },
                        )
                        PvotTopBarAction(
                            icon = painterResource(R.drawable.ic_search),
                            contentDescription = stringResource(R.string.search_this_folder),
                            onClick = { state.path?.let(onSearchIn) },
                        )
                        PvotTopBarAction(
                            icon = painterResource(R.drawable.ic_new_folder),
                            contentDescription = stringResource(R.string.new_folder),
                            onClick = { newFolderVisible = true },
                        )
                    }
                    PvotTopBarAction(
                        icon = painterResource(R.drawable.ic_sort),
                        contentDescription = stringResource(R.string.options_title),
                        onClick = { optionsVisible = true },
                    )
                }
            }
        },
        bottomBar = bottomBar,
        snackbarHost = { PvotSnackbarHost(snackbar) },
        floatingAction = {
            state.clipboard?.let { clipboard ->
                PasteBar(
                    clipboard = clipboard,
                    canPaste = state.path != null,
                    onPaste = paste,
                    onCancel = { onEvent(BrowseEvent.ClearClipboard) },
                )
            }
        },
        overlay = {
            ViewOptionsSheet(
                visible = optionsVisible,
                options = state.options,
                onDismiss = { optionsVisible = false },
                onEvent = onEvent,
            )
            FileActionsSheet(
                actionsEntry,
                launcher,
                onDismiss = { actionsPath = null },
                onRename = { renamePath = it.path },
                onDelete = { onEvent(BrowseEvent.Delete(it.path)) },
                onCopy = { onEvent(BrowseEvent.Copy(it.path)) },
                onMove = { onEvent(BrowseEvent.Move(it.path)) },
                onTags = { tagsPath = it.path },
                onInfo = { infoPath = it.path },
                onCompress = { onEvent(BrowseEvent.Compress(listOf(it.path))) },
                onExtract = { onEvent(BrowseEvent.Extract(it.path)) },
                onMoveToSafe = { onEvent(BrowseEvent.MoveToSafe(listOf(it.path))) },
                isFavourite = actionsEntry?.path in state.favourites,
                onToggleFavourite = { onEvent(BrowseEvent.ToggleFavouriteOf(it.path)) },
            )
            InfoSheet(path = infoPath, onDismiss = { infoPath = null })
            TagPickerSheet(
                paths =
                    when {
                        tagSelection -> state.selection.toList()
                        else -> listOfNotNull(tagsPath)
                    },
                title = if (tagSelection) null else tagsPath?.let { File(it).name },
                snackbar = snackbar,
                onDismiss = {
                    tagsPath = null
                    tagSelection = false
                },
            )
            SelectionActionsSheet(
                visible = selectionSheet,
                count = state.selection.size,
                onDismiss = { selectionSheet = false },
                onCopy = { onEvent(BrowseEvent.CopySelection) },
                onMove = { onEvent(BrowseEvent.MoveSelection) },
                onTags = { tagSelection = true },
                onCompress = { onEvent(BrowseEvent.CompressSelection) },
                onMoveToSafe = { onEvent(BrowseEvent.MoveSelectionToSafe) },
                onInfo =
                    state.selection.singleOrNull()?.let { only ->
                        {
                            onEvent(BrowseEvent.ClearSelection)
                            infoPath = only
                        }
                    },
            )
            NameDialog(
                visible = renameEntry != null,
                title = stringResource(R.string.rename_title),
                confirmLabel = stringResource(R.string.action_rename),
                initialName = renameEntry?.name.orEmpty(),
                onConfirm = { name ->
                    renameEntry?.let { entry ->
                        if (
                            entry is FileEntry.File && extensionOf(entry.name) != extensionOf(name)
                        ) {
                            pendingRename = listOf(entry.path, name)
                        } else {
                            onEvent(BrowseEvent.Rename(entry.path, name))
                        }
                    }
                    renamePath = null
                },
                onDismiss = { renamePath = null },
            )
            val pending = pendingRename
            PvotDialog(
                visible = pending != null,
                onDismissRequest = { pendingRename = null },
                title = stringResource(R.string.rename_extension_title),
                message =
                    stringResource(
                        R.string.rename_extension_message,
                        pending?.let { extensionOf(File(it[0]).name) }.orEmpty().ifEmpty { "—" },
                        pending?.let { extensionOf(it[1]) }.orEmpty().ifEmpty { "—" },
                    ),
            ) {
                PvotDialogAction(
                    label = stringResource(R.string.action_rename),
                    onClick = {
                        pending?.let { onEvent(BrowseEvent.Rename(it[0], it[1])) }
                        pendingRename = null
                    },
                )
            }
            NameDialog(
                visible = newFolderVisible,
                title = stringResource(R.string.new_folder),
                confirmLabel = stringResource(R.string.create),
                initialName = "",
                onConfirm = {
                    newFolderVisible = false
                    onEvent(BrowseEvent.CreateFolder(it))
                },
                onDismiss = { newFolderVisible = false },
            )
        },
    ) { padding ->
        when (val content = state.content) {
            UiState.Loading -> CenteredContent(padding) { PvotLoadingContent() }
            is UiState.Error ->
                CenteredContent(padding) {
                    PvotErrorContent(
                        title = stringResource(R.string.browse_error_title),
                        message = stringResource(R.string.browse_error_message),
                        onRetry = { onEvent(BrowseEvent.Retry) },
                    )
                }
            is UiState.Success ->
                if (content.data.isEmpty()) {
                    CenteredContent(padding) {
                        PvotEmptyContent(stringResource(R.string.browse_empty))
                    }
                } else {
                    when (state.options.layout) {
                        ViewLayout.List ->
                            EntryList(
                                content.data,
                                listState,
                                padding,
                                state.selection,
                                onClick,
                                onLongClick,
                                onMore,
                            )
                        ViewLayout.Grid ->
                            EntryGrid(
                                content.data,
                                gridState,
                                padding,
                                state.selection,
                                onClick,
                                onLongClick,
                            )
                    }
                }
        }
    }
}

@Composable
private fun EntryList(
    entries: List<FileEntry>,
    state: LazyListState,
    padding: PaddingValues,
    selection: Set<String>,
    onClick: (FileEntry) -> Unit,
    onLongClick: (FileEntry) -> Unit,
    onMore: (FileEntry) -> Unit,
) {
    PvotScreen(
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
    ) {
        items(entries, key = { it.path }) { entry ->
            FileEntryRow(
                entry,
                onClick,
                onLongClick,
                Modifier.fillMaxWidth(),
                selected = entry.path in selection,
                onMore = if (selection.isEmpty()) onMore else null,
            )
        }
    }
}

@Composable
private fun EntryGrid(
    entries: List<FileEntry>,
    state: LazyGridState,
    padding: PaddingValues,
    selection: Set<String>,
    onClick: (FileEntry) -> Unit,
    onLongClick: (FileEntry) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(GridCellMinWidth),
        modifier = Modifier.fillMaxSize(),
        state = state,
        contentPadding = PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
        verticalArrangement = Arrangement.spacedBy(PvotSpacing.S),
        horizontalArrangement = Arrangement.spacedBy(PvotSpacing.S),
    ) {
        items(entries, key = { it.path }) { entry ->
            FileEntryTile(entry, onClick, onLongClick, selected = entry.path in selection)
        }
    }
}

private val GridCellMinWidth = 104.dp

private fun extensionOf(name: String): String = name.substringAfterLast('.', "").lowercase()
