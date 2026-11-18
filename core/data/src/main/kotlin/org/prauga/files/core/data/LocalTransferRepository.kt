// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.TransferKind
import org.prauga.files.core.model.TransferProgress
import org.prauga.files.core.model.TransferResult

internal class LocalTransferRepository
@Inject
constructor(mediaIndex: MediaIndex, @IoDispatcher private val dispatcher: CoroutineDispatcher) :
    TransferRepository {
    private val engine = TransferEngine(mediaIndex)

    override suspend fun transfer(
        sources: List<String>,
        destination: String,
        kind: TransferKind,
        onProgress: (TransferProgress) -> Unit,
    ): TransferResult =
        withContext(dispatcher) {
            val context = currentCoroutineContext()
            engine.run(sources, destination, kind, onProgress, isCancelled = { !context.isActive })
        }
}
