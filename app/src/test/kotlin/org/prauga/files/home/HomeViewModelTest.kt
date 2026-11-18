// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.home

import com.prauga.pvot.coreui.UiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.prauga.files.core.data.DocumentTreeRepository
import org.prauga.files.core.data.FavouritesRepository
import org.prauga.files.core.data.RecentFilesRepository
import org.prauga.files.core.data.StorageRepository
import org.prauga.files.core.data.StorageUsageRepository
import org.prauga.files.core.data.TagsRepository
import org.prauga.files.core.model.CategoryUsage
import org.prauga.files.core.model.DocumentEntry
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.SavedLocation
import org.prauga.files.core.model.StorageUsage
import org.prauga.files.core.model.Tag
import org.prauga.files.core.model.Volume

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val usage =
        StorageUsage(
            totalBytes = 100,
            freeBytes = 40,
            listOf(CategoryUsage(FileCategory.Photos, 20, 3)),
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
    fun refreshShowsTheMeasuredUsage() = runTest {
        val viewModel =
            HomeViewModel(
                recentFiles = FakeRecentFilesRepository(),
                favourites = FakeFavouritesRepository(),
                tags = FakeTagsRepository(),
                storage = FakeStorageRepository(),
                locations = FakeDocumentTreeRepository(),
                repository = FakeStorageUsageRepository { usage },
            )

        viewModel.onEvent(HomeEvent.Refresh)

        assertEquals(UiState.Success(usage), viewModel.state.value)
    }

    @Test
    fun unavailableStorageIsAnError() = runTest {
        val viewModel =
            HomeViewModel(
                recentFiles = FakeRecentFilesRepository(),
                favourites = FakeFavouritesRepository(),
                tags = FakeTagsRepository(),
                storage = FakeStorageRepository(),
                locations = FakeDocumentTreeRepository(),
                repository = FakeStorageUsageRepository { null },
            )

        viewModel.onEvent(HomeEvent.Refresh)

        assertTrue(viewModel.state.value is UiState.Error)
    }

    @Test
    fun refreshingKeepsTheLastUsageOnScreen() = runTest {
        val pending = CompletableDeferred<StorageUsage?>()
        var calls = 0
        val viewModel =
            HomeViewModel(
                FakeStorageUsageRepository { if (calls++ == 0) usage else pending.await() },
                FakeRecentFilesRepository(),
                FakeFavouritesRepository(),
                FakeTagsRepository(),
                FakeStorageRepository(),
                FakeDocumentTreeRepository(),
            )
        viewModel.onEvent(HomeEvent.Refresh)

        viewModel.onEvent(HomeEvent.Refresh)
        assertEquals(UiState.Success(usage), viewModel.state.value)

        val updated = usage.copy(freeBytes = 30)
        pending.complete(updated)
        assertEquals(UiState.Success(updated), viewModel.state.value)
    }

    @Test
    fun refreshAlsoLoadsTheRecentFiles() = runTest {
        val file = FileEntry.File("/storage/a.pdf", "a.pdf", 0L, sizeBytes = 1)
        val viewModel =
            HomeViewModel(
                repository = FakeStorageUsageRepository { usage },
                recentFiles = FakeRecentFilesRepository(listOf(file)),
                favourites = FakeFavouritesRepository(),
                tags = FakeTagsRepository(),
                storage = FakeStorageRepository(),
                locations = FakeDocumentTreeRepository(),
            )

        viewModel.onEvent(HomeEvent.Refresh)

        assertEquals(listOf(file), viewModel.recent.value)
    }

    private class FakeDocumentTreeRepository : DocumentTreeRepository {
        override val locations: Flow<List<SavedLocation>> = flowOf(emptyList())

        override suspend fun add(treeUri: String): SavedLocation? = null

        override suspend fun remove(treeUri: String) = Unit

        override suspend fun children(treeUri: String, documentId: String?): List<DocumentEntry>? =
            null

        override fun documentUri(treeUri: String, documentId: String): String = treeUri
    }

    private class FakeStorageRepository : StorageRepository {
        override fun primaryRootPath(): String? = "/storage/emulated/0"

        override fun volumes(): List<Volume> =
            listOf(
                Volume(
                    "/storage/emulated/0",
                    "Internal",
                    primary = true,
                    mediaStoreName = "external_primary",
                ),
                Volume(
                    "/storage/6200-0A16",
                    "SD card",
                    primary = false,
                    mediaStoreName = "6200-0a16",
                ),
            )
    }

    @Test
    fun refreshListsTheRemovableVolumes() = runTest {
        val viewModel =
            HomeViewModel(
                repository = FakeStorageUsageRepository { usage },
                recentFiles = FakeRecentFilesRepository(),
                favourites = FakeFavouritesRepository(),
                tags = FakeTagsRepository(),
                storage = FakeStorageRepository(),
                locations = FakeDocumentTreeRepository(),
            )

        viewModel.onEvent(HomeEvent.Refresh)

        assertEquals(listOf("SD card"), viewModel.removableVolumes.value.map { it.label })
    }

    private class FakeTagsRepository : TagsRepository {
        override val tags: Flow<List<Tag>> = flowOf(emptyList())

        override fun tagsOf(path: String): Flow<Set<String>> = flowOf(emptySet())

        override suspend fun createTag(tag: Tag): FileOperationResult = FileOperationResult.Done

        override suspend fun deleteTag(name: String) = Unit

        override suspend fun setTagged(path: String, tagName: String, tagged: Boolean) = Unit

        override suspend fun entriesTagged(tagName: String): List<FileEntry> = emptyList()
    }

    private class FakeFavouritesRepository : FavouritesRepository {
        override val favourites: Flow<List<String>> = flowOf(emptyList())

        override suspend fun setFavourite(path: String, favourite: Boolean) = Unit
    }

    private class FakeRecentFilesRepository(private val files: List<FileEntry.File> = emptyList()) :
        RecentFilesRepository {
        override suspend fun recent(limit: Int): List<FileEntry.File>? = files.take(limit)
    }

    private class FakeStorageUsageRepository(private val measure: suspend () -> StorageUsage?) :
        StorageUsageRepository {
        override suspend fun measure(): StorageUsage? = measure.invoke()
    }
}
