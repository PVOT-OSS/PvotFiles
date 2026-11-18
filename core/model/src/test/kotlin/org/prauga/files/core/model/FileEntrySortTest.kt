// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FileEntrySortTest {
    private val music = FileEntry.Directory("/m", "music", lastModifiedMillis = 5, childCount = 9)
    private val docs = FileEntry.Directory("/d", "Docs", lastModifiedMillis = 9, childCount = 1)
    private val big = FileEntry.File("/b", "b.zip", lastModifiedMillis = 1, sizeBytes = 900)
    private val small = FileEntry.File("/a", "A.txt", lastModifiedMillis = 7, sizeBytes = 10)
    private val photo = FileEntry.File("/c", "c.jpg", lastModifiedMillis = 3, sizeBytes = 50)
    private val entries = listOf(big, music, small, photo, docs)

    @Test
    fun nameKeepsFoldersFirstAndIgnoresCase() {
        assertEquals(listOf(docs, music, small, big, photo), entries.sortedFor(SortOrder.Name))
    }

    @Test
    fun modifiedPutsTheNewestFirst() {
        assertEquals(listOf(docs, music, small, photo, big), entries.sortedFor(SortOrder.Modified))
    }

    @Test
    fun sizePutsTheLargestFirstAndFoldersByItemCount() {
        assertEquals(listOf(music, docs, big, photo, small), entries.sortedFor(SortOrder.Size))
    }

    @Test
    fun typeGroupsFilesByExtension() {
        assertEquals(listOf(docs, music, photo, small, big), entries.sortedFor(SortOrder.Type))
    }
}
