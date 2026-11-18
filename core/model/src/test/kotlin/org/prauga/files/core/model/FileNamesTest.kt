// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileNamesTest {
    @Test
    fun acceptsOrdinaryNames() {
        assertTrue(FileNames.isValid("Holiday photos"))
        assertTrue(FileNames.isValid(".nomedia"))
        assertTrue(FileNames.isValid("résumé.pdf"))
    }

    @Test
    fun rejectsNamesThatCannotBeAFileName() {
        listOf("", "  ", ".", "..", "a/b", "a\u0000b", " padded", "padded ").forEach {
            assertFalse(it, FileNames.isValid(it))
        }
    }

    @Test
    fun rejectsNamesLongerThan255Bytes() {
        assertTrue(FileNames.isValid("a".repeat(255)))
        assertFalse(FileNames.isValid("a".repeat(256)))
        assertFalse(FileNames.isValid("é".repeat(128)))
    }
}
