// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

object FileNames {
    private const val MAX_NAME_BYTES = 255

    fun isValid(name: String): Boolean =
        name.isNotBlank() &&
            name == name.trim() &&
            name != "." &&
            name != ".." &&
            '/' !in name &&
            '\u0000' !in name &&
            name.toByteArray(Charsets.UTF_8).size <= MAX_NAME_BYTES
}
