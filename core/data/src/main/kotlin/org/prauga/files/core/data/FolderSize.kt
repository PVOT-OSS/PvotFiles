// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File

internal object FolderSize {
    data class Totals(val bytes: Long, val items: Int)

    fun of(folder: File): Totals {
        var bytes = 0L
        var items = 0
        folder
            .walkTopDown()
            .filter { it != folder }
            .forEach {
                items++
                if (it.isFile) bytes += it.length()
            }
        return Totals(bytes, items)
    }
}
