// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotErrorContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.common.FileEntryRow
import org.prauga.files.common.FileEntryTile
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.ViewLayout

@Composable
fun BrowseScreen(bottomBar: @Composable () -> Unit, viewModel: BrowseViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(enabled = state.canGoUp) { viewModel.onEvent(BrowseEvent.Up) }
    BrowseContent(state, viewModel::onEvent, bottomBar)
}

@Composable
private fun BrowseContent(
    state: BrowseUiState,
    onEvent: (BrowseEvent) -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    var optionsVisible by rememberSaveable { mutableStateOf(false) }
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = state.folderName ?: stringResource(R.string.browse_root_title),
                onBack = if (state.canGoUp) ({ onEvent(BrowseEvent.Up) }) else null,
            ) {
                PvotTopBarAction(
                    icon = painterResource(R.drawable.ic_sort),
                    contentDescription = stringResource(R.string.options_title),
                    onClick = { optionsVisible = true },
                )
            }
        },
        bottomBar = bottomBar,
        overlay = {
            ViewOptionsSheet(
                visible = optionsVisible,
                options = state.options,
                onDismiss = { optionsVisible = false },
                onEvent = onEvent,
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
                                padding,
                                onOpen = { onEvent(BrowseEvent.Open(it)) },
                            )
                        ViewLayout.Grid ->
                            EntryGrid(
                                content.data,
                                padding,
                                onOpen = { onEvent(BrowseEvent.Open(it)) },
                            )
                    }
                }
        }
    }
}

@Composable
private fun EntryList(
    entries: List<FileEntry>,
    padding: PaddingValues,
    onOpen: (FileEntry.Directory) -> Unit,
) {
    PvotScreen(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
    ) {
        items(entries, key = { it.path }) { entry ->
            FileEntryRow(entry, onOpenDirectory = onOpen, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun EntryGrid(
    entries: List<FileEntry>,
    padding: PaddingValues,
    onOpen: (FileEntry.Directory) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(GridCellMinWidth),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
        verticalArrangement = Arrangement.spacedBy(PvotSpacing.S),
        horizontalArrangement = Arrangement.spacedBy(PvotSpacing.S),
    ) {
        items(entries, key = { it.path }) { entry ->
            FileEntryTile(entry, onOpenDirectory = onOpen)
        }
    }
}

private val GridCellMinWidth = 104.dp
