// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

sealed interface TransferResult {
    data class Done(val itemCount: Int) : TransferResult

    data object IntoItself : TransferResult

    data object Cancelled : TransferResult

    data object Failed : TransferResult
}
