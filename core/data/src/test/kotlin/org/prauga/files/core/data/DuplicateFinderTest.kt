// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.junit.Assert.assertEquals
import org.junit.Test
import org.prauga.files.core.model.FileEntry

class DuplicateFinderTest {
    private fun file(path: String, size: Long, modified: Long) =
        FileEntry.File(path, path, modified, sizeBytes = size)

    @Test
    fun groupsSameSizeAndSameContentOldestFirst() {
        val a = file("a", 10, modified = 2)
        val b = file("b", 10, modified = 1)
        val c = file("c", 10, modified = 3)
        val d = file("d", 20, modified = 1)
        val contents = mapOf("a" to "x", "b" to "x", "c" to "y", "d" to "x")

        assertEquals(
            listOf(listOf(b, a)),
            DuplicateFinder.groups(listOf(a, b, c, d), contents::get),
        )
    }

    @Test
    fun emptyAndUnreadableFilesAreSkipped() {
        val empty1 = file("e1", 0, 1)
        val empty2 = file("e2", 0, 1)
        val gone = file("g", 5, 1)
        val kept = file("k", 5, 1)

        assertEquals(
            emptyList<List<FileEntry.File>>(),
            DuplicateFinder.groups(listOf(empty1, empty2, gone, kept)) {
                if (it == "g") null else "same"
            },
        )
    }
}
