// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.prauga.files.core.model.FileOperationResult

interface FileOperationsRepository {
    suspend fun createFolder(parentPath: String, name: String): FileOperationResult

    suspend fun rename(path: String, newName: String): FileOperationResult
}
