// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.provider.MediaStore.Files.FileColumns
import org.junit.Assert.assertEquals
import org.junit.Test
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.ModifiedWithin
import org.prauga.files.core.model.SearchFilters
import org.prauga.files.core.model.SizeRange

class SearchSelectionTest {
    @Test
    fun aPlainQueryMatchesNamesOfKnownFiles() {
        assertEquals(
            CategorySelection(
                "mime_type IS NOT NULL AND _display_name LIKE ? ESCAPE '\\'",
                arrayOf("%trip%"),
            ),
            SearchSelection.of("trip", SearchFilters(), nowSeconds = 0),
        )
    }

    @Test
    fun filtersAddTheCategoryDateAndSize() {
        val selection =
            SearchSelection.of(
                "",
                SearchFilters(FileCategory.Photos, ModifiedWithin.Week, SizeRange.Medium),
                nowSeconds = 1_000_000,
            )

        assertEquals(
            "(mime_type IS NOT NULL AND media_type = ?) AND date_modified >= ? AND _size >= ? AND _size < ?",
            selection.selection,
        )
        assertEquals(
            listOf(
                FileColumns.MEDIA_TYPE_IMAGE.toString(),
                (1_000_000 - 7 * 86_400).toString(),
                (1024L * 1024).toString(),
                (100L * 1024 * 1024).toString(),
            ),
            selection.arguments.toList(),
        )
    }
}
