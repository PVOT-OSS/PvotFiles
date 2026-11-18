// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.prauga.files.core.model.TransferKind
import org.prauga.files.core.model.TransferProgress
import org.prauga.files.core.model.TransferResult

interface TransferRepository {
    suspend fun transfer(
        sources: List<String>,
        destination: String,
        kind: TransferKind,
        onProgress: (TransferProgress) -> Unit,
    ): TransferResult
}
