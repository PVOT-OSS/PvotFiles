// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.components.textfield.PvotTextField
import com.prauga.pvot.designsystem.navigation.LocalPvotNavBarSizes
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.common.FileEntryRow
import org.prauga.files.common.rememberFileLauncher
import org.prauga.files.core.model.FileEntry
import org.prauga.files.info.EntryActionsSheets

@Composable
fun SearchScreen(bottomBar: @Composable () -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchContent(state, viewModel::onEvent, bottomBar)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchContent(
    state: SearchUiState,
    onEvent: (SearchEvent) -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val launcher = rememberFileLauncher(snackbar)
    val results = (state.results as? UiState.Success)?.data.orEmpty()
    var actionsPath by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val typing = WindowInsets.isImeVisible
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = stringResource(R.string.search_title),
                compact = rememberPvotTopBarCompact(listState),
            )
        },
        bottomBar = {
            Column(Modifier.imePadding()) {
                SearchField(state.query, onEvent)
                if (!typing) bottomBar()
            }
        },
        snackbarHost = { PvotSnackbarHost(snackbar) },
        overlay = {
            EntryActionsSheets(
                results.firstOrNull { it.path == actionsPath },
                launcher,
                onDismiss = { actionsPath = null },
            )
        },
    ) { padding ->
        when (val found = state.results) {
            null ->
                CenteredContent(padding) { PvotEmptyContent(stringResource(R.string.search_hint)) }
            UiState.Loading -> CenteredContent(padding) { PvotLoadingContent() }
            is UiState.Error ->
                CenteredContent(padding) {
                    PvotErrorContent(
                        title = stringResource(R.string.search_error_title),
                        message = stringResource(R.string.search_error_message),
                        onRetry = { onEvent(SearchEvent.Retry) },
                    )
                }
            is UiState.Success ->
                if (found.data.isEmpty()) {
                    CenteredContent(padding) {
                        PvotEmptyContent(stringResource(R.string.search_no_results))
                    }
                } else {
                    PvotScreen(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding =
                            PvotScreenDefaults.contentPadding(
                                top = padding.calculateTopPadding(),
                                bottom = LocalPvotNavBarSizes.current.barHeight + SearchFieldHeight,
                            ),
                    ) {
                        items(found.data, key = FileEntry.File::path) { file ->
                            FileEntryRow(
                                file,
                                onClick = { launcher.open(file) },
                                onLongClick = { actionsPath = it.path },
                                modifier = Modifier.fillMaxWidth(),
                                onMore = { actionsPath = it.path },
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun SearchField(query: String, onEvent: (SearchEvent) -> Unit) {
    PvotTextField(
        value = query,
        onValueChange = { onEvent(SearchEvent.QueryChanged(it)) },
        modifier =
            Modifier.fillMaxWidth().padding(horizontal = PvotSpacing.L, vertical = PvotSpacing.S),
        placeholder = stringResource(R.string.search_placeholder),
        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
        trailingIcon =
            if (query.isEmpty()) {
                null
            } else {
                {
                    IconButton(onClick = { onEvent(SearchEvent.Clear) }) {
                        Icon(
                            painterResource(R.drawable.ic_clear),
                            contentDescription = stringResource(R.string.search_clear),
                        )
                    }
                }
            },
        singleLine = true,
    )
}

private val SearchFieldHeight = 72.dp
