// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.provider.MediaStore.Files.FileColumns
import org.junit.Assert.assertEquals
import org.junit.Test
import org.prauga.files.core.model.CategoryUsage
import org.prauga.files.core.model.FileCategory

class CategoryTallyTest {
    @Test
    fun addsSizesAndCountsPerCategory() {
        val tally = CategoryTally()

        tally.add(FileColumns.MEDIA_TYPE_IMAGE, "image/jpeg", 10)
        tally.add(FileColumns.MEDIA_TYPE_IMAGE, "image/png", 5)
        tally.add(FileColumns.MEDIA_TYPE_VIDEO, "video/mp4", 100)
        tally.add(FileColumns.MEDIA_TYPE_DOCUMENT, "application/pdf", 7)
        tally.add(FileColumns.MEDIA_TYPE_NONE, CategorySelection.APK_MIME_TYPE, 20)
        tally.add(FileColumns.MEDIA_TYPE_NONE, "application/zip", 3)

        assertEquals(
            listOf(
                CategoryUsage(FileCategory.Photos, 15, 2),
                CategoryUsage(FileCategory.Videos, 100, 1),
                CategoryUsage(FileCategory.Audio, 0, 0),
                CategoryUsage(FileCategory.Documents, 7, 1),
                CategoryUsage(FileCategory.Apps, 20, 1),
                CategoryUsage(FileCategory.Other, 3, 1),
            ),
            tally.usage(),
        )
    }

    @Test
    fun playlistsAndSubtitlesCountAsOtherFiles() {
        assertEquals(
            FileCategory.Other,
            CategoryTally.categoryOf(FileColumns.MEDIA_TYPE_PLAYLIST, null),
        )
        assertEquals(
            FileCategory.Other,
            CategoryTally.categoryOf(FileColumns.MEDIA_TYPE_SUBTITLE, null),
        )
        assertEquals(
            FileCategory.Audio,
            CategoryTally.categoryOf(FileColumns.MEDIA_TYPE_AUDIO, "audio/mpeg"),
        )
    }
}
