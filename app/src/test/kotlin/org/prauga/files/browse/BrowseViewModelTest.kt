// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import androidx.lifecycle.SavedStateHandle
import com.prauga.pvot.coreui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.prauga.files.core.data.DirectoryRepository
import org.prauga.files.core.data.StorageRepository
import org.prauga.files.core.model.DirectoryListing
import org.prauga.files.core.model.FileEntry

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModelTest {
    private val music = FileEntry.Directory("/storage/Music", "Music", 0L, childCount = 1)
    private val song = FileEntry.File("/storage/Music/song.mp3", "song.mp3", 0L, sizeBytes = 3)
    private val directories =
        FakeDirectoryRepository(
            mapOf(
                "/storage" to DirectoryListing.Entries(listOf(music)),
                "/storage/Music" to DirectoryListing.Entries(listOf(song)),
            )
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsAtTheStorageRoot() = runTest {
        val viewModel = viewModel(root = "/storage")
        collect(viewModel)

        val state = viewModel.state.value
        assertEquals(UiState.Success(listOf(music)), state.content)
        assertEquals(null, state.folderName)
        assertFalse(state.canGoUp)
    }

    @Test
    fun openingAFolderListsItAndUpReturns() = runTest {
        val viewModel = viewModel(root = "/storage")
        collect(viewModel)

        viewModel.onEvent(BrowseEvent.Open(music))
        assertEquals(UiState.Success(listOf(song)), viewModel.state.value.content)
        assertEquals("Music", viewModel.state.value.folderName)
        assertTrue(viewModel.state.value.canGoUp)

        viewModel.onEvent(BrowseEvent.Up)
        assertEquals(UiState.Success(listOf(music)), viewModel.state.value.content)
    }

    @Test
    fun upStopsAtTheRoot() = runTest {
        val viewModel = viewModel(root = "/storage")
        collect(viewModel)

        viewModel.onEvent(BrowseEvent.Up)

        assertEquals(UiState.Success(listOf(music)), viewModel.state.value.content)
    }

    @Test
    fun unreadableFolderIsAnErrorAndRetryReloads() = runTest {
        val failing = FakeDirectoryRepository(mapOf("/storage" to DirectoryListing.Unreadable))
        val viewModel =
            BrowseViewModel(SavedStateHandle(), FakeStorageRepository("/storage"), failing)
        collect(viewModel)
        assertTrue(viewModel.state.value.content is UiState.Error)

        failing.listings = mapOf("/storage" to DirectoryListing.Entries(emptyList()))
        viewModel.onEvent(BrowseEvent.Retry)

        assertEquals(UiState.Success(emptyList<FileEntry>()), viewModel.state.value.content)
    }

    @Test
    fun missingStorageIsAnError() = runTest {
        val viewModel = viewModel(root = null)
        collect(viewModel)

        assertTrue(viewModel.state.value.content is UiState.Error)
        assertFalse(viewModel.state.value.canGoUp)
    }

    @Test
    fun theOpenFolderSurvivesProcessDeath() = runTest {
        val saved = SavedStateHandle()
        val first = BrowseViewModel(saved, FakeStorageRepository("/storage"), directories)
        first.onEvent(BrowseEvent.Open(music))

        val restored = BrowseViewModel(saved, FakeStorageRepository("/storage"), directories)
        collect(restored)

        assertEquals("Music", restored.state.value.folderName)
    }

    private fun viewModel(root: String?) =
        BrowseViewModel(SavedStateHandle(), FakeStorageRepository(root), directories)

    private fun TestScope.collect(viewModel: BrowseViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
    }

    private class FakeStorageRepository(private val root: String?) : StorageRepository {
        override fun primaryRootPath(): String? = root
    }

    private class FakeDirectoryRepository(var listings: Map<String, DirectoryListing>) :
        DirectoryRepository {
        override suspend fun list(path: String): DirectoryListing =
            listings[path] ?: DirectoryListing.NotFound
    }
}
