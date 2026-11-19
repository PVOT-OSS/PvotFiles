// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DataStoreSearchHistoryRepository
@Inject
constructor(@SearchHistoryStore private val store: DataStore<Preferences>) :
    SearchHistoryRepository {

    override val queries: Flow<List<String>> =
        store.data.map { preferences -> decode(preferences[KEY_QUERIES]) }

    override suspend fun add(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        store.edit { preferences ->
            val updated =
                (listOf(trimmed) +
                        decode(preferences[KEY_QUERIES]).filterNot {
                            it.equals(trimmed, ignoreCase = true)
                        })
                    .take(LIMIT)
            preferences[KEY_QUERIES] = updated.joinToString(SEPARATOR)
        }
    }

    override suspend fun clear() {
        store.edit { it.remove(KEY_QUERIES) }
    }

    private fun decode(value: String?): List<String> =
        value?.split(SEPARATOR)?.filter { it.isNotEmpty() }.orEmpty()

    private companion object {
        val KEY_QUERIES = stringPreferencesKey("queries")
        const val SEPARATOR = "\n"
        const val LIMIT = 10
    }
}
