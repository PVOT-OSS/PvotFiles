// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.net.URLDecoder
import java.net.URLEncoder
import org.prauga.files.core.model.SafeItem

internal object SafeIndexCodec {
    private const val FIELD = '\t'
    private const val CHARSET = "UTF-8"

    fun encode(items: List<SafeItem>): String =
        items.joinToString("\n") { item ->
            listOf(item.id, URLEncoder.encode(item.name, CHARSET), item.sizeBytes, item.addedMillis)
                .joinToString(FIELD.toString())
        }

    fun decode(text: String): List<SafeItem> =
        text
            .lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val fields = line.split(FIELD)
                if (fields.size != 4) return@mapNotNull null
                val size = fields[2].toLongOrNull() ?: return@mapNotNull null
                val added = fields[3].toLongOrNull() ?: return@mapNotNull null
                SafeItem(fields[0], URLDecoder.decode(fields[1], CHARSET), size, added)
            }
            .toList()
}
