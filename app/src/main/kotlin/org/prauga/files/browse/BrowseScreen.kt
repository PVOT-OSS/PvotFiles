// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import android.content.Context
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import org.prauga.files.R
import org.prauga.files.core.model.FileEntry

@Composable
fun BrowseScreen(viewModel: BrowseViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(enabled = state.canGoUp) { viewModel.onEvent(BrowseEvent.Up) }
    BrowseContent(state, viewModel::onEvent)
}

@Composable
private fun BrowseContent(state: BrowseUiState, onEvent: (BrowseEvent) -> Unit) {
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = state.folderName ?: stringResource(R.string.browse_internal_storage),
                onBack = if (state.canGoUp) ({ onEvent(BrowseEvent.Up) }) else null,
            )
        }
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
    val context = LocalContext.current
    PvotScreen(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
    ) {
        items(entries, key = { it.path }) { entry ->
            when (entry) {
                is FileEntry.Directory ->
                    PvotListRow(
                        title = entry.name,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText =
                            pluralStringResource(
                                R.plurals.browse_folder_items,
                                entry.childCount,
                                entry.childCount,
                            ),
                        leading = { EntryIcon(R.drawable.ic_folder) },
                        onClick = { onOpen(entry) },
                        onClickLabel = stringResource(R.string.browse_open),
                    )
                is FileEntry.File ->
                    PvotListRow(
                        title = entry.name,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText =
                            stringResource(
                                R.string.browse_file_details,
                                context.formatModified(entry.lastModifiedMillis),
                                Formatter.formatShortFileSize(context, entry.sizeBytes),
                            ),
                        leading = { EntryIcon(R.drawable.ic_file) },
                    )
            }
        }
    }
}

@Composable
private fun EntryIcon(icon: Int) {
    Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
}

private fun Context.formatModified(millis: Long): String {
    val flags =
        if (DateUtils.isToday(millis)) DateUtils.FORMAT_SHOW_TIME
        else DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH
    return DateUtils.formatDateTime(this, millis, flags)
}
