// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class UnusedAppsTest {
    private val day = 24L * 60 * 60 * 1000
    private val now = 100 * day

    @Test
    fun appsNotUsedForAMonthOrNeverAreUnused() {
        val installed =
            listOf(
                UnusedApps.Installed("old.used", 0),
                UnusedApps.Installed("old.never", 0),
                UnusedApps.Installed("recent.used", 0),
                UnusedApps.Installed("just.installed", now - day),
            )
        val lastUsed = mapOf("old.used" to now - 40 * day, "recent.used" to now - 2 * day)

        assertEquals(
            mapOf("old.used" to now - 40 * day, "old.never" to null),
            UnusedApps.select(installed, lastUsed, now, 30 * day),
        )
    }
}
