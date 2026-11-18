// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.junit.Assert.assertEquals
import org.junit.Test

class NameQueryTest {
    @Test
    fun wrapsTheTrimmedQuery() {
        assertEquals("%holiday%", NameQuery.likePattern("  holiday "))
    }

    @Test
    fun escapesLikeWildcards() {
        assertEquals("%50\\%\\_off\\\\x%", NameQuery.likePattern("50%_off\\x"))
    }
}
