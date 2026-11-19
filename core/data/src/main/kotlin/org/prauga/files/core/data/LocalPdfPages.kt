// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class LocalPdfPages(
    private val descriptor: ParcelFileDescriptor,
    private val renderer: PdfRenderer,
    private val dispatcher: CoroutineDispatcher,
) : PdfPages {
    private val lock = Mutex()
    private val ratios: List<Float> =
        List(renderer.pageCount) { index ->
            renderer.openPage(index).use { page ->
                page.height.toFloat() / page.width.coerceAtLeast(1)
            }
        }
    private var closed = false

    override val pageCount: Int = renderer.pageCount

    override fun aspectRatio(index: Int): Float = ratios.getOrElse(index) { 1f }

    override suspend fun render(index: Int, widthPx: Int): Bitmap? =
        withContext(dispatcher) {
            lock.withLock {
                if (closed || index !in 0 until pageCount || widthPx <= 0) return@withLock null
                val width = widthPx.coerceAtMost(MAX_WIDTH_PX)
                val height = (width * aspectRatio(index)).toInt().coerceAtLeast(1)
                val bitmap = createBitmap(width, height)
                bitmap.eraseColor(Color.WHITE)
                renderer.openPage(index).use { page ->
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                }
                bitmap
            }
        }

    override fun close() {
        if (lock.tryLock()) {
            try {
                closeNow()
            } finally {
                lock.unlock()
            }
        } else {
            closeNow()
        }
    }

    private fun closeNow() {
        if (closed) return
        closed = true
        renderer.close()
        descriptor.close()
    }

    private companion object {
        const val MAX_WIDTH_PX = 2048
    }
}
