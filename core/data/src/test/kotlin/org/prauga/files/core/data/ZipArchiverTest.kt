// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.prauga.files.core.model.ArchiveEntry

class ZipArchiverTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun compressThenExtractKeepsFilesAndFolders() {
        val trip = folder.newFolder("Trip")
        File(trip, "day1").mkdir()
        File(trip, "day1/a.txt").writeText("alpha")
        val note = folder.newFile("note.txt").apply { writeText("beta") }
        val archive = File(folder.root, "out.zip")

        ZipArchiver.compress(listOf(trip, note), archive)
        val destination = File(folder.root, "unpacked")
        val created = ZipArchiver.extract(archive, destination)

        assertEquals("alpha", File(destination, "Trip/day1/a.txt").readText())
        assertEquals("beta", File(destination, "note.txt").readText())
        assertEquals(2, created.size)
    }

    @Test
    fun listsEntriesWithoutExtracting() {
        val trip = folder.newFolder("Trip")
        File(trip, "a.txt").writeText("alpha")
        val archive = File(folder.root, "list.zip")
        ZipArchiver.compress(listOf(trip), archive)

        assertEquals(
            listOf(ArchiveEntry("Trip/", 0, true), ArchiveEntry("Trip/a.txt", 5, false)),
            ZipArchiver.entries(archive),
        )
    }

    @Test(expected = IOException::class)
    fun entriesThatEscapeTheDestinationAreRefused() {
        val archive = File(folder.root, "evil.zip")
        ZipOutputStream(archive.outputStream()).use {
            it.putNextEntry(ZipEntry("../escaped.txt"))
            it.write(1)
            it.closeEntry()
        }

        try {
            ZipArchiver.extract(archive, File(folder.root, "dest"))
        } finally {
            assertFalse(File(folder.root, "escaped.txt").exists())
        }
    }
}
