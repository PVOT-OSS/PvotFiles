// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VolumeTest {
    private val internal =
        Volume(
            "/storage/emulated/0",
            "Internal",
            primary = true,
            mediaStoreName = "external_primary",
        )
    private val sd =
        Volume("/storage/6200-0A16", "SD card", primary = false, mediaStoreName = "6200-0a16")

    @Test
    fun findsTheVolumeThatHoldsAPath() {
        val volumes = listOf(internal, sd)

        assertEquals(sd, volumes.volumeOf("/storage/6200-0A16/DCIM/a.jpg"))
        assertEquals(internal, volumes.volumeOf("/storage/emulated/0"))
        assertNull(volumes.volumeOf("/storage/6200-0A16x/a"))
        assertNull(volumes.volumeOf("/data/local"))
    }
}
