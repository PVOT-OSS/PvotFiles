// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.graphics.Bitmap
import org.prauga.files.core.model.FileEntry

interface ThumbnailRepository {
    fun hasThumbnail(file: FileEntry.File): Boolean

    suspend fun thumbnail(file: FileEntry.File, sizePx: Int): Bitmap?
}
