// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.prauga.files.core.model.DisplayOptions

internal class DataStoreDisplayOptionsRepository
@Inject
constructor(private val store: DataStore<Preferences>) : DisplayOptionsRepository {

    override val options: Flow<DisplayOptions> = store.data.map { it.toOptions() }

    override suspend fun update(transform: (DisplayOptions) -> DisplayOptions) {
        store.edit { preferences ->
            val updated = transform(preferences.toOptions())
            preferences[SORT_ORDER] = updated.sortOrder.name
            preferences[LAYOUT] = updated.layout.name
            preferences[SHOW_HIDDEN] = updated.showHidden
        }
    }

    private fun Preferences.toOptions(): DisplayOptions {
        val defaults = DisplayOptions()
        return DisplayOptions(
            sortOrder = enumOrDefault(this[SORT_ORDER], defaults.sortOrder),
            layout = enumOrDefault(this[LAYOUT], defaults.layout),
            showHidden = this[SHOW_HIDDEN] ?: defaults.showHidden,
        )
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default

    private companion object {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val LAYOUT = stringPreferencesKey("layout")
        val SHOW_HIDDEN = booleanPreferencesKey("show_hidden")
    }
}
