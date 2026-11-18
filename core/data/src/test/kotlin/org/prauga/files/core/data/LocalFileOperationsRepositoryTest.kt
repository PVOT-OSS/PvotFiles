// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.prauga.files.core.model.FileOperationResult

class LocalFileOperationsRepositoryTest {
    @get:Rule val folder = TemporaryFolder()
    private val index = RecordingMediaIndex()

    @Test
    fun createsAFolderAndUpdatesTheIndex() = runTest {
        val result = repository().createFolder(folder.root.path, "Trips")

        assertEquals(FileOperationResult.Done, result)
        assertTrue(File(folder.root, "Trips").isDirectory)
        assertEquals(listOf(listOf(File(folder.root, "Trips").path)), index.updates)
    }

    @Test
    fun refusesAnExistingNameOrAnInvalidOne() = runTest {
        File(folder.root, "Trips").mkdir()
        val repository = repository()

        assertEquals(
            FileOperationResult.AlreadyExists,
            repository.createFolder(folder.root.path, "Trips"),
        )
        assertEquals(
            FileOperationResult.InvalidName,
            repository.createFolder(folder.root.path, "a/b"),
        )
        assertTrue(index.updates.isEmpty())
    }

    @Test
    fun renamesAFileAndUpdatesBothPaths() = runTest {
        val old = File(folder.root, "old.txt").apply { writeText("x") }

        val result = repository().rename(old.path, "new.txt")

        assertEquals(FileOperationResult.Done, result)
        assertFalse(old.exists())
        assertEquals("x", File(folder.root, "new.txt").readText())
        assertEquals(listOf(listOf(old.path, File(folder.root, "new.txt").path)), index.updates)
    }

    @Test
    fun renameDoesNotOverwrite() = runTest {
        val a = File(folder.root, "a.txt").apply { writeText("a") }
        File(folder.root, "b.txt").writeText("b")

        assertEquals(FileOperationResult.AlreadyExists, repository().rename(a.path, "b.txt"))
        assertEquals("b", File(folder.root, "b.txt").readText())
    }

    @Test
    fun renameOfAMissingFileFails() = runTest {
        assertEquals(
            FileOperationResult.Failed,
            repository().rename(File(folder.root, "x").path, "y"),
        )
    }

    private fun TestScope.repository() =
        LocalFileOperationsRepository(index, StandardTestDispatcher(testScheduler))
}
