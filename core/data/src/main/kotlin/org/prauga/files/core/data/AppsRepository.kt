// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.graphics.Bitmap
import org.prauga.files.core.model.UnusedApp

interface AppsRepository {
    fun hasUsageAccess(): Boolean

    suspend fun unusedApps(): List<UnusedApp>?

    suspend fun icon(packageName: String, sizePx: Int): Bitmap?
}
