// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.clean

sealed interface CleanListEvent {
    data object Refresh : CleanListEvent

    data class Toggle(val path: String) : CleanListEvent

    data object SelectAll : CleanListEvent

    data object ClearSelection : CleanListEvent

    data object DeleteSelection : CleanListEvent

    data class Restore(val trashIds: List<String>) : CleanListEvent
}
