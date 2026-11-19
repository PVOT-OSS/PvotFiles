// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.storage

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.storage.StorageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.File
import org.prauga.files.LaunchTarget
import org.prauga.files.MainActivity
import org.prauga.files.R

class StorageEventsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val path = intent.data?.path ?: return
        val notifications = NotificationManagerCompat.from(context)
        when (intent.action) {
            Intent.ACTION_MEDIA_MOUNTED -> {
                if (
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                val volume =
                    context
                        .getSystemService(StorageManager::class.java)
                        .getStorageVolume(File(path))
                if (volume == null || volume.isPrimary || !volume.isRemovable) return
                createChannel(context)
                val open =
                    Intent(context, MainActivity::class.java)
                        .setAction(LaunchTarget.Folder.action)
                        .putExtra(LaunchTarget.EXTRA_PATH, path)
                val notification =
                    NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_sd_card)
                        .setContentTitle(
                            context.getString(
                                R.string.storage_ready_title,
                                volume.getDescription(context),
                            )
                        )
                        .setContentText(context.getString(R.string.storage_ready_text))
                        .setContentIntent(
                            PendingIntent.getActivity(
                                context,
                                path.hashCode(),
                                open,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                            )
                        )
                        .setAutoCancel(true)
                        .build()
                notifications.notify(TAG, path.hashCode(), notification)
            }
            Intent.ACTION_MEDIA_EJECT,
            Intent.ACTION_MEDIA_UNMOUNTED,
            Intent.ACTION_MEDIA_REMOVED -> notifications.cancel(TAG, path.hashCode())
        }
    }

    private fun createChannel(context: Context) {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.storage_channel),
                NotificationManager.IMPORTANCE_LOW,
            )
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "storage"
        const val TAG = "volume"
    }
}
