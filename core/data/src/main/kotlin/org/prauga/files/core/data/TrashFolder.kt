// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.UUID
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.TrashedItem

internal class TrashFolder(private val root: File) {
    private val files = File(root, "files")
    private val info = File(root, "info")

    fun contains(path: String): Boolean = File(path).canonicalPath.startsWith(root.canonicalPath)

    fun moveIn(path: String, nowMillis: Long): String? {
        val source = File(path)
        if (!source.exists() || contains(path)) return null
        if (!files.isDirectory && !files.mkdirs()) return null
        if (!info.isDirectory && !info.mkdirs()) return null
        val noMedia = File(root, ".nomedia")
        if (!noMedia.exists() && !noMedia.createNewFile()) return null
        val id = UUID.randomUUID().toString()
        val record = infoFile(id)
        record.writeText("$PATH=${URLEncoder.encode(source.path, UTF_8)}\n$DELETED=$nowMillis\n")
        if (!source.renameTo(File(files, id))) {
            record.delete()
            return null
        }
        return id
    }

    fun list(): List<TrashedItem> =
        info
            .listFiles { file -> file.name.endsWith(INFO_SUFFIX) }
            .orEmpty()
            .mapNotNull { read(it.name.removeSuffix(INFO_SUFFIX)) }
            .sortedByDescending { it.deletedAtMillis }

    fun restore(id: String): FileOperationResult {
        val item = read(id) ?: return FileOperationResult.Failed
        val target = File(item.originalPath)
        if (target.exists()) return FileOperationResult.AlreadyExists
        val parent = target.parentFile ?: return FileOperationResult.Failed
        if (!parent.isDirectory && !parent.mkdirs()) return FileOperationResult.Failed
        if (!File(files, id).renameTo(target)) return FileOperationResult.Failed
        infoFile(id).delete()
        return FileOperationResult.Done
    }

    fun deleteForever(id: String): FileOperationResult {
        if (read(id) == null) return FileOperationResult.Failed
        val item = File(files, id)
        if (item.exists() && !item.deleteRecursively()) return FileOperationResult.Failed
        return if (infoFile(id).delete()) FileOperationResult.Done else FileOperationResult.Failed
    }

    fun deleteExpired(nowMillis: Long, keepMillis: Long): List<TrashedItem> =
        list()
            .filter { nowMillis - it.deletedAtMillis > keepMillis }
            .filter { deleteForever(it.id) == FileOperationResult.Done }

    private fun read(id: String): TrashedItem? {
        if (!isSafeId(id)) return null
        val record = infoFile(id).takeIf { it.isFile } ?: return null
        val fields =
            record
                .readLines()
                .mapNotNull { line -> line.split('=', limit = 2).takeIf { it.size == 2 } }
                .associate { it[0] to it[1] }
        val path = fields[PATH]?.let { URLDecoder.decode(it, UTF_8) } ?: return null
        val deleted = fields[DELETED]?.toLongOrNull() ?: return null
        val item = File(files, id)
        if (!item.exists()) return null
        return TrashedItem(id, File(path).name, path, deleted, item.isDirectory)
    }

    private fun infoFile(id: String) = File(info, id + INFO_SUFFIX)

    private fun isSafeId(id: String) =
        id.isNotEmpty() && id.all { it.isLetterOrDigit() || it == '-' }

    private companion object {
        const val INFO_SUFFIX = ".trashinfo"
        const val UTF_8 = "UTF-8"
        const val PATH = "Path"
        const val DELETED = "DeletionMillis"
    }
}
