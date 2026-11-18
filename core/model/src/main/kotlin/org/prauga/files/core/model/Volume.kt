// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

import java.io.File

data class Volume(
    val path: String,
    val label: String,
    val primary: Boolean,
    val mediaStoreName: String,
) {
    fun contains(other: String): Boolean = other == path || other.startsWith(path + File.separator)
}

fun List<Volume>.volumeOf(path: String): Volume? =
    filter { it.contains(path) }.maxByOrNull { it.path.length }
