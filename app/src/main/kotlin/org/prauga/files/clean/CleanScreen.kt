// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.clean

import android.content.Intent
import android.os.storage.StorageManager
import android.provider.Settings
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotErrorContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import org.prauga.files.R
import org.prauga.files.core.model.CleanCategory
import org.prauga.files.core.model.CleanSummary

@Composable
fun CleanScreen(
    onOpenCategory: (CleanCategory) -> Unit,
    onOpenUnusedApps: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: CleanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.onEvent(CleanEvent.Refresh)
        onPauseOrDispose {}
    }
    val clearCaches =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.onEvent(CleanEvent.Refresh)
        }
    val context = LocalContext.current
    var hasUsageAccess by remember { mutableStateOf(viewModel.hasUsageAccess()) }
    LifecycleResumeEffect(viewModel) {
        hasUsageAccess = viewModel.hasUsageAccess()
        onPauseOrDispose {}
    }
    val usageAccess =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            hasUsageAccess = viewModel.hasUsageAccess()
        }
    val listState = rememberLazyListState()
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = stringResource(R.string.clean_title),
                compact = rememberPvotTopBarCompact(listState),
            )
        },
        bottomBar = bottomBar,
    ) { padding ->
        PvotScreen(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
        ) {
            when (val loaded = state) {
                UiState.Loading -> item { PvotLoadingContent() }
                is UiState.Error ->
                    item {
                        PvotErrorContent(
                            title = stringResource(R.string.clean_error_title),
                            message = stringResource(R.string.clean_error_message),
                            onRetry = { viewModel.onEvent(CleanEvent.Refresh) },
                        )
                    }
                is UiState.Success ->
                    item {
                        PvotListGroup(Modifier.fillMaxWidth()) {
                            loaded.data.forEach { summary ->
                                item { SummaryRow(summary) { onOpenCategory(summary.category) } }
                            }
                        }
                    }
            }
            item {
                PvotListGroup(Modifier.fillMaxWidth()) {
                    item {
                        PvotListRow(
                            title = stringResource(R.string.clean_unused_apps),
                            modifier = Modifier.fillMaxWidth(),
                            supportingText =
                                stringResource(
                                    if (hasUsageAccess) R.string.clean_unused_apps_details
                                    else R.string.clean_usage_access
                                ),
                            leading = {
                                Icon(
                                    painterResource(R.drawable.ic_android),
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                if (hasUsageAccess) {
                                    onOpenUnusedApps()
                                } else {
                                    usageAccess.launch(
                                        Intent(
                                            Settings.ACTION_USAGE_ACCESS_SETTINGS,
                                            "package:${context.packageName}".toUri(),
                                        )
                                    )
                                }
                            },
                        )
                    }
                    item {
                        PvotListRow(
                            title = stringResource(R.string.clean_caches),
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = stringResource(R.string.clean_caches_details),
                            leading = {
                                Icon(
                                    painterResource(R.drawable.ic_clean),
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                clearCaches.launch(Intent(StorageManager.ACTION_CLEAR_APP_CACHE))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(summary: CleanSummary, onClick: () -> Unit) {
    val context = LocalContext.current
    PvotListRow(
        title = stringResource(summary.category.titleRes),
        modifier = Modifier.fillMaxWidth(),
        supportingText =
            if (summary.count == 0) {
                stringResource(R.string.clean_nothing)
            } else {
                stringResource(
                    R.string.home_category_details,
                    Formatter.formatShortFileSize(context, summary.sizeBytes),
                    pluralStringResource(R.plurals.clean_item_count, summary.count, summary.count),
                )
            },
        leading = { Icon(painterResource(summary.category.iconRes), contentDescription = null) },
        onClick = onClick,
        onClickLabel = stringResource(R.string.file_entry_open),
    )
}
