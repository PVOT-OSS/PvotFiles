// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExifDateTest {
    @Test
    fun parsesTheExifFormatInTheGivenZone() {
        assertEquals(1_700_000_000_000L, ExifDate.parse("2023:11:14 22:13:20", ZoneOffset.UTC))
    }

    @Test
    fun ignoresMissingOrBrokenDates() {
        assertNull(ExifDate.parse(null, ZoneOffset.UTC))
        assertNull(ExifDate.parse("0000:00:00 00:00:00", ZoneOffset.UTC))
        assertNull(ExifDate.parse("yesterday", ZoneOffset.UTC))
    }
}
