// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

sealed interface FileEntry {
    val path: String
    val name: String
    val lastModifiedMillis: Long

    data class Directory(
        override val path: String,
        override val name: String,
        override val lastModifiedMillis: Long,
        val childCount: Int,
    ) : FileEntry

    data class File(
        override val path: String,
        override val name: String,
        override val lastModifiedMillis: Long,
        val sizeBytes: Long,
    ) : FileEntry
}
