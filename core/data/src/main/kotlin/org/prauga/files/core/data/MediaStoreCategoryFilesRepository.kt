// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.FileEntry

internal class MediaStoreCategoryFilesRepository
@Inject
constructor(
    private val files: MediaStoreFileQuery,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : CategoryFilesRepository {

    override suspend fun files(category: FileCategory): List<FileEntry.File>? =
        withContext(dispatcher) {
            val selection = CategorySelection.of(category)
            files.newestFirst(selection.selection, selection.arguments)
        }
}
