// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import org.prauga.files.core.model.FileEntry

sealed interface BrowseEvent {
    data class Open(val directory: FileEntry.Directory) : BrowseEvent

    data object Up : BrowseEvent

    data object Retry : BrowseEvent
}
