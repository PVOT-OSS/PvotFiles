// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.clean

import android.content.Intent
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotErrorContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.components.list.PvotListRowDefaults
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.core.model.UnusedApp

@Composable
fun UnusedAppsScreen(
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: UnusedAppsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack)
    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose {}
    }
    val uninstall =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.refresh()
        }
    val listState = rememberLazyListState()
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = stringResource(R.string.clean_unused_apps),
                onBack = onBack,
                compact = rememberPvotTopBarCompact(listState),
            )
        },
        bottomBar = bottomBar,
    ) { padding ->
        when (val loaded = state) {
            null ->
                CenteredContent(padding) {
                    PvotEmptyContent(stringResource(R.string.clean_usage_access))
                }
            UiState.Loading -> CenteredContent(padding) { PvotLoadingContent() }
            is UiState.Error ->
                CenteredContent(padding) {
                    PvotErrorContent(
                        title = stringResource(R.string.clean_error_title),
                        message = stringResource(R.string.clean_error_message),
                        onRetry = viewModel::refresh,
                    )
                }
            is UiState.Success ->
                if (loaded.data.isEmpty()) {
                    CenteredContent(padding) {
                        PvotEmptyContent(stringResource(R.string.clean_no_unused_apps))
                    }
                } else {
                    PvotScreen(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding =
                            PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
                    ) {
                        item {
                            PvotListGroup(Modifier.fillMaxWidth()) {
                                loaded.data.forEach { app ->
                                    item {
                                        AppRow(app, viewModel::icon) {
                                            uninstall.launch(
                                                Intent(
                                                    Intent.ACTION_DELETE,
                                                    "package:${app.packageName}".toUri(),
                                                )
                                            )
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
private fun AppRow(
    app: UnusedApp,
    loadIcon: suspend (String, Int) -> android.graphics.Bitmap?,
    onUninstall: () -> Unit,
) {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { PvotListRowDefaults.LeadingSize.roundToPx() }
    val icon by
        produceState<ImageBitmap?>(null, app.packageName, sizePx) {
            value = loadIcon(app.packageName, sizePx)?.asImageBitmap()
        }
    val lastUsed =
        app.lastUsedMillis?.let {
            stringResource(
                R.string.clean_last_used,
                DateUtils.getRelativeTimeSpanString(it).toString(),
            )
        } ?: stringResource(R.string.clean_never_used)
    PvotListRow(
        title = app.label,
        modifier = Modifier.fillMaxWidth(),
        supportingText =
            stringResource(
                R.string.home_category_details,
                lastUsed,
                Formatter.formatShortFileSize(context, app.sizeBytes),
            ),
        leading = {
            icon?.let {
                Image(
                    it,
                    contentDescription = null,
                    modifier = Modifier.size(PvotListRowDefaults.LeadingSize),
                )
            }
        },
        onClick = onUninstall,
        onClickLabel = stringResource(R.string.clean_uninstall),
    )
}
