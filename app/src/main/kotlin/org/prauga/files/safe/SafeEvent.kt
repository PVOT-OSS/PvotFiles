// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.safe

sealed interface SafeEvent {
    data class CreatePin(val pin: String, val confirmation: String) : SafeEvent

    data class Unlock(val pin: String) : SafeEvent

    data object Lock : SafeEvent

    data class Open(val id: String) : SafeEvent

    data class MoveOut(val id: String) : SafeEvent

    data class Delete(val id: String) : SafeEvent

    data object Stopped : SafeEvent

    data object Started : SafeEvent
}
