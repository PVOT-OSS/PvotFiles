// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.TrashedItem

interface TrashRepository {
    suspend fun moveToTrash(path: String): String?

    suspend fun items(): List<TrashedItem>?

    suspend fun restore(id: String): FileOperationResult

    suspend fun deleteForever(id: String): FileOperationResult

    fun isTrash(path: String): Boolean
}
