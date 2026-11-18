// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import kotlinx.coroutines.flow.Flow
import org.prauga.files.core.model.DocumentEntry
import org.prauga.files.core.model.SavedLocation

interface DocumentTreeRepository {
    val locations: Flow<List<SavedLocation>>

    suspend fun add(treeUri: String): SavedLocation?

    suspend fun remove(treeUri: String)

    suspend fun children(treeUri: String, documentId: String?): List<DocumentEntry>?

    fun documentUri(treeUri: String, documentId: String): String
}
