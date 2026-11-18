// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

fun List<FileEntry>.sortedFor(order: SortOrder): List<FileEntry> {
    val byName = compareBy(String.CASE_INSENSITIVE_ORDER, FileEntry::name)
    val within: Comparator<FileEntry> =
        when (order) {
            SortOrder.Name -> byName
            SortOrder.Modified -> compareByDescending(FileEntry::lastModifiedMillis).then(byName)
            SortOrder.Size -> compareByDescending<FileEntry> { it.sizeKey() }.then(byName)
            SortOrder.Type ->
                compareBy(String.CASE_INSENSITIVE_ORDER, FileEntry::extension).then(byName)
        }
    return sortedWith(compareBy<FileEntry> { it !is FileEntry.Directory }.then(within))
}

private fun FileEntry.sizeKey(): Long =
    when (this) {
        is FileEntry.Directory -> childCount.toLong()
        is FileEntry.File -> sizeBytes
    }

private val FileEntry.extension: String
    get() =
        if (this is FileEntry.File) name.substringAfterLast('.', missingDelimiterValue = "") else ""
