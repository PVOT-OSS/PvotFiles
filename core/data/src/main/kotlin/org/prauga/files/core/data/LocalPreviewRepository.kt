// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileKind
import org.prauga.files.core.model.TextPreview

internal class LocalPreviewRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : PreviewRepository {

    override suspend fun siblingImages(path: String): List<String> =
        withContext(dispatcher) {
            val file = File(path)
            file.parentFile
                ?.listFiles()
                .orEmpty()
                .filter {
                    it.isFile && !it.name.startsWith('.') && FileKind.of(it.name) == FileKind.Image
                }
                .sortedBy { it.name.lowercase() }
                .map { it.path }
                .ifEmpty { listOf(path) }
        }

    override suspend fun image(path: String, maxSidePx: Int): Bitmap? =
        withContext(dispatcher) {
            try {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(File(path))) { decoder, info, _
                    ->
                    val longest = max(info.size.width, info.size.height)
                    if (longest > maxSidePx) {
                        val scale = maxSidePx.toFloat() / longest
                        decoder.setTargetSize(
                            (info.size.width * scale).toInt().coerceAtLeast(1),
                            (info.size.height * scale).toInt().coerceAtLeast(1),
                        )
                    }
                }
            } catch (e: IOException) {
                null
            }
        }

    override suspend fun text(path: String): TextPreview? =
        withContext(dispatcher) {
            try {
                TextReader.read(File(path))
            } catch (e: IOException) {
                null
            }
        }

    override suspend fun openPdf(path: String): PdfPages? =
        withContext(dispatcher) {
            val descriptor =
                try {
                    ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
                } catch (e: IOException) {
                    return@withContext null
                }
            try {
                LocalPdfPages(descriptor, PdfRenderer(descriptor), dispatcher)
            } catch (e: IOException) {
                descriptor.close()
                null
            } catch (e: SecurityException) {
                descriptor.close()
                null
            }
        }

    override suspend fun displayName(uri: String): String =
        withContext(dispatcher) {
            val name =
                try {
                    context.contentResolver
                        .query(uri.toUri(), arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                        ?.use { if (it.moveToFirst()) it.getString(0) else null }
                } catch (e: SecurityException) {
                    null
                } catch (e: IllegalArgumentException) {
                    null
                }
            SafeName.of(name ?: uri.toUri().lastPathSegment)
        }

    override suspend fun copyToCache(uri: String, name: String): String? =
        withContext(dispatcher) {
            val folder =
                File(context.cacheDir, VIEW_FOLDER).apply {
                    deleteRecursively()
                    mkdirs()
                }
            val target = File(folder, SafeName.of(name))
            try {
                val input =
                    context.contentResolver.openInputStream(uri.toUri()) ?: return@withContext null
                input.use { source ->
                    target.outputStream().use { out ->
                        val buffer = ByteArray(BUFFER_BYTES)
                        var total = 0L
                        while (true) {
                            val read = source.read(buffer)
                            if (read < 0) break
                            total += read
                            if (total > MAX_COPY_BYTES) throw IOException("Too large to preview")
                            out.write(buffer, 0, read)
                        }
                    }
                }
                target.path
            } catch (e: IOException) {
                target.delete()
                null
            } catch (e: SecurityException) {
                target.delete()
                null
            }
        }

    private companion object {
        const val VIEW_FOLDER = "view"
        const val BUFFER_BYTES = 64 * 1024
        const val MAX_COPY_BYTES = 100L * 1024 * 1024
    }
}
