// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.provider.MediaStore.Files.FileColumns
import org.junit.Assert.assertEquals
import org.junit.Test
import org.prauga.files.core.model.FileCategory

class CategorySelectionTest {
    @Test
    fun mediaCategoriesSelectTheirMediaType() {
        assertEquals(
            CategorySelection(
                "mime_type IS NOT NULL AND media_type = ?",
                arrayOf(FileColumns.MEDIA_TYPE_IMAGE.toString()),
            ),
            CategorySelection.of(FileCategory.Photos),
        )
    }

    @Test
    fun otherFilesExcludeEveryMediaType() {
        assertEquals(
            CategorySelection(
                "mime_type IS NOT NULL AND media_type NOT IN (?, ?, ?)",
                arrayOf(
                    FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    FileColumns.MEDIA_TYPE_VIDEO.toString(),
                    FileColumns.MEDIA_TYPE_AUDIO.toString(),
                ),
            ),
            CategorySelection.of(FileCategory.Other),
        )
    }
}
