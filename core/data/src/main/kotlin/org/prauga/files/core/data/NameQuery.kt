// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

internal object NameQuery {
    const val ESCAPE = '\\'

    fun likePattern(query: String): String = buildString {
        append('%')
        query.trim().forEach { char ->
            if (char == '%' || char == '_' || char == ESCAPE) append(ESCAPE)
            append(char)
        }
        append('%')
    }
}
