// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.prauga.pvot.designsystem.components.list.PvotPreferenceRow
import com.prauga.pvot.designsystem.components.list.PvotRowChevron
import com.prauga.pvot.designsystem.components.list.PvotRowIconStyle
import com.prauga.pvot.designsystem.components.progress.PvotProportionBarDefaults
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import org.prauga.files.R
import org.prauga.files.core.model.StorageUsage

@Composable
fun HomeScreen(
    onOpenDevice: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.onEvent(HomeEvent.Refresh)
        onPauseOrDispose {}
    }
    HomeContent(
        state,
        onEvent = viewModel::onEvent,
        onOpenDevice = onOpenDevice,
        bottomBar = bottomBar,
    )
}

@Composable
private fun HomeContent(
    state: UiState<StorageUsage>,
    onEvent: (HomeEvent) -> Unit,
    onOpenDevice: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    val barColors = PvotProportionBarDefaults.tonalColors()
    PvotScreenScaffold(
        topBar = { PvotTopBar(title = stringResource(R.string.home_title)) },
        bottomBar = bottomBar,
    ) { padding ->
        PvotScreen(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
        ) {
            when (state) {
                UiState.Loading -> item { PvotLoadingContent() }
                is UiState.Error ->
                    item {
                        PvotErrorContent(
                            title = stringResource(R.string.home_error_title),
                            message = stringResource(R.string.home_error_message),
                            onRetry = { onEvent(HomeEvent.Refresh) },
                        )
                    }
                is UiState.Success -> {
                    item { StorageCard(state.data, barColors, Modifier.fillMaxWidth()) }
                    item {
                        PvotListGroup(Modifier.fillMaxWidth()) {
                            state.data.categories.forEachIndexed { index, usage ->
                                item {
                                    CategoryRow(
                                        usage = usage,
                                        totalBytes = state.data.totalBytes,
                                        color =
                                            barColors.segmentColors[
                                                    index % barColors.segmentColors.size],
                                        trackColor = barColors.trackColor,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                PvotListGroup(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.home_locations),
                ) {
                    item {
                        PvotPreferenceRow(
                            title = stringResource(R.string.browse_root_title),
                            onClick = onOpenDevice,
                            icon = {
                                Icon(
                                    painterResource(R.drawable.ic_folder),
                                    contentDescription = null,
                                )
                            },
                            iconStyle = PvotRowIconStyle.Plain,
                            chevron = PvotRowChevron.Arrow,
                        )
                    }
                }
            }
        }
    }
}
