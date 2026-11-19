// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files

import android.app.DownloadManager
import android.content.Intent
import android.os.storage.StorageManager

enum class LaunchTarget(val action: String) {
    Downloads("org.prauga.files.action.OPEN_DOWNLOADS"),
    Clean("org.prauga.files.action.OPEN_CLEAN"),
    Search("org.prauga.files.action.OPEN_SEARCH"),
    SafeFolder("org.prauga.files.action.OPEN_SAFE_FOLDER");

    companion object {
        fun of(intent: Intent?): LaunchTarget? =
            when (val action = intent?.action) {
                DownloadManager.ACTION_VIEW_DOWNLOADS -> Downloads
                StorageManager.ACTION_MANAGE_STORAGE -> Clean
                else -> entries.firstOrNull { it.action == action }
            }
    }
}
