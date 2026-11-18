// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import java.io.IOException
import org.prauga.files.core.model.TransferKind
import org.prauga.files.core.model.TransferProgress
import org.prauga.files.core.model.TransferResult

internal class TransferEngine(private val mediaIndex: MediaIndex) {

    fun run(
        sources: List<String>,
        destination: String,
        kind: TransferKind,
        onProgress: (TransferProgress) -> Unit,
        isCancelled: () -> Boolean,
    ): TransferResult {
        val target = File(destination)
        if (!target.isDirectory) return TransferResult.Failed
        val items = sources.map(::File)
        if (items.any { !it.exists() }) return TransferResult.Failed
        if (items.any { target.isInside(it) }) return TransferResult.IntoItself
        val work = items.filterNot { kind == TransferKind.Move && it.parentFile == target }
        val total = work.sumOf { it.totalBytes() }
        val progress = Progress(total, onProgress)
        val changed = mutableListOf<String>()
        for (item in work) {
            val copy = File(target, uniqueName(target, item.name, item.isDirectory))
            val result =
                if (kind == TransferKind.Move && item.renameTo(copy)) {
                    progress.add(item.totalBytes(), item.name)
                    true
                } else {
                    copyTree(item, copy, progress, isCancelled) &&
                        (kind == TransferKind.Copy || item.deleteRecursively())
                }
            if (!result) {
                mediaIndex.update(changed)
                return if (isCancelled()) TransferResult.Cancelled else TransferResult.Failed
            }
            changed += copy.path
            if (kind == TransferKind.Move) changed += item.path
        }
        mediaIndex.update(changed)
        return TransferResult.Done(work.size)
    }

    private fun copyTree(
        source: File,
        target: File,
        progress: Progress,
        isCancelled: () -> Boolean,
    ): Boolean {
        if (source.isDirectory) {
            if (!target.mkdir()) return false
            val children = source.listFiles() ?: return false
            return children.all { copyTree(it, File(target, it.name), progress, isCancelled) }
        }
        return try {
            source.inputStream().use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(BUFFER_BYTES)
                    while (true) {
                        if (isCancelled()) throw IOException("cancelled")
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        progress.add(read.toLong(), source.name)
                    }
                }
            }
            target.setLastModified(source.lastModified())
            true
        } catch (e: IOException) {
            target.delete()
            false
        }
    }

    private class Progress(
        private val total: Long,
        private val onProgress: (TransferProgress) -> Unit,
    ) {
        private var done = 0L

        fun add(bytes: Long, name: String) {
            done += bytes
            onProgress(TransferProgress(done, total, name))
        }
    }

    companion object {
        private const val BUFFER_BYTES = 64 * 1024

        fun uniqueName(folder: File, name: String, isDirectory: Boolean): String {
            if (!File(folder, name).exists()) return name
            val dot = if (isDirectory) -1 else name.lastIndexOf('.').takeIf { it > 0 } ?: -1
            val base = if (dot < 0) name else name.substring(0, dot)
            val extension = if (dot < 0) "" else name.substring(dot)
            return generateSequence(1) { it + 1 }
                .map { "$base ($it)$extension" }
                .first { !File(folder, it).exists() }
        }

        private fun File.isInside(other: File): Boolean {
            val self = canonicalPath
            val parent = other.canonicalPath
            return self == parent || self.startsWith(parent + File.separator)
        }

        private fun File.totalBytes(): Long =
            if (isDirectory) listFiles().orEmpty().sumOf { it.totalBytes() } else length()
    }
}
