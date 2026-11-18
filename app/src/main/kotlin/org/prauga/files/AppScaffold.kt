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
import org.prauga.files.home.HomeScreen

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
    BackHandler(enabled = tab != AppTab.Home) { tab = AppTab.Home }
    tabStates.SaveableStateProvider(tab) {
        when (tab) {
            AppTab.Home -> HomeScreen(onOpenDevice = { tab = AppTab.Browse }, bottomBar = navBar)
            AppTab.Browse -> BrowseScreen(bottomBar = navBar)
        }
    }
}
