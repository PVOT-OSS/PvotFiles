// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.prauga.files.core.model.TextPreview

class TextReaderTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun readsSmallFilesWhole() {
        val file = folder.newFile("a.txt").apply { writeText("héllo") }

        assertEquals(TextPreview("héllo", truncated = false), TextReader.read(file))
    }

    @Test
    fun cutsLongFilesWithoutABrokenCharacter() {
        val file = folder.newFile("b.txt").apply { writeText("abé") }

        assertEquals(TextPreview("ab", truncated = true), TextReader.read(file, maxBytes = 3))
    }
}
