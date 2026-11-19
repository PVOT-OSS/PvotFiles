// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.trash

sealed interface TrashEvent {
    data class Restore(val id: String) : TrashEvent

    data class DeleteForever(val id: String) : TrashEvent

    data object Retry : TrashEvent

    data object EmptyAll : TrashEvent
}
