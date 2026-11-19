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

    data class Rename(val path: String, val newName: String) : BrowseEvent

    data class Delete(val path: String) : BrowseEvent

    data class Restore(val trashIds: List<String>) : BrowseEvent

    data class ToggleSelection(val path: String) : BrowseEvent

    data object SelectAll : BrowseEvent

    data object ClearSelection : BrowseEvent

    data object CopySelection : BrowseEvent

    data object MoveSelection : BrowseEvent

    data object DeleteSelection : BrowseEvent

    data class Compress(val paths: List<String>) : BrowseEvent

    data object CompressSelection : BrowseEvent

    data class Extract(val path: String) : BrowseEvent

    data class ToggleFavouriteOf(val path: String) : BrowseEvent

    data class MoveToSafe(val paths: List<String>) : BrowseEvent

    data object MoveSelectionToSafe : BrowseEvent

    data class Copy(val path: String) : BrowseEvent

    data class Move(val path: String) : BrowseEvent

    data object Paste : BrowseEvent

    data object ClearClipboard : BrowseEvent

    data class GoTo(val path: String) : BrowseEvent

    data object ToggleFavourite : BrowseEvent
}
