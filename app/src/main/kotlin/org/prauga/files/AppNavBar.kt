// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files

import androidx.compose.runtime.Composable
import com.prauga.pvot.designsystem.navigation.PvotNavBar

@Composable
fun AppNavBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    PvotNavBar(
        selectedTab = selected.ordinal,
        onTabClick = { onSelect(AppTab.entries[it]) },
        tabs = AppTab.entries.map { it.item },
    )
}
