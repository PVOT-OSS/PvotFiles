// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.util.LruCache
import android.util.Size
import android.webkit.MimeTypeMap
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
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
constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ThumbnailRepository {
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
                        Kind.Apk -> apkIcon(file.path, sizePx)
                    }
                } catch (e: IOException) {
                    null
                }
                ?.also { cache.put(key, it) }
        }
    }

    private fun apkIcon(path: String, sizePx: Int): Bitmap? {
        val packages = context.packageManager
        val info =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packages.getPackageArchiveInfo(path, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION") packages.getPackageArchiveInfo(path, 0)
            }
        val application = info?.applicationInfo ?: return null
        application.sourceDir = path
        application.publicSourceDir = path
        return application.loadIcon(packages).toBitmap(sizePx, sizePx)
    }

    private fun kindOf(file: FileEntry.File): Kind? {
        val extension = file.name.substringAfterLast('.', "").lowercase()
        if (extension == APK_EXTENSION) return Kind.Apk
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
        Apk,
    }

    private companion object {
        const val CACHE_BYTES = 16 * 1024 * 1024
        const val APK_EXTENSION = "apk"
    }
}
