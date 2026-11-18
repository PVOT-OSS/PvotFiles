// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.content.Context
import android.os.storage.StorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class VolumeStorageRepository
@Inject
constructor(@ApplicationContext private val context: Context) : StorageRepository {

    override fun primaryRootPath(): String? =
        context.getSystemService(StorageManager::class.java).primaryStorageVolume.directory?.path
}
