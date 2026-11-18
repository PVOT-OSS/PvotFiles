// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.SortOrder
import org.prauga.files.core.model.ViewLayout

sealed interface BrowseEvent {
    data class Open(val directory: FileEntry.Directory) : BrowseEvent

    data object Up : BrowseEvent

    data object Retry : BrowseEvent

    data class SetSortOrder(val order: SortOrder) : BrowseEvent

    data class SetLayout(val layout: ViewLayout) : BrowseEvent

    data class SetShowHidden(val show: Boolean) : BrowseEvent

    data class CreateFolder(val name: String) : BrowseEvent
}
