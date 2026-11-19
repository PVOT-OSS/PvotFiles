// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.prauga.files.core.model.FileOperationResult

interface ArchiveRepository {
    suspend fun compress(paths: List<String>, folder: String): FileOperationResult

    suspend fun extract(path: String): FileOperationResult
}
