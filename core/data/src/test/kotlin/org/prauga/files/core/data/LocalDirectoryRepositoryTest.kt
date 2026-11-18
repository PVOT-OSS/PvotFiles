// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.prauga.files.core.model.DirectoryListing
import org.prauga.files.core.model.FileEntry

class LocalDirectoryRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun listsFoldersFirstThenFilesByNameWithoutHiddenEntries() = runTest {
        val root = folder.root
        File(root, "b.txt").writeText("hello")
        File(root, "A.txt").writeText("")
        File(root, ".hidden").writeText("")
        File(root, "zeta").mkdir()
        File(root, "alpha").mkdir()
        File(root, "alpha/one.txt").writeText("")
        File(root, "alpha/.two").writeText("")
        val repository = LocalDirectoryRepository(StandardTestDispatcher(testScheduler))

        val listing = repository.list(root.path) as DirectoryListing.Entries

        assertEquals(listOf("alpha", "zeta", "A.txt", "b.txt"), listing.entries.map { it.name })
        assertEquals(1, (listing.entries[0] as FileEntry.Directory).childCount)
        assertEquals(5L, (listing.entries[3] as FileEntry.File).sizeBytes)
    }

    @Test
    fun missingFolderIsNotFound() = runTest {
        val repository = LocalDirectoryRepository(StandardTestDispatcher(testScheduler))

        assertEquals(DirectoryListing.NotFound, repository.list(File(folder.root, "missing").path))
    }

    @Test
    fun aFileIsNotAFolder() = runTest {
        val file = folder.newFile("note.txt")
        val repository = LocalDirectoryRepository(StandardTestDispatcher(testScheduler))

        assertEquals(DirectoryListing.NotFound, repository.list(file.path))
    }
}
