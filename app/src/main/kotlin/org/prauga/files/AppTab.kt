// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files

import com.prauga.pvot.designsystem.navigation.PvotTabItem

enum class AppTab(val item: PvotTabItem) {
    Home(PvotTabItem(R.drawable.ic_home, R.string.tab_home, R.string.tab_home)),
    Clean(PvotTabItem(R.drawable.ic_clean, R.string.tab_clean, R.string.tab_clean)),
    Search(PvotTabItem(R.drawable.ic_search, R.string.tab_search, R.string.tab_search)),
    Browse(PvotTabItem(R.drawable.ic_folder, R.string.tab_browse, R.string.tab_browse)),
}
