// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

data class DisplayOptions(
    val sortOrder: SortOrder = SortOrder.Name,
    val layout: ViewLayout = ViewLayout.List,
    val showHidden: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = false,
)
