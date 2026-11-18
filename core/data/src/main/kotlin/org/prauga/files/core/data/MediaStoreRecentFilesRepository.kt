// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.provider.MediaStore.Files.FileColumns
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileEntry

internal class MediaStoreRecentFilesRepository
@Inject
constructor(
    private val files: MediaStoreFileQuery,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : RecentFilesRepository {

    override suspend fun recent(limit: Int): List<FileEntry.File>? =
        withContext(dispatcher) {
            files.newestFirst("${FileColumns.MIME_TYPE} IS NOT NULL", emptyArray(), limit)
        }
}
