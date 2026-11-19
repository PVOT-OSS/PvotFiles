// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

internal object PinPolicy {
    const val FREE_ATTEMPTS = 5
    const val MIN_LENGTH = 4
    private const val FIRST_LOCKOUT_MILLIS = 30_000L
    private const val MAX_LOCKOUT_MILLIS = 60 * 60 * 1000L

    fun isAcceptable(pin: String): Boolean = pin.length >= MIN_LENGTH && pin.all { it.isDigit() }

    fun lockoutMillis(failures: Int): Long {
        if (failures < FREE_ATTEMPTS) return 0L
        val doublings = (failures - FREE_ATTEMPTS).coerceAtMost(MAX_DOUBLINGS)
        return (FIRST_LOCKOUT_MILLIS shl doublings).coerceAtMost(MAX_LOCKOUT_MILLIS)
    }

    private const val MAX_DOUBLINGS = 10
}
