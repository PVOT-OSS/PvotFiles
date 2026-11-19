// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.transfer

import kotlinx.coroutines.flow.Flow

interface ArchiveQueue {
    val updates: Flow<List<ArchiveUpdate>>

    fun start(operation: ArchiveOperation, paths: List<String>, folder: String)
}
