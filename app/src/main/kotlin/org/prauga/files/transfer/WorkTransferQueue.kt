// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.transfer

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.prauga.files.core.model.TransferKind

class WorkTransferQueue @Inject constructor(@ApplicationContext context: Context) : TransferQueue {
    private val workManager = WorkManager.getInstance(context)

    override val updates: Flow<List<TransferUpdate>> =
        workManager.getWorkInfosByTagFlow(TAG).map { infos -> infos.mapNotNull { it.toUpdate() } }

    override fun start(sources: List<String>, destination: String, kind: TransferKind) {
        val request =
            OneTimeWorkRequestBuilder<TransferWorker>()
                .setInputData(
                    workDataOf(
                        TransferWorker.KEY_SOURCES to sources.toTypedArray(),
                        TransferWorker.KEY_DESTINATION to destination,
                        TransferWorker.KEY_KIND to kind.name,
                    )
                )
                .addTag(TAG)
                .addTag(kind.name)
                .build()
        workManager.enqueue(request)
    }

    private fun WorkInfo.toUpdate(): TransferUpdate? {
        val kind = TransferKind.entries.firstOrNull { it.name in tags } ?: return null
        val outcome =
            when (state) {
                WorkInfo.State.SUCCEEDED -> TransferOutcome.Done
                WorkInfo.State.CANCELLED -> TransferOutcome.Cancelled
                WorkInfo.State.FAILED ->
                    when (outputData.getString(TransferWorker.KEY_OUTCOME)) {
                        TransferWorker.OUTCOME_INTO_ITSELF -> TransferOutcome.IntoItself
                        TransferWorker.OUTCOME_CANCELLED -> TransferOutcome.Cancelled
                        else -> TransferOutcome.Failed
                    }
                else -> null
            }
        return TransferUpdate(id.toString(), kind, running = outcome == null, outcome = outcome)
    }

    private companion object {
        const val TAG = "transfer"
    }
}
