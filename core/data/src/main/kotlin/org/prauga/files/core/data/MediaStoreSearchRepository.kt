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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileEntry

internal class MediaStoreSearchRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val storage: StorageRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : SearchRepository {

    override suspend fun search(query: String): List<FileEntry.File>? =
        withContext(dispatcher) {
            val root = storage.primaryRootPath() ?: return@withContext null
            val arguments =
                Bundle().apply {
                    putString(
                        ContentResolver.QUERY_ARG_SQL_SELECTION,
                        "${FileColumns.MIME_TYPE} IS NOT NULL AND ${FileColumns.DISPLAY_NAME} LIKE ? ESCAPE '${NameQuery.ESCAPE}'",
                    )
                    putStringArray(
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                        arrayOf(NameQuery.likePattern(query)),
                    )
                    putString(
                        ContentResolver.QUERY_ARG_SQL_SORT_ORDER,
                        "${FileColumns.DATE_MODIFIED} DESC",
                    )
                    putInt(ContentResolver.QUERY_ARG_LIMIT, LIMIT)
                }
            context.contentResolver
                .query(
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    arrayOf(
                        FileColumns.DISPLAY_NAME,
                        FileColumns.RELATIVE_PATH,
                        FileColumns.SIZE,
                        FileColumns.DATE_MODIFIED,
                    ),
                    arguments,
                    null,
                )
                ?.use { cursor ->
                    val name = cursor.getColumnIndexOrThrow(FileColumns.DISPLAY_NAME)
                    val relativePath = cursor.getColumnIndexOrThrow(FileColumns.RELATIVE_PATH)
                    val size = cursor.getColumnIndexOrThrow(FileColumns.SIZE)
                    val modified = cursor.getColumnIndexOrThrow(FileColumns.DATE_MODIFIED)
                    buildList {
                        while (cursor.moveToNext()) {
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

    private companion object {
        const val LIMIT = 200
    }
}
