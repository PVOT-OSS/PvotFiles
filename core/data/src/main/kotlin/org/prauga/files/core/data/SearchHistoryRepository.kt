// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    val queries: Flow<List<String>>

    suspend fun add(query: String)

    suspend fun clear()
}
