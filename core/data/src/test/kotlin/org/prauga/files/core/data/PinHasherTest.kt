// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {
    @Test
    fun onlyTheSamePinMatches() {
        val salt = PinHasher.newSalt()
        val hash = PinHasher.hash("2468", salt, iterations = 1_000)

        assertTrue(PinHasher.matches("2468", salt, hash, iterations = 1_000))
        assertFalse(PinHasher.matches("2469", salt, hash, iterations = 1_000))
        assertFalse(PinHasher.matches("2468", PinHasher.newSalt(), hash, iterations = 1_000))
    }
}
