// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.locations

import org.prauga.files.core.model.DocumentEntry

sealed interface TreeEvent {
    data class Open(val folder: DocumentEntry) : TreeEvent

    data object Up : TreeEvent

    data object Retry : TreeEvent

    data object RemoveLocation : TreeEvent
}
