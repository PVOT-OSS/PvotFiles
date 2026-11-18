// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.prauga.files.core.model.DisplayOptions
import org.prauga.files.core.model.SortOrder
import org.prauga.files.core.model.ViewLayout

class DataStoreDisplayOptionsRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun startsWithTheDefaults() = runTest {
        assertEquals(DisplayOptions(), repository().options.first())
    }

    @Test
    fun keepsWhatWasChanged() = runTest {
        val repository = repository()

        repository.update { it.copy(sortOrder = SortOrder.Size, layout = ViewLayout.Grid) }
        repository.update { it.copy(showHidden = true) }

        assertEquals(
            DisplayOptions(SortOrder.Size, ViewLayout.Grid, showHidden = true),
            repository.options.first(),
        )
    }

    @Test
    fun anUnknownStoredValueFallsBackToTheDefault() = runTest {
        val store = store()
        store.edit { it[stringPreferencesKey("sort_order")] = "Colour" }

        assertEquals(
            SortOrder.Name,
            DataStoreDisplayOptionsRepository(store).options.first().sortOrder,
        )
    }

    private fun TestScope.store() =
        PreferenceDataStoreFactory.create(scope = backgroundScope) {
            File(folder.root, "options.preferences_pb")
        }

    private fun TestScope.repository() = DataStoreDisplayOptionsRepository(store())
}
