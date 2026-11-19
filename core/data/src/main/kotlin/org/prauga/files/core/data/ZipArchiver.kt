// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.prauga.files.core.model.ArchiveEntry

internal object ZipArchiver {
    private const val MAX_ENTRIES = 2_000

    fun compress(sources: List<File>, target: File) {
        ZipOutputStream(target.outputStream().buffered()).use { zip ->
            sources.forEach { source -> add(zip, source, source.name) }
        }
    }

    fun entries(archive: File, limit: Int = MAX_ENTRIES): List<ArchiveEntry> =
        ZipFile(archive).use { zip ->
            zip.entries()
                .asSequence()
                .take(limit)
                .map { ArchiveEntry(it.name, it.size.coerceAtLeast(0), it.isDirectory) }
                .toList()
        }

    fun extract(archive: File, destination: File): List<File> {
        val root = destination.canonicalFile
        val created = mutableListOf<File>()
        ZipInputStream(archive.inputStream().buffered()).use { zip ->
            generateSequence { zip.nextEntry }
                .forEach { entry ->
                    val out = File(root, entry.name).canonicalFile
                    if (out != root && !out.path.startsWith(root.path + File.separator)) {
                        throw IOException("Entry outside the destination: ${entry.name}")
                    }
                    if (entry.isDirectory) {
                        out.mkdirs()
                    } else {
                        out.parentFile?.mkdirs()
                        out.outputStream().buffered().use { zip.copyTo(it) }
                        created += out
                    }
                }
        }
        return created
    }

    private fun add(zip: ZipOutputStream, file: File, entryName: String) {
        if (file.isDirectory) {
            zip.putNextEntry(ZipEntry("$entryName/").apply { time = file.lastModified() })
            zip.closeEntry()
            file
                .listFiles()
                .orEmpty()
                .sortedBy { it.name }
                .forEach { add(zip, it, "$entryName/${it.name}") }
        } else {
            zip.putNextEntry(ZipEntry(entryName).apply { time = file.lastModified() })
            file.inputStream().buffered().use { it.copyTo(zip) }
            zip.closeEntry()
        }
    }
}
