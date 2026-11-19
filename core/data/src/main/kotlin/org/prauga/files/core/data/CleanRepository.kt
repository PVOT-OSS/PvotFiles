// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.prauga.files.core.model.CleanCategory
import org.prauga.files.core.model.CleanSummary
import org.prauga.files.core.model.FileEntry

interface CleanRepository {
    suspend fun summary(): List<CleanSummary>?

    suspend fun groups(category: CleanCategory): List<List<FileEntry>>?
}
