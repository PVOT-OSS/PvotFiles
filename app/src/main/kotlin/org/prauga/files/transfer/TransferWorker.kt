// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.transfer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.hilt.android.EntryPointAccessors
import org.prauga.files.R
import org.prauga.files.core.model.TransferKind
import org.prauga.files.core.model.TransferProgress
import org.prauga.files.core.model.TransferResult

class TransferWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val sources = inputData.getStringArray(KEY_SOURCES)?.toList() ?: return Result.failure()
        val destination = inputData.getString(KEY_DESTINATION) ?: return Result.failure()
        val kind =
            TransferKind.entries.firstOrNull { it.name == inputData.getString(KEY_KIND) }
                ?: return Result.failure()
        val transfers =
            EntryPointAccessors.fromApplication(applicationContext, TransferEntryPoint::class.java)
                .transfers()
        setForeground(foregroundInfo(kind, null))
        var lastUpdate = 0L
        val result =
            transfers.transfer(sources, destination, kind) { progress ->
                val now = System.currentTimeMillis()
                if (
                    now - lastUpdate >= UPDATE_INTERVAL_MILLIS ||
                        progress.doneBytes == progress.totalBytes
                ) {
                    lastUpdate = now
                    setForegroundAsync(foregroundInfo(kind, progress))
                }
            }
        // Outputs become inputs of the transfer appended after this one, so no input key may
        // appear.
        val output = workDataOf(KEY_OUTCOME to result.outcome())
        return when (result) {
            is TransferResult.Done -> Result.success(output)
            else -> Result.failure(output)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        foregroundInfo(
            TransferKind.entries.firstOrNull { it.name == inputData.getString(KEY_KIND) }
                ?: TransferKind.Copy,
            null,
        )

    private fun foregroundInfo(kind: TransferKind, progress: TransferProgress?): ForegroundInfo {
        createChannel()
        val title =
            applicationContext.getString(
                if (kind == TransferKind.Copy) R.string.transfer_copying
                else R.string.transfer_moving
            )
        val notification =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_folder)
                .setContentTitle(title)
                .setContentText(progress?.currentName)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(
                    PROGRESS_MAX,
                    progress?.let { fraction(it) } ?: 0,
                    progress == null || progress.totalBytes == 0L,
                )
                .build()
        return ForegroundInfo(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    }

    private fun createChannel() {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.transfer_channel),
                NotificationManager.IMPORTANCE_LOW,
            )
        manager.createNotificationChannel(channel)
    }

    private fun fraction(progress: TransferProgress): Int =
        if (progress.totalBytes <= 0) 0
        else (progress.doneBytes * PROGRESS_MAX / progress.totalBytes).toInt()

    companion object {
        const val KEY_SOURCES = "sources"
        const val KEY_DESTINATION = "destination"
        const val KEY_KIND = "kind"
        const val KEY_OUTCOME = "outcome"
        const val OUTCOME_DONE = "done"
        const val OUTCOME_INTO_ITSELF = "into_itself"
        const val OUTCOME_CANCELLED = "cancelled"
        const val OUTCOME_FAILED = "failed"
        private const val CHANNEL_ID = "transfers"
        private const val NOTIFICATION_ID = 1
        private const val PROGRESS_MAX = 100
        private const val UPDATE_INTERVAL_MILLIS = 500L

        private fun TransferResult.outcome(): String =
            when (this) {
                is TransferResult.Done -> OUTCOME_DONE
                TransferResult.IntoItself -> OUTCOME_INTO_ITSELF
                TransferResult.Cancelled -> OUTCOME_CANCELLED
                TransferResult.Failed -> OUTCOME_FAILED
            }
    }
}
