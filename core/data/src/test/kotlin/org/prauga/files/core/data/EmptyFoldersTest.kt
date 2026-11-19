// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EmptyFoldersTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun findsNestedEmptyFoldersButNotTopLevelOrSystemOnes() {
        val root = folder.root
        File(root, "Music").mkdir()
        File(root, "Download/old/empty").mkdirs()
        File(root, "Download/full").mkdirs()
        File(root, "Download/full/a.txt").writeText("a")
        File(root, "Android/data/app").mkdirs()
        File(root, "DCIM/.thumbnails").mkdirs()

        assertEquals(
            listOf(File(root, "Download/old/empty")),
            EmptyFolders.find(root).sortedBy { it.path },
        )
    }
}
