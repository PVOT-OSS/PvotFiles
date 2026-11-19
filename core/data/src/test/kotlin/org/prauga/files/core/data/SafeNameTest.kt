// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SafeNameTest {
    @Test
    fun keepsOnlyTheLastPathPart() {
        assertEquals("evil.txt", SafeName.of("../../data/evil.txt"))
        assertEquals("b.pdf", SafeName.of("a\\b.pdf"))
    }

    @Test
    fun emptyOrDotNamesGetAFallback() {
        assertEquals("file", SafeName.of(null))
        assertEquals("file", SafeName.of(".."))
        assertEquals("file", SafeName.of("   "))
    }
}
