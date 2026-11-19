// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

internal object SafeName {
    private const val FALLBACK = "file"
    private const val MAX_LENGTH = 120

    fun of(displayName: String?): String {
        val name = displayName.orEmpty().substringAfterLast('/').substringAfterLast('\\').trim()
        val cleaned = name.filter { it >= ' ' && it != ':' }.take(MAX_LENGTH)
        return if (cleaned.isEmpty() || cleaned == "." || cleaned == "..") FALLBACK else cleaned
    }
}
