// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import java.io.IOException
import java.security.MessageDigest
import org.prauga.files.core.model.FileEntry

internal object DuplicateFinder {

    fun groups(files: List<FileEntry.File>, hash: (String) -> String?): List<List<FileEntry.File>> =
        files
            .filter { it.sizeBytes > 0 }
            .groupBy { it.sizeBytes }
            .values
            .filter { it.size > 1 }
            .flatMap { sameSize ->
                sameSize
                    .mapNotNull { file -> hash(file.path)?.let { it to file } }
                    .groupBy({ it.first }, { it.second })
                    .values
                    .filter { it.size > 1 }
            }
            .map { group -> group.sortedBy { it.lastModifiedMillis } }
            .sortedByDescending { group -> group.first().sizeBytes * (group.size - 1) }

    fun sha256(path: String): String? =
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            File(path).inputStream().buffered().use { input ->
                val buffer = ByteArray(BUFFER_BYTES)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    digest.update(buffer, 0, read)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: IOException) {
            null
        }

    private const val BUFFER_BYTES = 64 * 1024
}
