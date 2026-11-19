// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File

internal object EmptyFolders {
    private const val SYSTEM_FOLDER = "Android"

    fun find(root: File): List<File> =
        root
            .listFiles()
            .orEmpty()
            .filter { it.isDirectory && it.name != SYSTEM_FOLDER && !it.name.startsWith('.') }
            .flatMap { top ->
                top.listFiles().orEmpty().filter { it.isDirectory }.flatMap(::emptyWithin)
            }

    private fun emptyWithin(folder: File): List<File> {
        if (folder.name.startsWith('.')) return emptyList()
        val children = folder.listFiles() ?: return emptyList()
        if (children.isEmpty()) return listOf(folder)
        return children.filter { it.isDirectory }.flatMap(::emptyWithin)
    }
}
