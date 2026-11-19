// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

internal object UnusedApps {
    data class Installed(val packageName: String, val installedMillis: Long)

    fun select(
        installed: List<Installed>,
        lastUsed: Map<String, Long>,
        nowMillis: Long,
        unusedForMillis: Long,
    ): Map<String, Long?> {
        val cutoff = nowMillis - unusedForMillis
        return installed
            .filter { it.installedMillis < cutoff }
            .mapNotNull { app ->
                val used = lastUsed[app.packageName]?.takeIf { it > 0 }
                if (used == null || used < cutoff) app.packageName to used else null
            }
            .toMap()
    }
}
