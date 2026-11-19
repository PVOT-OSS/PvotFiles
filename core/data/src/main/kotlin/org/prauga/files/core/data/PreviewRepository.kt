// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.graphics.Bitmap
import org.prauga.files.core.model.TextPreview

interface PreviewRepository {
    suspend fun siblingImages(path: String): List<String>

    suspend fun image(path: String, maxSidePx: Int): Bitmap?

    suspend fun text(path: String): TextPreview?

    suspend fun openPdf(path: String): PdfPages?
}
