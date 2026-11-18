// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.DirectoryListing
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.SortOrder
import org.prauga.files.core.model.sortedFor

internal class LocalDirectoryRepository
@Inject
constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) : DirectoryRepository {

    override suspend fun list(path: String, includeHidden: Boolean): DirectoryListing =
        withContext(dispatcher) {
            val directory = File(path)
            if (!directory.isDirectory) return@withContext DirectoryListing.NotFound
            val children = directory.listFiles() ?: return@withContext DirectoryListing.Unreadable
            DirectoryListing.Entries(
                children
                    .filter { includeHidden || !it.isHidden }
                    .map { it.toEntry(includeHidden) }
                    .sortedFor(SortOrder.Name)
            )
        }

    private fun File.toEntry(includeHidden: Boolean): FileEntry =
        if (isDirectory) {
            FileEntry.Directory(
                path = path,
                name = name,
                lastModifiedMillis = lastModified(),
                childCount = listFiles()?.count { includeHidden || !it.isHidden } ?: 0,
            )
        } else {
            FileEntry.File(
                path = path,
                name = name,
                lastModifiedMillis = lastModified(),
                sizeBytes = length(),
            )
        }
}
