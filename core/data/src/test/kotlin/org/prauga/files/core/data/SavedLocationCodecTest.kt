// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.prauga.files.core.model.SavedLocation

class SavedLocationCodecTest {
    @Test
    fun roundTripsALocationWithAnyLabel() {
        val location =
            SavedLocation(
                "content://com.google.android.apps.docs.storage/tree/abc%3A1",
                "My Drive: work",
            )

        assertEquals(location, SavedLocationCodec.decode(SavedLocationCodec.encode(location)))
    }

    @Test
    fun rejectsAValueWithoutAUri() {
        assertNull(SavedLocationCodec.decode("just a label"))
        assertNull(SavedLocationCodec.decode("\nlabel"))
    }
}
