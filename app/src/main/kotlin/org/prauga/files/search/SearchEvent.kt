// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.search

import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.ModifiedWithin
import org.prauga.files.core.model.SizeRange

sealed interface SearchEvent {
    data class QueryChanged(val query: String) : SearchEvent

    data object Clear : SearchEvent

    data object Retry : SearchEvent

    data class CategoryChanged(val category: FileCategory?) : SearchEvent

    data class ModifiedChanged(val modified: ModifiedWithin) : SearchEvent

    data class SizeChanged(val size: SizeRange) : SearchEvent

    data object ResultOpened : SearchEvent

    data class HistoryPicked(val query: String) : SearchEvent

    data object ClearHistory : SearchEvent
}
