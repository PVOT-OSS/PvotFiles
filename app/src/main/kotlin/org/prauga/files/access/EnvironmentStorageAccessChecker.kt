// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.access

import android.os.Environment
import javax.inject.Inject

class EnvironmentStorageAccessChecker @Inject constructor() : StorageAccessChecker {
    override fun isGranted(): Boolean = Environment.isExternalStorageManager()
}
