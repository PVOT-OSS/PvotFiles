// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.provider.MediaStore.Files.FileColumns
import org.prauga.files.core.model.CategoryUsage
import org.prauga.files.core.model.FileCategory

internal class CategoryTally {
    private val sizes = LongArray(FileCategory.entries.size)
    private val counts = IntArray(FileCategory.entries.size)

    fun add(mediaType: Int, sizeBytes: Long) {
        val index = categoryOf(mediaType).ordinal
        sizes[index] += sizeBytes
        counts[index]++
    }

    fun usage(): List<CategoryUsage> =
        FileCategory.entries.map { CategoryUsage(it, sizes[it.ordinal], counts[it.ordinal]) }

    companion object {
        fun categoryOf(mediaType: Int): FileCategory =
            when (mediaType) {
                FileColumns.MEDIA_TYPE_IMAGE -> FileCategory.Photos
                FileColumns.MEDIA_TYPE_VIDEO -> FileCategory.Videos
                FileColumns.MEDIA_TYPE_AUDIO -> FileCategory.Audio
                else -> FileCategory.Other
            }
    }
}
