// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotErrorContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotPreferenceRow
import com.prauga.pvot.designsystem.components.list.PvotRowChevron
import com.prauga.pvot.designsystem.components.list.PvotRowIconStyle
import com.prauga.pvot.designsystem.components.progress.PvotProportionBarDefaults
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import java.io.File
import org.prauga.files.R
import org.prauga.files.common.FileEntryRow
import org.prauga.files.common.TagDot
import org.prauga.files.common.rememberFileLauncher
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.SavedLocation
import org.prauga.files.core.model.StorageUsage
import org.prauga.files.core.model.Tag
import org.prauga.files.core.model.Volume
import org.prauga.files.info.EntryActionsSheets

@Composable
fun HomeScreen(
    onOpenDevice: () -> Unit,
    onOpenFolder: (String) -> Unit,
    onOpenCategory: (FileCategory) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenSafe: () -> Unit,
    onOpenTag: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLocation: (SavedLocation) -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val favourites by viewModel.favouriteFolders.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val volumes by viewModel.removableVolumes.collectAsStateWithLifecycle()
    val locations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val pickFolder =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) viewModel.onEvent(HomeEvent.AddLocation(uri.toString()))
        }
    LifecycleResumeEffect(viewModel) {
        viewModel.onEvent(HomeEvent.Refresh)
        onPauseOrDispose {}
    }
    HomeContent(
        state,
        recent = recent,
        favourites = favourites,
        tags = tags,
        volumes = volumes,
        locations = locations,
        downloadsPath = viewModel.downloadsPath,
        onEvent = viewModel::onEvent,
        onOpenDevice = onOpenDevice,
        onOpenFolder = onOpenFolder,
        onOpenCategory = onOpenCategory,
        onOpenTrash = onOpenTrash,
        onOpenSafe = onOpenSafe,
        onOpenTag = onOpenTag,
        onOpenSettings = onOpenSettings,
        onOpenLocation = onOpenLocation,
        onAddLocation = { pickFolder.launch(null) },
        bottomBar = bottomBar,
    )
}

@Composable
private fun HomeContent(
    state: UiState<StorageUsage>,
    recent: List<FileEntry.File>,
    favourites: List<String>,
    tags: List<Tag>,
    volumes: List<Volume>,
    locations: List<SavedLocation>,
    downloadsPath: String?,
    onEvent: (HomeEvent) -> Unit,
    onOpenDevice: () -> Unit,
    onOpenFolder: (String) -> Unit,
    onOpenCategory: (FileCategory) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenSafe: () -> Unit,
    onOpenTag: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLocation: (SavedLocation) -> Unit,
    onAddLocation: () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    val barColors = PvotProportionBarDefaults.tonalColors()
    val listState = rememberLazyListState()
    val snackbar = remember { SnackbarHostState() }
    val launcher = rememberFileLauncher(snackbar)
    var actionsPath by rememberSaveable { mutableStateOf<String?>(null) }
    val compact = rememberPvotTopBarCompact(listState)
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(title = stringResource(R.string.home_title), compact = compact) {
                PvotTopBarAction(
                    icon = painterResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.settings_title),
                    onClick = onOpenSettings,
                )
            }
        },
        bottomBar = bottomBar,
        snackbarHost = { PvotSnackbarHost(snackbar) },
        overlay = {
            EntryActionsSheets(
                recent.firstOrNull { it.path == actionsPath },
                launcher,
                onDismiss = { actionsPath = null },
            )
        },
    ) { padding ->
        PvotScreen(
            modifier = Modifier.fillMaxSize(),
            state = listState,
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
                                        onClick = { onOpenCategory(usage.category) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (favourites.isNotEmpty()) {
                item {
                    PvotListGroup(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.home_favourites),
                    ) {
                        favourites.forEach { path ->
                            item {
                                PvotPreferenceRow(
                                    title = File(path).name,
                                    onClick = { onOpenFolder(path) },
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
                    if (downloadsPath != null) {
                        item {
                            PvotPreferenceRow(
                                title = stringResource(R.string.home_downloads),
                                onClick = { onOpenFolder(downloadsPath) },
                                icon = {
                                    Icon(
                                        painterResource(R.drawable.ic_download),
                                        contentDescription = null,
                                    )
                                },
                                iconStyle = PvotRowIconStyle.Plain,
                                chevron = PvotRowChevron.Arrow,
                            )
                        }
                    }
                    volumes.forEach { volume ->
                        item {
                            PvotPreferenceRow(
                                title = volume.label,
                                onClick = { onOpenFolder(volume.path) },
                                icon = {
                                    Icon(
                                        painterResource(R.drawable.ic_sd_card),
                                        contentDescription = null,
                                    )
                                },
                                iconStyle = PvotRowIconStyle.Plain,
                                chevron = PvotRowChevron.Arrow,
                            )
                        }
                    }
                    locations.forEach { location ->
                        item {
                            PvotPreferenceRow(
                                title = location.label,
                                onClick = { onOpenLocation(location) },
                                icon = {
                                    Icon(
                                        painterResource(R.drawable.ic_cloud),
                                        contentDescription = null,
                                    )
                                },
                                iconStyle = PvotRowIconStyle.Plain,
                                chevron = PvotRowChevron.Arrow,
                            )
                        }
                    }
                    item {
                        PvotPreferenceRow(
                            title = stringResource(R.string.location_add),
                            onClick = onAddLocation,
                            icon = {
                                Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                            },
                            iconStyle = PvotRowIconStyle.Plain,
                            showChevron = false,
                        )
                    }
                    item {
                        PvotPreferenceRow(
                            title = stringResource(R.string.trash_title),
                            onClick = onOpenTrash,
                            icon = {
                                Icon(
                                    painterResource(R.drawable.ic_delete),
                                    contentDescription = null,
                                )
                            },
                            iconStyle = PvotRowIconStyle.Plain,
                            chevron = PvotRowChevron.Arrow,
                        )
                    }
                    item {
                        PvotPreferenceRow(
                            title = stringResource(R.string.safe_title),
                            onClick = onOpenSafe,
                            icon = {
                                Icon(painterResource(R.drawable.ic_lock), contentDescription = null)
                            },
                            iconStyle = PvotRowIconStyle.Plain,
                            chevron = PvotRowChevron.Arrow,
                        )
                    }
                }
            }
            if (tags.isNotEmpty()) {
                item {
                    PvotListGroup(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.home_tags),
                    ) {
                        tags.forEach { tag ->
                            item {
                                PvotPreferenceRow(
                                    title = tag.name,
                                    onClick = { onOpenTag(tag.name) },
                                    icon = { TagDot(tag.color) },
                                    iconStyle = PvotRowIconStyle.Plain,
                                    chevron = PvotRowChevron.Arrow,
                                )
                            }
                        }
                    }
                }
            }
            if (recent.isNotEmpty()) {
                item {
                    PvotListGroup(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.home_recent),
                    ) {
                        recent.forEach { file ->
                            item {
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
    }
}
