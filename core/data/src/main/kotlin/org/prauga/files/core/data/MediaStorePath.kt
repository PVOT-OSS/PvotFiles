// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File

internal object MediaStorePath {

    fun resolve(root: String, relativePath: String, displayName: String): String? {
        if (!isSafeName(displayName)) return null
        val folders = relativePath.split('/').filter { it.isNotEmpty() }
        if (!folders.all(::isSafeName)) return null
        return folders
            .fold(File(root)) { parent, folder -> File(parent, folder) }
            .let { File(it, displayName).path }
    }

    private fun isSafeName(name: String): Boolean =
        name.isNotEmpty() && name != "." && name != ".." && '/' !in name && '\u0000' !in name
}
