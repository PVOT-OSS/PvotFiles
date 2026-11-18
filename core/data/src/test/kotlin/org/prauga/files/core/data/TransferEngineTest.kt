// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.prauga.files.core.model.TransferKind
import org.prauga.files.core.model.TransferProgress
import org.prauga.files.core.model.TransferResult

class TransferEngineTest {
    @get:Rule val folder = TemporaryFolder()
    private val index = RecordingMediaIndex()
    private val engine = TransferEngine(index)

    @Test
    fun copiesAFolderTreeAndReportsProgress() {
        val trips = folder.newFolder("Trips")
        File(trips, "a.jpg").writeText("12345")
        File(trips, "day2").mkdir()
        File(trips, "day2/b.jpg").writeText("678")
        val target = folder.newFolder("Backup")
        val progress = mutableListOf<TransferProgress>()

        val result =
            engine.run(
                listOf(trips.path),
                target.path,
                TransferKind.Copy,
                { progress += it },
                { false },
            )

        assertEquals(TransferResult.Done(1), result)
        assertEquals("12345", File(target, "Trips/a.jpg").readText())
        assertEquals("678", File(target, "Trips/day2/b.jpg").readText())
        assertTrue(trips.exists())
        assertEquals(8, progress.last().doneBytes)
        assertEquals(8, progress.last().totalBytes)
        assertEquals(listOf(listOf(File(target, "Trips").path)), index.updates)
    }

    @Test
    fun movingRenamesAndKeepsBothWhenTheNameIsTaken() {
        val file = File(folder.root, "a.txt").apply { writeText("new") }
        val target = folder.newFolder("Docs")
        File(target, "a.txt").writeText("old")

        val result = engine.run(listOf(file.path), target.path, TransferKind.Move, {}, { false })

        assertEquals(TransferResult.Done(1), result)
        assertFalse(file.exists())
        assertEquals("old", File(target, "a.txt").readText())
        assertEquals("new", File(target, "a (1).txt").readText())
    }

    @Test
    fun aFolderCannotGoIntoItself() {
        val trips = folder.newFolder("Trips")
        val inner = File(trips, "inner").apply { mkdir() }

        assertEquals(
            TransferResult.IntoItself,
            engine.run(listOf(trips.path), inner.path, TransferKind.Copy, {}, { false }),
        )
        assertEquals(
            TransferResult.IntoItself,
            engine.run(listOf(trips.path), trips.path, TransferKind.Move, {}, { false }),
        )
    }

    @Test
    fun movingIntoTheSameFolderChangesNothing() {
        val file = File(folder.root, "a.txt").apply { writeText("x") }

        assertEquals(
            TransferResult.Done(0),
            engine.run(listOf(file.path), folder.root.path, TransferKind.Move, {}, { false }),
        )
        assertTrue(file.exists())
    }

    @Test
    fun cancellingRemovesThePartialCopy() {
        val big = File(folder.root, "big.bin").apply { writeBytes(ByteArray(200_000)) }
        val target = folder.newFolder("Out")
        var calls = 0

        val result =
            engine.run(listOf(big.path), target.path, TransferKind.Copy, {}, { calls++ > 1 })

        assertEquals(TransferResult.Cancelled, result)
        assertFalse(File(target, "big.bin").exists())
        assertTrue(big.exists())
    }

    @Test
    fun uniqueNamesKeepTheExtensionAndSkipTakenNumbers() {
        File(folder.root, "photo.jpg").writeText("")
        File(folder.root, "photo (1).jpg").writeText("")
        File(folder.root, "v1.2").mkdir()

        assertEquals(
            "photo (2).jpg",
            TransferEngine.uniqueName(folder.root, "photo.jpg", isDirectory = false),
        )
        assertEquals("v1.2 (1)", TransferEngine.uniqueName(folder.root, "v1.2", isDirectory = true))
        assertEquals(
            "new.txt",
            TransferEngine.uniqueName(folder.root, "new.txt", isDirectory = false),
        )
    }
}
