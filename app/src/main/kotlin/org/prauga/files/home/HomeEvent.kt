// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.home

sealed interface HomeEvent {
    data object Refresh : HomeEvent

    data class AddLocation(val treeUri: String) : HomeEvent
}
