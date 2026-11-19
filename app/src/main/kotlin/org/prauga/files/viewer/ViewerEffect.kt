// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.viewer

sealed interface ViewerEffect {
    data class Extracted(val folder: String) : ViewerEffect

    data object Failed : ViewerEffect
}
