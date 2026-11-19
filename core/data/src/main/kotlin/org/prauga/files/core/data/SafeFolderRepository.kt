// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import kotlinx.coroutines.flow.Flow
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.SafeItem
import org.prauga.files.core.model.UnlockResult

interface SafeFolderRepository {
    val hasPin: Flow<Boolean>

    suspend fun setPin(pin: String): Boolean

    suspend fun unlock(pin: String): UnlockResult

    suspend fun items(): List<SafeItem>?

    suspend fun add(paths: List<String>): FileOperationResult

    suspend fun moveOut(id: String, folder: String): FileOperationResult

    suspend fun open(id: String): String?

    suspend fun delete(id: String): FileOperationResult

    suspend fun clearOpened()
}
