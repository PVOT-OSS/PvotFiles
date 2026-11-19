// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

data class ArchiveEntry(val path: String, val sizeBytes: Long, val isDirectory: Boolean)
