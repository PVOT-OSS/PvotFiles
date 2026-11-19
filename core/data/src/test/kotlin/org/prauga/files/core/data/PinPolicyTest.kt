// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinPolicyTest {
    @Test
    fun pinsAreAtLeastFourDigits() {
        assertTrue(PinPolicy.isAcceptable("0000"))
        assertFalse(PinPolicy.isAcceptable("123"))
        assertFalse(PinPolicy.isAcceptable("12a4"))
    }

    @Test
    fun lockoutStartsAfterFiveFailuresAndDoublesUpToAnHour() {
        assertEquals(0L, PinPolicy.lockoutMillis(4))
        assertEquals(30_000L, PinPolicy.lockoutMillis(5))
        assertEquals(60_000L, PinPolicy.lockoutMillis(6))
        assertEquals(60 * 60 * 1000L, PinPolicy.lockoutMillis(40))
    }
}
