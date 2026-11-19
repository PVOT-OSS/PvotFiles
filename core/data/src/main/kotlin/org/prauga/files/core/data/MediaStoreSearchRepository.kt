// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.SearchFilters
import org.prauga.files.core.model.volumeOf

internal class MediaStoreSearchRepository
@Inject
constructor(
    private val files: MediaStoreFileQuery,
    private val clock: Clock,
    private val storage: StorageRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : SearchRepository {

    override suspend fun search(query: String, filters: SearchFilters): List<FileEntry.File>? =
        withContext(dispatcher) {
            val scope =
                filters.folder?.let { folder ->
                    val volume =
                        storage.volumes().volumeOf(folder) ?: return@withContext emptyList()
                    SearchSelection.scopeOf(folder, volume.path, volume.mediaStoreName)
                }
            val selection = SearchSelection.of(query, filters, clock.instant().epochSecond, scope)
            files.newestFirst(selection.selection, selection.arguments, LIMIT)
        }

    private companion object {
        const val LIMIT = 200
    }
}
