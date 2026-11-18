// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import org.prauga.files.R
import org.prauga.files.common.FileEntryRow
import org.prauga.files.core.model.FileEntry

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
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = state.folderName ?: stringResource(R.string.browse_root_title),
                onBack = if (state.canGoUp) ({ onEvent(BrowseEvent.Up) }) else null,
            )
        },
        bottomBar = bottomBar,
    ) { padding ->
        when (val content = state.content) {
            UiState.Loading -> Centered(padding) { PvotLoadingContent() }
            is UiState.Error ->
                Centered(padding) {
                    PvotErrorContent(
                        title = stringResource(R.string.browse_error_title),
                        message = stringResource(R.string.browse_error_message),
                        onRetry = { onEvent(BrowseEvent.Retry) },
                    )
                }
            is UiState.Success ->
                if (content.data.isEmpty()) {
                    Centered(padding) { PvotEmptyContent(stringResource(R.string.browse_empty)) }
                } else {
                    EntryList(content.data, padding, onOpen = { onEvent(BrowseEvent.Open(it)) })
                }
        }
    }
}

@Composable
private fun Centered(padding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
        contentAlignment = Alignment.Center,
    ) {
        content()
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
