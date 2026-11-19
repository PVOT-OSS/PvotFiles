// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.junit.Assert.assertEquals
import org.junit.Test
import org.prauga.files.core.model.SafeItem

class SafeIndexCodecTest {
    @Test
    fun namesWithTabsAndNewLinesSurvive() {
        val items =
            listOf(
                SafeItem("a1", "tax\treturn\n2026.pdf", 10, 5),
                SafeItem("b2", "photo.jpg", 3, 7),
            )

        assertEquals(items, SafeIndexCodec.decode(SafeIndexCodec.encode(items)))
    }

    @Test
    fun brokenLinesAreSkipped() {
        assertEquals(
            listOf(SafeItem("a", "x", 1, 2)),
            SafeIndexCodec.decode("a\tx\t1\t2\nnonsense\n"),
        )
    }
}
