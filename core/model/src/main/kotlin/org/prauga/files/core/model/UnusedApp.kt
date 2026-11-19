// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

data class UnusedApp(
    val packageName: String,
    val label: String,
    val lastUsedMillis: Long?,
    val sizeBytes: Long,
)
