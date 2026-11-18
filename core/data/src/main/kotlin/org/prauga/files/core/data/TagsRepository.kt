// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import kotlinx.coroutines.flow.Flow
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.Tag

interface TagsRepository {
    val tags: Flow<List<Tag>>

    fun tagsOf(path: String): Flow<Set<String>>

    suspend fun createTag(tag: Tag): FileOperationResult

    suspend fun deleteTag(name: String)

    suspend fun setTagged(path: String, tagName: String, tagged: Boolean)

    suspend fun entriesTagged(tagName: String): List<FileEntry>
}
