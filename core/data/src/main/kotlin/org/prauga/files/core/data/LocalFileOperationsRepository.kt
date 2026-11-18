// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileNames
import org.prauga.files.core.model.FileOperationResult

internal class LocalFileOperationsRepository
@Inject
constructor(
    private val mediaIndex: MediaIndex,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : FileOperationsRepository {

    override suspend fun createFolder(parentPath: String, name: String): FileOperationResult =
        withContext(dispatcher) {
            if (!FileNames.isValid(name)) return@withContext FileOperationResult.InvalidName
            val folder = File(parentPath, name)
            when {
                folder.exists() -> FileOperationResult.AlreadyExists
                folder.mkdir() -> {
                    mediaIndex.update(listOf(folder.path))
                    FileOperationResult.Done
                }
                else -> FileOperationResult.Failed
            }
        }

    override suspend fun rename(path: String, newName: String): FileOperationResult =
        withContext(dispatcher) {
            if (!FileNames.isValid(newName)) return@withContext FileOperationResult.InvalidName
            val source = File(path)
            val target = File(source.parentFile, newName)
            when {
                !source.exists() -> FileOperationResult.Failed
                target.path == source.path -> FileOperationResult.Done
                target.exists() && !target.path.equals(source.path, ignoreCase = true) ->
                    FileOperationResult.AlreadyExists
                source.renameTo(target) -> {
                    mediaIndex.update(listOf(source.path, target.path))
                    FileOperationResult.Done
                }
                else -> FileOperationResult.Failed
            }
        }
}
