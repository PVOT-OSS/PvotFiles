// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.prauga.files.core.model.FileEntry

internal class MediaStoreFileQuery
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val storage: StorageRepository,
) {

    fun newestFirst(
        selection: String,
        arguments: Array<String>,
        limit: Int? = null,
    ): List<FileEntry.File>? {
        val roots = storage.volumes().associate { it.mediaStoreName to it.path }
        if (roots.isEmpty()) return null
        val query =
            Bundle().apply {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arguments)
                putString(
                    ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                    "${FileColumns.DATE_MODIFIED} DESC",
                )
                if (limit != null) putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
            }
        return context.contentResolver
            .query(
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
                arrayOf(
                    FileColumns.DISPLAY_NAME,
                    FileColumns.RELATIVE_PATH,
                    FileColumns.SIZE,
                    FileColumns.DATE_MODIFIED,
                    FileColumns.VOLUME_NAME,
                ),
                query,
                null,
            )
            ?.use { cursor ->
                val name = cursor.getColumnIndexOrThrow(FileColumns.DISPLAY_NAME)
                val relativePath = cursor.getColumnIndexOrThrow(FileColumns.RELATIVE_PATH)
                val size = cursor.getColumnIndexOrThrow(FileColumns.SIZE)
                val modified = cursor.getColumnIndexOrThrow(FileColumns.DATE_MODIFIED)
                val volume = cursor.getColumnIndexOrThrow(FileColumns.VOLUME_NAME)
                buildList {
                    while (cursor.moveToNext()) {
                        val root = roots[cursor.getString(volume)] ?: continue
                        val displayName = cursor.getString(name) ?: continue
                        val folder = cursor.getString(relativePath).orEmpty()
                        val path = MediaStorePath.resolve(root, folder, displayName) ?: continue
                        add(
                            FileEntry.File(
                                path = path,
                                name = displayName,
                                lastModifiedMillis = cursor.getLong(modified) * 1_000,
                                sizeBytes = cursor.getLong(size),
                            )
                        )
                    }
                }
            }
    }
}
