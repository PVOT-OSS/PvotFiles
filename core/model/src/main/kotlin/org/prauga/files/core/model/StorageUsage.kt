// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

data class StorageUsage(
    val totalBytes: Long,
    val freeBytes: Long,
    val categories: List<CategoryUsage>,
) {
    val usedBytes: Long
        get() = totalBytes - freeBytes
}
