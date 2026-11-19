// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSegmentedControl
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotPreferenceRow
import com.prauga.pvot.designsystem.components.list.PvotRowChevron
import com.prauga.pvot.designsystem.components.list.PvotSwitchRow
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.files.R
import org.prauga.files.core.model.ThemeMode

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val options by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(onBack = onBack)
    val listState = rememberLazyListState()
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = stringResource(R.string.settings_title),
                onBack = onBack,
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
            item {
                PvotListGroup(
                    Modifier.fillMaxWidth(),
                    title = stringResource(R.string.settings_appearance),
                ) {
                    item {
                        PvotSegmentedControl(
                            options =
                                listOf(
                                    stringResource(R.string.settings_theme_system),
                                    stringResource(R.string.settings_theme_light),
                                    stringResource(R.string.settings_theme_dark),
                                ),
                            selectedIndex = options.themeMode.ordinal,
                            onSelect = { viewModel.setThemeMode(ThemeMode.entries[it]) },
                            modifier = Modifier.fillMaxWidth().padding(PvotSpacing.M),
                        )
                    }
                    item {
                        PvotSwitchRow(
                            title = stringResource(R.string.settings_dynamic_color),
                            checked = options.dynamicColor,
                            onCheckedChange = viewModel::setDynamicColor,
                            summary = stringResource(R.string.settings_dynamic_color_summary),
                        )
                    }
                }
            }
            item {
                PvotListGroup(
                    Modifier.fillMaxWidth(),
                    title = stringResource(R.string.settings_display),
                ) {
                    item {
                        PvotSwitchRow(
                            title = stringResource(R.string.options_show_hidden),
                            checked = options.showHidden,
                            onCheckedChange = viewModel::setShowHidden,
                            summary = stringResource(R.string.settings_show_hidden_summary),
                        )
                    }
                }
            }
            item {
                PvotListGroup(Modifier.fillMaxWidth()) {
                    item {
                        PvotPreferenceRow(
                            title = stringResource(R.string.about_title),
                            onClick = onOpenAbout,
                            chevron = PvotRowChevron.Arrow,
                        )
                    }
                }
            }
        }
    }
}
