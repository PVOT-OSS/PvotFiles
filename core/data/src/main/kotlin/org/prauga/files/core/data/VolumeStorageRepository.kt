// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.prauga.files.core.model.Volume

internal class VolumeStorageRepository
@Inject
constructor(@ApplicationContext private val context: Context) : StorageRepository {

    override fun primaryRootPath(): String? =
        context.getSystemService(StorageManager::class.java).primaryStorageVolume.directory?.path

    override fun volumes(): List<Volume> =
        context
            .getSystemService(StorageManager::class.java)
            .storageVolumes
            .filter {
                it.state == Environment.MEDIA_MOUNTED ||
                    it.state == Environment.MEDIA_MOUNTED_READ_ONLY
            }
            .mapNotNull { volume ->
                val directory = volume.directory ?: return@mapNotNull null
                val mediaStoreName = volume.mediaStoreVolumeName ?: return@mapNotNull null
                Volume(
                    directory.path,
                    volume.getDescription(context),
                    volume.isPrimary,
                    mediaStoreName,
                )
            }
            .sortedByDescending { it.primary }
}
