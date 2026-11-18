// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.provider.MediaStore.Files.FileColumns
import org.prauga.files.core.model.FileCategory

internal data class CategorySelection(val selection: String, val arguments: Array<String>) {

    override fun equals(other: Any?): Boolean =
        other is CategorySelection &&
            selection == other.selection &&
            arguments.contentEquals(other.arguments)

    override fun hashCode(): Int = 31 * selection.hashCode() + arguments.contentHashCode()

    companion object {
        private val mediaTypes =
            mapOf(
                FileCategory.Photos to FileColumns.MEDIA_TYPE_IMAGE,
                FileCategory.Videos to FileColumns.MEDIA_TYPE_VIDEO,
                FileCategory.Audio to FileColumns.MEDIA_TYPE_AUDIO,
            )

        fun of(category: FileCategory): CategorySelection {
            val known = "${FileColumns.MIME_TYPE} IS NOT NULL"
            return when (category) {
                FileCategory.Photos,
                FileCategory.Videos,
                FileCategory.Audio ->
                    CategorySelection(
                        "$known AND ${FileColumns.MEDIA_TYPE} = ?",
                        arrayOf(mediaTypes.getValue(category).toString()),
                    )
                FileCategory.Other ->
                    CategorySelection(
                        "$known AND ${FileColumns.MEDIA_TYPE} NOT IN (?, ?, ?)",
                        mediaTypes.values.map { it.toString() }.toTypedArray(),
                    )
            }
        }
    }
}
