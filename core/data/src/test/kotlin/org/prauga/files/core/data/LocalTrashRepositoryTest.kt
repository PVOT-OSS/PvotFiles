// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.prauga.files.core.model.FileOperationResult

class LocalTrashRepositoryTest {
    @get:Rule val folder = TemporaryFolder()
    private val index = RecordingMediaIndex()

    @Test
    fun trashAndRestoreUpdateTheMediaIndex() = runTest {
        val file = File(folder.root, "a.jpg").apply { writeText("a") }
        val repository = repository(atMillis = 0)

        val id = checkNotNull(repository.moveToTrash(file.path))
        assertEquals(FileOperationResult.Done, repository.restore(id))

        assertEquals(listOf(listOf(file.path), listOf(file.path)), index.updates)
        assertTrue(file.exists())
    }

    @Test
    fun listingForgetsItemsOlderThanThirtyDays() = runTest {
        val file = File(folder.root, "a.txt").apply { writeText("") }
        repository(atMillis = 0).moveToTrash(file.path)

        assertEquals(1, repository(atMillis = TimeUnit.DAYS.toMillis(29)).items()?.size)
        assertEquals(0, repository(atMillis = TimeUnit.DAYS.toMillis(31)).items()?.size)
    }

    @Test
    fun knowsItsOwnFolder() = runTest {
        val repository = repository(atMillis = 0)

        assertTrue(repository.isTrash(File(folder.root, ".trash-files").path))
        assertFalse(repository.isTrash(File(folder.root, "Download").path))
    }

    @Test
    fun withoutStorageNothingWorks() = runTest {
        val repository =
            LocalTrashRepository(
                FakeStorage(null),
                index,
                clockAt(0),
                StandardTestDispatcher(testScheduler),
            )

        assertNull(repository.moveToTrash(File(folder.root, "x").path))
        assertNull(repository.items())
    }

    private fun TestScope.repository(atMillis: Long) =
        LocalTrashRepository(
            FakeStorage(folder.root.path),
            index,
            clockAt(atMillis),
            StandardTestDispatcher(testScheduler),
        )

    private fun clockAt(millis: Long) = Clock.fixed(Instant.ofEpochMilli(millis), ZoneOffset.UTC)

    private class FakeStorage(private val root: String?) : StorageRepository {
        override fun primaryRootPath(): String? = root
    }
}
