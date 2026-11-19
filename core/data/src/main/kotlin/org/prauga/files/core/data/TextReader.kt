// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import org.prauga.files.core.model.TextPreview

internal object TextReader {
    const val MAX_BYTES = 256 * 1024

    fun read(file: File, maxBytes: Int = MAX_BYTES): TextPreview {
        val bytes =
            file.inputStream().use { input ->
                val buffer = ByteArray(maxBytes + 1)
                var filled = 0
                while (filled < buffer.size) {
                    val read = input.read(buffer, filled, buffer.size - filled)
                    if (read < 0) break
                    filled += read
                }
                buffer.copyOf(filled)
            }
        val truncated = bytes.size > maxBytes
        val kept = if (truncated) bytes.copyOf(maxBytes) else bytes
        return TextPreview(String(kept, Charsets.UTF_8).trimEnd('�'), truncated)
    }
}
