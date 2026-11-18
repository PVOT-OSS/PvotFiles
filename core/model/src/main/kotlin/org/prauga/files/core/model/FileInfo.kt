// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

data class FileInfo(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val mimeType: String?,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val itemCount: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val durationMillis: Long? = null,
    val takenMillis: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
