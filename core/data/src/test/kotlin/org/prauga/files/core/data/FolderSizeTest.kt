// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FolderSizeTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun countsEverythingInsideAndAddsFileSizes() {
        val root = folder.newFolder("Trips")
        File(root, "a.jpg").writeBytes(ByteArray(10))
        File(root, "day2").mkdir()
        File(root, "day2/b.jpg").writeBytes(ByteArray(5))

        assertEquals(FolderSize.Totals(bytes = 15, items = 3), FolderSize.of(root))
    }
}
