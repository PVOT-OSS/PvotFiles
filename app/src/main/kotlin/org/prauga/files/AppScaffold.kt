// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.prauga.files.access.AccessScreen
import org.prauga.files.browse.BrowseScreen
import org.prauga.files.category.CategoryScreen
import org.prauga.files.clean.CleanListScreen
import org.prauga.files.clean.CleanScreen
import org.prauga.files.clean.UnusedAppsScreen
import org.prauga.files.core.model.CleanCategory
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.SavedLocation
import org.prauga.files.home.HomeScreen
import org.prauga.files.locations.TreeScreen
import org.prauga.files.safe.SafeFolderScreen
import org.prauga.files.search.SearchScreen
import org.prauga.files.settings.AboutScreen
import org.prauga.files.settings.SettingsScreen
import org.prauga.files.tags.TaggedScreen
import org.prauga.files.trash.TrashScreen

@Composable
fun AppScaffold() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Box(contentAlignment = Alignment.Center) { AccessScreen { AppTabs() } }
    }
}

@Composable
private fun AppTabs() {
    var tab by rememberSaveable { mutableStateOf(AppTab.Home) }
    val tabStates = rememberSaveableStateHolder()
    val navBar: @Composable () -> Unit = { AppNavBar(selected = tab, onSelect = { tab = it }) }
    var browseTarget by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(enabled = tab != AppTab.Home) { tab = AppTab.Home }
    tabStates.SaveableStateProvider(tab) {
        when (tab) {
            AppTab.Home ->
                HomeTab(
                    onOpenDevice = { tab = AppTab.Browse },
                    onOpenFolder = {
                        browseTarget = it
                        tab = AppTab.Browse
                    },
                    bottomBar = navBar,
                )
            AppTab.Clean -> CleanTab(bottomBar = navBar)
            AppTab.Search -> SearchScreen(bottomBar = navBar)
            AppTab.Browse ->
                BrowseScreen(
                    bottomBar = navBar,
                    openPath = browseTarget,
                    onOpened = { browseTarget = null },
                )
        }
    }
}

@Composable
private fun CleanTab(bottomBar: @Composable () -> Unit) {
    var open by rememberSaveable { mutableStateOf<CleanCategory?>(null) }
    var unusedApps by rememberSaveable { mutableStateOf(false) }
    val category = open
    when {
        unusedApps -> UnusedAppsScreen(onBack = { unusedApps = false }, bottomBar = bottomBar)
        category != null ->
            CleanListScreen(category, onBack = { open = null }, bottomBar = bottomBar)
        else ->
            CleanScreen(
                onOpenCategory = { open = it },
                onOpenUnusedApps = { unusedApps = true },
                bottomBar = bottomBar,
            )
    }
}

@Composable
private fun HomeTab(
    onOpenDevice: () -> Unit,
    onOpenFolder: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    var category by rememberSaveable { mutableStateOf<FileCategory?>(null) }
    var trashOpen by rememberSaveable { mutableStateOf(false) }
    var safeOpen by rememberSaveable { mutableStateOf(false) }
    var tagOpen by rememberSaveable { mutableStateOf<String?>(null) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var aboutOpen by rememberSaveable { mutableStateOf(false) }
    var locationUri by rememberSaveable { mutableStateOf<String?>(null) }
    var locationLabel by rememberSaveable { mutableStateOf("") }
    val location = locationUri?.let { SavedLocation(it, locationLabel) }
    val open = category
    val tag = tagOpen
    when {
        location != null ->
            TreeScreen(location, onBack = { locationUri = null }, bottomBar = bottomBar)
        aboutOpen -> AboutScreen(onBack = { aboutOpen = false }, bottomBar = bottomBar)
        settingsOpen ->
            SettingsScreen(
                onBack = { settingsOpen = false },
                onOpenAbout = { aboutOpen = true },
                bottomBar = bottomBar,
            )
        trashOpen -> TrashScreen(onBack = { trashOpen = false }, bottomBar = bottomBar)
        safeOpen -> SafeFolderScreen(onBack = { safeOpen = false }, bottomBar = bottomBar)
        tag != null ->
            TaggedScreen(
                tagName = tag,
                onBack = { tagOpen = null },
                onOpenFolder = onOpenFolder,
                bottomBar = bottomBar,
            )
        open != null -> CategoryScreen(open, onBack = { category = null }, bottomBar = bottomBar)
        else ->
            HomeScreen(
                onOpenDevice = onOpenDevice,
                onOpenFolder = onOpenFolder,
                onOpenCategory = { category = it },
                onOpenTrash = { trashOpen = true },
                onOpenSafe = { safeOpen = true },
                onOpenTag = { tagOpen = it },
                onOpenSettings = { settingsOpen = true },
                onOpenLocation = {
                    locationLabel = it.label
                    locationUri = it.uri
                },
                bottomBar = bottomBar,
            )
    }
}
