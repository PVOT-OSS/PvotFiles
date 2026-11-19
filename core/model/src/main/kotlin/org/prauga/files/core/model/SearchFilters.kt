// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

data class SearchFilters(
    val category: FileCategory? = null,
    val modified: ModifiedWithin = ModifiedWithin.Any,
    val size: SizeRange = SizeRange.Any,
    val folder: String? = null,
) {
    val isDefault: Boolean
        get() = this == SearchFilters()
}

enum class ModifiedWithin(val days: Long?) {
    Any(null),
    Week(7),
    Month(30),
    Year(365),
}

enum class SizeRange(val minBytes: Long?, val maxBytes: Long?) {
    Any(null, null),
    Small(null, 1024L * 1024),
    Medium(1024L * 1024, 100L * 1024 * 1024),
    Large(100L * 1024 * 1024, null),
}
