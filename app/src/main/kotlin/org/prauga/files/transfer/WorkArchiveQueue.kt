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

class WorkArchiveQueue @Inject constructor(@ApplicationContext context: Context) : ArchiveQueue {
    private val workManager = WorkManager.getInstance(context)

    override val updates: Flow<List<ArchiveUpdate>> =
        workManager.getWorkInfosByTagFlow(TAG).map { infos -> infos.mapNotNull { it.toUpdate() } }

    override fun start(operation: ArchiveOperation, paths: List<String>, folder: String) {
        val request =
            OneTimeWorkRequestBuilder<ArchiveWorker>()
                .setInputData(
                    workDataOf(
                        ArchiveWorker.KEY_OPERATION to operation.name,
                        ArchiveWorker.KEY_PATHS to paths.toTypedArray(),
                        ArchiveWorker.KEY_FOLDER to folder,
                    )
                )
                .addTag(TAG)
                .addTag(operation.name)
                .build()
        workManager.enqueue(request)
    }

    private fun WorkInfo.toUpdate(): ArchiveUpdate? {
        val operation = ArchiveOperation.entries.firstOrNull { it.name in tags } ?: return null
        val succeeded =
            when (state) {
                WorkInfo.State.SUCCEEDED -> true
                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED -> false
                else -> null
            }
        return ArchiveUpdate(
            id.toString(),
            operation,
            running = succeeded == null,
            succeeded = succeeded,
        )
    }

    private companion object {
        const val TAG = "archive"
    }
}
