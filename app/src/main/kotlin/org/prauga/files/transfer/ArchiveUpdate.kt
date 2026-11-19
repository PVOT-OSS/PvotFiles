// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.transfer

data class ArchiveUpdate(
    val id: String,
    val operation: ArchiveOperation,
    val running: Boolean,
    val succeeded: Boolean?,
)
