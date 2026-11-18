// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.transfer

import kotlinx.coroutines.flow.Flow
import org.prauga.files.core.model.TransferKind

interface TransferQueue {
    val updates: Flow<List<TransferUpdate>>

    fun start(sources: List<String>, destination: String, kind: TransferKind)
}
