// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.category

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
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.common.FileEntryRow
import org.prauga.files.common.labelRes
import org.prauga.files.common.rememberFileLauncher
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.FileEntry
import org.prauga.files.info.EntryActionsSheets
import org.prauga.files.selection.BatchOverlay
import org.prauga.files.selection.BatchTopBar
import org.prauga.files.selection.rememberBatchSelection

@Composable
fun CategoryScreen(
    category: FileCategory,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: CategoryViewModel =
        hiltViewModel<CategoryViewModel, CategoryViewModel.Factory>(key = category.name) {
            it.create(category)
        },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack)
    val snackbar = remember { SnackbarHostState() }
    val launcher = rememberFileLauncher(snackbar)
    var actionsPath by rememberSaveable { mutableStateOf<String?>(null) }
    val actionsFile = (state as? UiState.Success)?.data?.firstOrNull { it.path == actionsPath }
    val listState = rememberLazyListState()
    val compact = rememberPvotTopBarCompact(listState)
    val batch =
        rememberBatchSelection((state as? UiState.Success)?.data.orEmpty(), launcher, snackbar) {
            viewModel.onEvent(CategoryEvent.Retry)
        }
    BackHandler(enabled = batch.active) { batch.clear() }
    PvotScreenScaffold(
        topBar = {
            if (batch.active) {
                BatchTopBar(batch, compact)
            } else {
                PvotTopBar(
                    title = stringResource(category.labelRes),
                    onBack = onBack,
                    compact = compact,
                )
            }
        },
        bottomBar = bottomBar,
        snackbarHost = { PvotSnackbarHost(snackbar) },
        overlay = {
            EntryActionsSheets(actionsFile, launcher, onDismiss = { actionsPath = null })
            BatchOverlay(batch)
        },
    ) { padding ->
        when (val content = state) {
            UiState.Loading -> CenteredContent(padding) { PvotLoadingContent() }
            is UiState.Error ->
                CenteredContent(padding) {
                    PvotErrorContent(
                        title = stringResource(R.string.category_error_title),
                        message = stringResource(R.string.category_error_message),
                        onRetry = { viewModel.onEvent(CategoryEvent.Retry) },
                    )
                }
            is UiState.Success ->
                if (content.data.isEmpty()) {
                    CenteredContent(padding) {
                        PvotEmptyContent(stringResource(R.string.category_empty))
                    }
                } else {
                    PvotScreen(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding =
                            PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
                    ) {
                        items(content.data, key = FileEntry.File::path) { file ->
                            FileEntryRow(
                                file,
                                onClick = {
                                    if (batch.active) batch.toggle(file.path)
                                    else launcher.open(file)
                                },
                                onLongClick = { entry -> batch.toggle(entry.path) },
                                modifier = Modifier.fillMaxWidth(),
                                selected = file.path in batch.selected,
                                onMore =
                                    if (batch.active) null
                                    else ({ entry -> actionsPath = entry.path }),
                            )
                        }
                    }
                }
        }
    }
}
