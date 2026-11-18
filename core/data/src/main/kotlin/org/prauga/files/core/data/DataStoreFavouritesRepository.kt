// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DataStoreFavouritesRepository
@Inject
constructor(@FavouritesStore private val store: DataStore<Preferences>) : FavouritesRepository {

    override val favourites: Flow<List<String>> =
        store.data.map { preferences ->
            preferences[PATHS]
                .orEmpty()
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { File(it).name })
        }

    override suspend fun setFavourite(path: String, favourite: Boolean) {
        store.edit { preferences ->
            val current = preferences[PATHS].orEmpty()
            preferences[PATHS] = if (favourite) current + path else current - path
        }
    }

    private companion object {
        val PATHS = stringSetPreferencesKey("paths")
    }
}
