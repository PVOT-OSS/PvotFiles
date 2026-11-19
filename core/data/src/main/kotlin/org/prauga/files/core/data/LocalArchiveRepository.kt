// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.ArchiveEntry
import org.prauga.files.core.model.FileOperationResult

internal class LocalArchiveRepository
@Inject
constructor(
    private val mediaIndex: MediaIndex,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ArchiveRepository {

    override suspend fun compress(paths: List<String>, folder: String): FileOperationResult =
        withContext(dispatcher) {
            val sources = paths.map(::File).filter { it.exists() }
            val parent = File(folder)
            if (sources.isEmpty() || !parent.isDirectory)
                return@withContext FileOperationResult.Failed
            val base = sources.singleOrNull()?.let { it.nameWithoutExtensionFor() } ?: ARCHIVE_NAME
            val target =
                File(parent, TransferEngine.uniqueName(parent, "$base.zip", isDirectory = false))
            try {
                ZipArchiver.compress(sources, target)
            } catch (e: IOException) {
                target.delete()
                return@withContext FileOperationResult.Failed
            }
            mediaIndex.update(listOf(target.path))
            FileOperationResult.Done
        }

    override suspend fun extract(path: String): FileOperationResult {
        val parent = File(path).parentFile ?: return FileOperationResult.Failed
        return extractTo(path, parent.path)
    }

    override suspend fun extractTo(path: String, folder: String): FileOperationResult =
        withContext(dispatcher) {
            val archive = File(path)
            val parent = File(folder)
            if (!archive.isFile || !(parent.isDirectory || parent.mkdirs()))
                return@withContext FileOperationResult.Failed
            val destination =
                File(
                    parent,
                    TransferEngine.uniqueName(
                        parent,
                        archive.nameWithoutExtension,
                        isDirectory = true,
                    ),
                )
            val created =
                try {
                    ZipArchiver.extract(archive, destination)
                } catch (e: IOException) {
                    destination.deleteRecursively()
                    return@withContext FileOperationResult.Failed
                } catch (e: IllegalArgumentException) {
                    destination.deleteRecursively()
                    return@withContext FileOperationResult.Failed
                }
            destination.mkdirs()
            mediaIndex.update(created.map { it.path })
            FileOperationResult.Done
        }

    override suspend fun entries(path: String): List<ArchiveEntry>? =
        withContext(dispatcher) {
            try {
                ZipArchiver.entries(File(path))
            } catch (e: IOException) {
                null
            }
        }

    private fun File.nameWithoutExtensionFor(): String =
        if (isDirectory) name else nameWithoutExtension

    private companion object {
        const val ARCHIVE_NAME = "Archive"
    }
}
