// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaStorePathTest {
    @Test
    fun joinsTheRootFolderAndName() {
        assertEquals(
            "/storage/emulated/0/DCIM/Camera/photo.jpg",
            MediaStorePath.resolve("/storage/emulated/0", "DCIM/Camera/", "photo.jpg"),
        )
    }

    @Test
    fun aFileAtTheRootHasNoFolder() {
        assertEquals(
            "/storage/emulated/0/a.txt",
            MediaStorePath.resolve("/storage/emulated/0", "", "a.txt"),
        )
    }

    @Test
    fun rejectsNamesThatLeaveTheirFolder() {
        assertNull(MediaStorePath.resolve("/root", "Download/", "../evil"))
        assertNull(MediaStorePath.resolve("/root", "Download/", ".."))
        assertNull(MediaStorePath.resolve("/root", "../../data/", "a.txt"))
        assertNull(MediaStorePath.resolve("/root", "Download/", ""))
    }
}
