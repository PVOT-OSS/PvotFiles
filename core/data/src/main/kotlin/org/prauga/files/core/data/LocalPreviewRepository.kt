// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
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
constructor(@IoDispatcher private val dispatcher: CoroutineDispatcher) : PreviewRepository {

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
}
