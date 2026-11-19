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
        const val APK_MIME_TYPE = "application/vnd.android.package-archive"

        private val mediaTypes =
            mapOf(
                FileCategory.Photos to FileColumns.MEDIA_TYPE_IMAGE,
                FileCategory.Videos to FileColumns.MEDIA_TYPE_VIDEO,
                FileCategory.Audio to FileColumns.MEDIA_TYPE_AUDIO,
                FileCategory.Documents to FileColumns.MEDIA_TYPE_DOCUMENT,
            )

        fun of(category: FileCategory): CategorySelection {
            val known = "${FileColumns.MIME_TYPE} IS NOT NULL"
            return when (category) {
                FileCategory.Photos,
                FileCategory.Videos,
                FileCategory.Audio,
                FileCategory.Documents ->
                    CategorySelection(
                        "$known AND ${FileColumns.MEDIA_TYPE} = ?",
                        arrayOf(mediaTypes.getValue(category).toString()),
                    )
                FileCategory.Apps ->
                    CategorySelection("${FileColumns.MIME_TYPE} = ?", arrayOf(APK_MIME_TYPE))
                FileCategory.Other ->
                    CategorySelection(
                        "$known AND ${FileColumns.MEDIA_TYPE} NOT IN (?, ?, ?, ?) AND ${FileColumns.MIME_TYPE} != ?",
                        (mediaTypes.values.map { it.toString() } + APK_MIME_TYPE).toTypedArray(),
                    )
            }
        }
    }
}
