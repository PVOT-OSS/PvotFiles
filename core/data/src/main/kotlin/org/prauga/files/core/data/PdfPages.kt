// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.graphics.Bitmap

interface PdfPages {
    val pageCount: Int

    fun aspectRatio(index: Int): Float

    suspend fun render(index: Int, widthPx: Int): Bitmap?

    fun close()
}
