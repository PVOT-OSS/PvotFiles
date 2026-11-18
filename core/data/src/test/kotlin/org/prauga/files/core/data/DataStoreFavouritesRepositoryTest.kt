// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreFavouritesRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun keepsFavouritesSortedByFolderName() = runTest {
        val repository = repository()

        repository.setFavourite("/storage/Pictures", favourite = true)
        repository.setFavourite("/storage/Download", favourite = true)
        repository.setFavourite("/storage/a/music", favourite = true)

        assertEquals(
            listOf("/storage/Download", "/storage/a/music", "/storage/Pictures"),
            repository.favourites.first(),
        )
    }

    @Test
    fun removesAFavourite() = runTest {
        val repository = repository()
        repository.setFavourite("/storage/Download", favourite = true)

        repository.setFavourite("/storage/Download", favourite = false)

        assertEquals(emptyList<String>(), repository.favourites.first())
    }

    private fun TestScope.repository() =
        DataStoreFavouritesRepository(
            PreferenceDataStoreFactory.create(scope = backgroundScope) {
                File(folder.root, "favourites.preferences_pb")
            }
        )
}
