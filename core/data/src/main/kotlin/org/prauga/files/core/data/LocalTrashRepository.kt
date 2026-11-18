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
import org.prauga.files.core.model.volumeOf

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
            val volume = storage.volumes().volumeOf(path) ?: return@withContext null
            val id = trashOn(volume.path).moveIn(path, clock.millis()) ?: return@withContext null
            mediaIndex.update(listOf(path))
            id
        }

    override suspend fun items(): List<TrashedItem>? =
        withContext(dispatcher) {
            val trashes =
                trashes().ifEmpty {
                    return@withContext null
                }
            trashes
                .flatMap { trash ->
                    trash.deleteExpired(clock.millis(), KEEP_MILLIS)
                    trash.list()
                }
                .sortedByDescending { it.deletedAtMillis }
        }

    override suspend fun restore(id: String): FileOperationResult =
        withContext(dispatcher) {
            val (trash, item) = find(id) ?: return@withContext FileOperationResult.Failed
            trash.restore(id).also {
                if (it == FileOperationResult.Done) mediaIndex.update(listOf(item.originalPath))
            }
        }

    override suspend fun deleteForever(id: String): FileOperationResult =
        withContext(dispatcher) { find(id)?.first?.deleteForever(id) ?: FileOperationResult.Failed }

    override fun isTrash(path: String): Boolean =
        storage.volumes().any { path == File(it.path, TRASH_FOLDER).path }

    private fun find(id: String): Pair<TrashFolder, TrashedItem>? =
        trashes().firstNotNullOfOrNull { trash ->
            trash.list().firstOrNull { it.id == id }?.let { trash to it }
        }

    private fun trashes(): List<TrashFolder> = storage.volumes().map { trashOn(it.path) }

    private fun trashOn(root: String) = TrashFolder(File(root, TRASH_FOLDER))

    companion object {
        const val TRASH_FOLDER = ".trash-files"
        val KEEP_MILLIS = TimeUnit.DAYS.toMillis(30)
    }
}
