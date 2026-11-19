// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.provider.MediaStore.Files.FileColumns
import java.io.File
import java.time.Clock
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.CleanCategory
import org.prauga.files.core.model.CleanSummary
import org.prauga.files.core.model.FileEntry

@Singleton
internal class LocalCleanRepository
@Inject
constructor(
    private val files: MediaStoreFileQuery,
    private val storage: StorageRepository,
    private val clock: Clock,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : CleanRepository {

    override suspend fun summary(): List<CleanSummary>? =
        withContext(dispatcher) {
            CleanCategory.entries.map { category ->
                val groups = load(category) ?: return@withContext null
                val removable = removable(category, groups)
                CleanSummary(category, removable.size, removable.sumOf { it.sizeOf() })
            }
        }

    override suspend fun groups(category: CleanCategory): List<List<FileEntry>>? =
        withContext(dispatcher) { load(category) }

    private fun load(category: CleanCategory): List<List<FileEntry>>? =
        when (category) {
            CleanCategory.LargeFiles ->
                files
                    .newestFirst("${FileColumns.SIZE} >= ?", arrayOf(LARGE_FILE_BYTES.toString()))
                    ?.sortedByDescending { it.sizeBytes }
                    ?.let(::single)
            CleanCategory.Duplicates ->
                files.newestFirst("${FileColumns.SIZE} > ?", arrayOf("0"))?.let {
                    DuplicateFinder.groups(it, ::cachedHash)
                }
            CleanCategory.OldScreenshots -> olderThan("%/Screenshots/%")?.let(::single)
            CleanCategory.OldDownloads -> olderThan("Download/%")?.let(::single)
            CleanCategory.EmptyFolders ->
                storage.primaryRootPath()?.let { root ->
                    single(
                        EmptyFolders.find(File(root)).map {
                            FileEntry.Directory(it.path, it.name, it.lastModified(), childCount = 0)
                        }
                    )
                }
        }

    private val hashes = ConcurrentHashMap<String, Pair<String, String>>()

    private fun cachedHash(path: String): String? {
        val file = File(path)
        val stamp = "${file.length()}:${file.lastModified()}"
        hashes[path]?.let { (cachedStamp, hash) -> if (cachedStamp == stamp) return hash }
        return DuplicateFinder.sha256(path)?.also { hashes[path] = stamp to it }
    }

    private fun olderThan(relativePath: String): List<FileEntry.File>? {
        val cutoff = clock.instant().minus(OLD_AFTER).epochSecond
        return files.newestFirst(
            "${FileColumns.RELATIVE_PATH} LIKE ? AND ${FileColumns.DATE_MODIFIED} < ?",
            arrayOf(relativePath, cutoff.toString()),
        )
    }

    private fun single(entries: List<FileEntry>): List<List<FileEntry>> =
        if (entries.isEmpty()) emptyList() else listOf(entries)

    private fun FileEntry.sizeOf(): Long = (this as? FileEntry.File)?.sizeBytes ?: 0L

    companion object {
        const val LARGE_FILE_BYTES = 100L * 1024 * 1024
        private val OLD_AFTER: Duration = Duration.ofDays(30)

        fun removable(category: CleanCategory, groups: List<List<FileEntry>>): List<FileEntry> =
            if (category == CleanCategory.Duplicates) groups.flatMap { it.drop(1) }
            else groups.flatten()
    }
}
