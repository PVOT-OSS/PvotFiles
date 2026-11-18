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

        tally.add(FileColumns.MEDIA_TYPE_IMAGE, 10)
        tally.add(FileColumns.MEDIA_TYPE_IMAGE, 5)
        tally.add(FileColumns.MEDIA_TYPE_VIDEO, 100)
        tally.add(FileColumns.MEDIA_TYPE_DOCUMENT, 7)
        tally.add(FileColumns.MEDIA_TYPE_NONE, 3)

        assertEquals(
            listOf(
                CategoryUsage(FileCategory.Other, 10, 2),
                CategoryUsage(FileCategory.Videos, 100, 1),
                CategoryUsage(FileCategory.Photos, 15, 2),
                CategoryUsage(FileCategory.Audio, 0, 0),
            ),
            tally.usage(),
        )
    }

    @Test
    fun playlistsAndSubtitlesCountAsOtherFiles() {
        assertEquals(FileCategory.Other, CategoryTally.categoryOf(FileColumns.MEDIA_TYPE_PLAYLIST))
        assertEquals(FileCategory.Other, CategoryTally.categoryOf(FileColumns.MEDIA_TYPE_SUBTITLE))
        assertEquals(FileCategory.Audio, CategoryTally.categoryOf(FileColumns.MEDIA_TYPE_AUDIO))
    }
}
