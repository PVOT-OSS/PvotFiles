// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.StorageUsage

internal class MediaStoreStorageUsageRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : StorageUsageRepository {

    override suspend fun measure(): StorageUsage? =
        withContext(dispatcher) {
            val stats = context.getSystemService(StorageStatsManager::class.java)
            val totalBytes: Long
            val freeBytes: Long
            try {
                totalBytes = stats.getTotalBytes(StorageManager.UUID_DEFAULT)
                freeBytes = stats.getFreeBytes(StorageManager.UUID_DEFAULT)
            } catch (e: IOException) {
                return@withContext null
            }
            val tally = CategoryTally()
            context.contentResolver
                .query(
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    arrayOf(FileColumns.MEDIA_TYPE, FileColumns.SIZE),
                    "${FileColumns.MIME_TYPE} IS NOT NULL",
                    null,
                    null,
                )
                ?.use { cursor ->
                    val mediaType = cursor.getColumnIndexOrThrow(FileColumns.MEDIA_TYPE)
                    val size = cursor.getColumnIndexOrThrow(FileColumns.SIZE)
                    while (cursor.moveToNext()) {
                        tally.add(cursor.getInt(mediaType), cursor.getLong(size))
                    }
                } ?: return@withContext null
            StorageUsage(totalBytes, freeBytes, tally.usage())
        }
}
