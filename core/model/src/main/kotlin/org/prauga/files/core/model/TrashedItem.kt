// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

data class TrashedItem(
    val id: String,
    val name: String,
    val originalPath: String,
    val deletedAtMillis: Long,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0,
)
