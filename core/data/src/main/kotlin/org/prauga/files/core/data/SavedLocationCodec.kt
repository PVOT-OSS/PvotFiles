// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.prauga.files.core.model.SavedLocation

internal object SavedLocationCodec {
    private const val SEPARATOR = '\n'

    fun encode(location: SavedLocation): String = location.uri + SEPARATOR + location.label

    fun decode(value: String): SavedLocation? {
        val uri = value.substringBefore(SEPARATOR, "").takeIf { it.isNotEmpty() } ?: return null
        return SavedLocation(uri, value.substringAfter(SEPARATOR))
    }
}
