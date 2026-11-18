// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.prauga.files.core.model.FileOperationResult

class TrashFolderTest {
    @get:Rule val folder = TemporaryFolder()
    private val trash by lazy { TrashFolder(File(folder.root, ".trash-files")) }

    @Test
    fun movesAFileInAndListsIt() {
        val file = File(folder.root, "notes.txt").apply { writeText("hi") }

        val id = checkNotNull(trash.moveIn(file.path, nowMillis = 1_000))

        assertFalse(file.exists())
        val item = trash.list().single()
        assertEquals(id, item.id)
        assertEquals("notes.txt", item.name)
        assertEquals(file.path, item.originalPath)
        assertEquals(1_000, item.deletedAtMillis)
        assertFalse(item.isDirectory)
        assertTrue(File(folder.root, ".trash-files/.nomedia").isFile)
    }

    @Test
    fun restorePutsItBackUnlessTheNameIsTaken() {
        val dir = File(folder.root, "Trips").apply { mkdir() }
        File(dir, "a.jpg").writeText("a")
        val id = checkNotNull(trash.moveIn(dir.path, nowMillis = 1))

        File(folder.root, "Trips").mkdir()
        assertEquals(FileOperationResult.AlreadyExists, trash.restore(id))

        File(folder.root, "Trips").delete()
        assertEquals(FileOperationResult.Done, trash.restore(id))
        assertEquals("a", File(folder.root, "Trips/a.jpg").readText())
        assertTrue(trash.list().isEmpty())
    }

    @Test
    fun deleteForeverRemovesItemAndRecord() {
        val dir = File(folder.root, "Old").apply { mkdir() }
        File(dir, "x").writeText("x")
        val id = checkNotNull(trash.moveIn(dir.path, nowMillis = 1))

        assertEquals(FileOperationResult.Done, trash.deleteForever(id))

        assertTrue(trash.list().isEmpty())
        assertEquals(0, File(folder.root, ".trash-files/files").listFiles()?.size)
    }

    @Test
    fun expiredItemsAreDeleted() {
        val old = File(folder.root, "old").apply { writeText("") }
        val recent = File(folder.root, "recent").apply { writeText("") }
        trash.moveIn(old.path, nowMillis = 0)
        trash.moveIn(recent.path, nowMillis = 900)

        val deleted = trash.deleteExpired(nowMillis = 1_000, keepMillis = 500)

        assertEquals(listOf("old"), deleted.map { it.name })
        assertEquals(listOf("recent"), trash.list().map { it.name })
    }

    @Test
    fun refusesMissingFilesAndTheTrashItself() {
        assertNull(trash.moveIn(File(folder.root, "missing").path, nowMillis = 0))
        val file = File(folder.root, "f").apply { writeText("") }
        trash.moveIn(file.path, nowMillis = 0)
        assertNull(trash.moveIn(File(folder.root, ".trash-files").path, nowMillis = 0))
    }

    @Test
    fun ignoresIdsThatCouldLeaveTheTrash() {
        assertEquals(FileOperationResult.Failed, trash.deleteForever("../../etc"))
        assertEquals(FileOperationResult.Failed, trash.restore("../x"))
    }
}
