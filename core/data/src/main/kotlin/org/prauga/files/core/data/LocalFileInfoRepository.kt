// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.webkit.MimeTypeMap
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileInfo

internal class LocalFileInfoRepository
@Inject
constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) : FileInfoRepository {

    override suspend fun info(path: String): FileInfo? =
        withContext(dispatcher) {
            val file = File(path)
            if (!file.exists()) return@withContext null
            if (file.isDirectory) {
                val totals = FolderSize.of(file)
                return@withContext FileInfo(
                    name = file.name,
                    path = file.path,
                    isDirectory = true,
                    mimeType = null,
                    sizeBytes = totals.bytes,
                    lastModifiedMillis = file.lastModified(),
                    itemCount = totals.items,
                )
            }
            val mime =
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase())
            val base =
                FileInfo(
                    name = file.name,
                    path = file.path,
                    isDirectory = false,
                    mimeType = mime,
                    sizeBytes = file.length(),
                    lastModifiedMillis = file.lastModified(),
                )
            when {
                mime?.startsWith("image/") == true -> withImage(base, file)
                mime?.startsWith("video/") == true || mime?.startsWith("audio/") == true ->
                    withMedia(base, file)
                else -> base
            }
        }

    private fun withImage(base: FileInfo, file: File): FileInfo {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        val exif =
            try {
                ExifInterface(file)
            } catch (e: IOException) {
                null
            }
        val position = exif?.latLong
        return base.copy(
            width = bounds.outWidth.takeIf { it > 0 },
            height = bounds.outHeight.takeIf { it > 0 },
            takenMillis =
                ExifDate.parse(
                    exif?.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                        ?: exif?.getAttribute(ExifInterface.TAG_DATETIME),
                    ZoneId.systemDefault(),
                ),
            latitude = position?.getOrNull(0),
            longitude = position?.getOrNull(1),
        )
    }

    private fun withMedia(base: FileInfo, file: File): FileInfo {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.path)
            base.copy(
                durationMillis =
                    retriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLongOrNull(),
                width =
                    retriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                        ?.toIntOrNull(),
                height =
                    retriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                        ?.toIntOrNull(),
            )
        } catch (e: IllegalArgumentException) {
            base
        } catch (e: RuntimeException) {
            base
        } finally {
            retriever.release()
        }
    }
}
