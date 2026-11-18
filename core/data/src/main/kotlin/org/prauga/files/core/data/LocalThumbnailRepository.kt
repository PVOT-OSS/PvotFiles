// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.util.LruCache
import android.util.Size
import android.webkit.MimeTypeMap
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileEntry

@Singleton
internal class LocalThumbnailRepository
@Inject
constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) : ThumbnailRepository {
    private val cache =
        object : LruCache<String, Bitmap>(CACHE_BYTES) {
            override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
        }

    override fun hasThumbnail(file: FileEntry.File): Boolean = kindOf(file) != null

    override suspend fun thumbnail(file: FileEntry.File, sizePx: Int): Bitmap? {
        val kind = kindOf(file) ?: return null
        val key = "${file.path}|${file.lastModifiedMillis}|$sizePx"
        cache.get(key)?.let {
            return it
        }
        return withContext(dispatcher) {
            val size = Size(sizePx, sizePx)
            try {
                    when (kind) {
                        Kind.Image ->
                            ThumbnailUtils.createImageThumbnail(File(file.path), size, null)
                        Kind.Video ->
                            ThumbnailUtils.createVideoThumbnail(File(file.path), size, null)
                    }
                } catch (e: IOException) {
                    null
                }
                ?.also { cache.put(key, it) }
        }
    }

    private fun kindOf(file: FileEntry.File): Kind? {
        val extension = file.name.substringAfterLast('.', "").lowercase()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: return null
        return when {
            mime.startsWith("image/") -> Kind.Image
            mime.startsWith("video/") -> Kind.Video
            else -> null
        }
    }

    private enum class Kind {
        Image,
        Video,
    }

    private companion object {
        const val CACHE_BYTES = 16 * 1024 * 1024
    }
}
