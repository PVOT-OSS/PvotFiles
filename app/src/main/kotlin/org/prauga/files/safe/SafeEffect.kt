// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.safe

sealed interface SafeEffect {
    data class OpenFile(val path: String) : SafeEffect

    data class MovedOut(val name: String) : SafeEffect

    data object Failed : SafeEffect
}
