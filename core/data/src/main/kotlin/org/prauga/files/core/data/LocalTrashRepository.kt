// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.TrashedItem

internal class LocalTrashRepository
@Inject
constructor(
    private val storage: StorageRepository,
    private val mediaIndex: MediaIndex,
    private val clock: Clock,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : TrashRepository {

    override suspend fun moveToTrash(path: String): String? =
        withContext(dispatcher) {
            val id = trash()?.moveIn(path, clock.millis()) ?: return@withContext null
            mediaIndex.update(listOf(path))
            id
        }

    override suspend fun items(): List<TrashedItem>? =
        withContext(dispatcher) {
            val trash = trash() ?: return@withContext null
            trash.deleteExpired(clock.millis(), KEEP_MILLIS)
            trash.list()
        }

    override suspend fun restore(id: String): FileOperationResult =
        withContext(dispatcher) {
            val trash = trash() ?: return@withContext FileOperationResult.Failed
            val item =
                trash.list().firstOrNull { it.id == id }
                    ?: return@withContext FileOperationResult.Failed
            trash.restore(id).also {
                if (it == FileOperationResult.Done) mediaIndex.update(listOf(item.originalPath))
            }
        }

    override suspend fun deleteForever(id: String): FileOperationResult =
        withContext(dispatcher) { trash()?.deleteForever(id) ?: FileOperationResult.Failed }

    override fun isTrash(path: String): Boolean =
        storage.primaryRootPath()?.let { path == File(it, TRASH_FOLDER).path } ?: false

    private fun trash(): TrashFolder? =
        storage.primaryRootPath()?.let { TrashFolder(File(it, TRASH_FOLDER)) }

    companion object {
        const val TRASH_FOLDER = ".trash-files"
        val KEEP_MILLIS = TimeUnit.DAYS.toMillis(30)
    }
}
