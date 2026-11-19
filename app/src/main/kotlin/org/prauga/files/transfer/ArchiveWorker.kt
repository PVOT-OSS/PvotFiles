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
import dagger.hilt.android.EntryPointAccessors
import org.prauga.files.R
import org.prauga.files.core.model.FileOperationResult

class ArchiveWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val operation = operation() ?: return Result.failure()
        val paths = inputData.getStringArray(KEY_PATHS)?.toList() ?: return Result.failure()
        val folder = inputData.getString(KEY_FOLDER) ?: return Result.failure()
        val archives =
            EntryPointAccessors.fromApplication(applicationContext, ArchiveEntryPoint::class.java)
                .archives()
        setForeground(foregroundInfo(operation))
        val result =
            when (operation) {
                ArchiveOperation.Compress -> archives.compress(paths, folder)
                ArchiveOperation.Extract ->
                    paths
                        .map { archives.extractTo(it, folder) }
                        .firstOrNull { it != FileOperationResult.Done } ?: FileOperationResult.Done
            }
        return if (result == FileOperationResult.Done) Result.success() else Result.failure()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        foregroundInfo(operation() ?: ArchiveOperation.Extract)

    private fun operation(): ArchiveOperation? =
        ArchiveOperation.entries.firstOrNull { it.name == inputData.getString(KEY_OPERATION) }

    private fun foregroundInfo(operation: ArchiveOperation): ForegroundInfo {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.transfer_channel),
                NotificationManager.IMPORTANCE_LOW,
            )
        )
        val notification =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_archive)
                .setContentTitle(
                    applicationContext.getString(
                        if (operation == ArchiveOperation.Compress) R.string.archive_compressing
                        else R.string.archive_extracting
                    )
                )
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(0, 0, true)
                .build()
        return ForegroundInfo(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    }

    companion object {
        const val KEY_OPERATION = "operation"
        const val KEY_PATHS = "paths"
        const val KEY_FOLDER = "folder"
        private const val CHANNEL_ID = "transfers"
        private const val NOTIFICATION_ID = 2
    }
}
