// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.content.Context
import android.media.MediaScannerConnection
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class ScannerMediaIndex
@Inject
constructor(@ApplicationContext private val context: Context) : MediaIndex {
    override fun update(paths: List<String>) {
        if (paths.isNotEmpty())
            MediaScannerConnection.scanFile(context, paths.toTypedArray(), null, null)
    }
}
