// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

sealed interface UnlockResult {
    data object Unlocked : UnlockResult

    data class Wrong(val attemptsLeft: Int) : UnlockResult

    data class LockedOut(val untilMillis: Long) : UnlockResult
}
