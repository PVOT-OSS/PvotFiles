// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.selection

sealed interface BatchEffect {
    data class Trashed(val trashIds: List<String>) : BatchEffect

    data object MovedToSafe : BatchEffect

    data object SafeFolderNeedsSetup : BatchEffect

    data object Failed : BatchEffect

    data object Changed : BatchEffect
}
