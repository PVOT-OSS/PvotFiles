// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StorageUsageTest {
    @Test
    fun otherUsedIsWhatTheCategoriesDoNotCover() {
        val usage =
            StorageUsage(
                totalBytes = 100,
                freeBytes = 40,
                categories =
                    listOf(
                        CategoryUsage(FileCategory.Photos, 20, 1),
                        CategoryUsage(FileCategory.Videos, 10, 1),
                    ),
            )

        assertEquals(60, usage.usedBytes)
        assertEquals(30, usage.otherUsedBytes)
    }

    @Test
    fun otherUsedIsNeverNegative() {
        val usage = StorageUsage(100, 90, listOf(CategoryUsage(FileCategory.Photos, 20, 1)))

        assertEquals(0, usage.otherUsedBytes)
    }
}
