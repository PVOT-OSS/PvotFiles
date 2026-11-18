// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import androidx.lifecycle.SavedStateHandle
import com.prauga.pvot.coreui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
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
import org.prauga.files.core.data.DisplayOptionsRepository
import org.prauga.files.core.data.FavouritesRepository
import org.prauga.files.core.data.FileOperationsRepository
import org.prauga.files.core.data.StorageRepository
import org.prauga.files.core.data.TrashRepository
import org.prauga.files.core.model.DirectoryListing
import org.prauga.files.core.model.DisplayOptions
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.SortOrder
import org.prauga.files.core.model.TransferKind
import org.prauga.files.core.model.TrashedItem
import org.prauga.files.core.model.ViewLayout
import org.prauga.files.core.model.Volume
import org.prauga.files.transfer.TransferOutcome
import org.prauga.files.transfer.TransferQueue
import org.prauga.files.transfer.TransferUpdate

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
        val viewModel = viewModel(listings = failing)
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
        val first = viewModel(saved = saved)
        first.onEvent(BrowseEvent.Open(music))

        val restored = viewModel(saved = saved)
        collect(restored)

        assertEquals("Music", restored.state.value.folderName)
    }

    @Test
    fun sortOrderReordersWithoutListingAgain() = runTest {
        val small = FileEntry.File("/storage/a-small.txt", "a-small.txt", 0L, sizeBytes = 1)
        val large = FileEntry.File("/storage/z-large.txt", "z-large.txt", 0L, sizeBytes = 9)
        val repository =
            FakeDirectoryRepository(
                mapOf("/storage" to DirectoryListing.Entries(listOf(large, small)))
            )
        val viewModel = viewModel(listings = repository)
        collect(viewModel)
        assertEquals(UiState.Success(listOf(small, large)), viewModel.state.value.content)

        viewModel.onEvent(BrowseEvent.SetSortOrder(SortOrder.Size))

        assertEquals(UiState.Success(listOf(large, small)), viewModel.state.value.content)
        assertEquals(SortOrder.Size, viewModel.state.value.options.sortOrder)
        assertEquals(1, repository.hiddenRequests.size)
    }

    @Test
    fun showingHiddenFilesListsAgainWithThem() = runTest {
        val viewModel = viewModel()
        collect(viewModel)

        viewModel.onEvent(BrowseEvent.SetShowHidden(true))

        assertEquals(listOf(false, true), directories.hiddenRequests)
        assertTrue(viewModel.state.value.options.showHidden)
    }

    @Test
    fun layoutIsPartOfTheState() = runTest {
        val viewModel = viewModel(root = "/storage")
        collect(viewModel)

        viewModel.onEvent(BrowseEvent.SetLayout(ViewLayout.Grid))

        assertEquals(ViewLayout.Grid, viewModel.state.value.options.layout)
    }

    @Test
    fun creatingAFolderListsAgain() = runTest {
        val operations = FakeFileOperationsRepository()
        val viewModel = viewModel(root = "/storage", operations = operations)
        collect(viewModel)

        viewModel.onEvent(BrowseEvent.CreateFolder("Trips"))

        assertEquals(listOf("/storage" to "Trips"), operations.created)
        assertEquals(listOf(false, false), directories.hiddenRequests)
    }

    @Test
    fun aFailedOperationIsReported() = runTest {
        val operations = FakeFileOperationsRepository(result = FileOperationResult.AlreadyExists)
        val viewModel = viewModel(root = "/storage", operations = operations)
        collect(viewModel)
        val effects = mutableListOf<BrowseEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onEvent(BrowseEvent.CreateFolder("Music"))

        assertEquals(
            listOf(BrowseEffect.OperationFailed(FileOperationResult.AlreadyExists)),
            effects,
        )
    }

    @Test
    fun deletingMovesToTheTrashAndOffersUndo() = runTest {
        val trash = FakeTrashRepository()
        val viewModel = viewModel(trash = trash)
        collect(viewModel)
        val effects = mutableListOf<BrowseEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onEvent(BrowseEvent.Delete(music.path))
        viewModel.onEvent(BrowseEvent.Restore("id-1"))

        assertEquals(listOf(music.path), trash.trashed)
        assertEquals(listOf("id-1"), trash.restored)
        assertEquals(listOf(BrowseEffect.Trashed("id-1", "Music")), effects)
        assertEquals(listOf(false, false, false), directories.hiddenRequests)
    }

    @Test
    fun aDeleteThatFailsIsReported() = runTest {
        val viewModel = viewModel(trash = FakeTrashRepository(id = null))
        collect(viewModel)
        val effects = mutableListOf<BrowseEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.onEvent(BrowseEvent.Delete(music.path))

        assertEquals(listOf(BrowseEffect.OperationFailed(FileOperationResult.Failed)), effects)
    }

    @Test
    fun renameIsForwarded() = runTest {
        val operations = FakeFileOperationsRepository()
        val viewModel = viewModel(operations = operations)
        collect(viewModel)

        viewModel.onEvent(BrowseEvent.Rename(music.path, "Songs"))

        assertEquals(listOf(music.path to "Songs"), operations.renamed)
    }

    @Test
    fun theTrashFolderIsNotListed() = runTest {
        val trashFolder = FileEntry.Directory("/storage/.trash-files", ".trash-files", 0L, 1)
        val listings =
            FakeDirectoryRepository(
                mapOf("/storage" to DirectoryListing.Entries(listOf(trashFolder, music)))
            )
        val viewModel = viewModel(listings = listings)
        collect(viewModel)

        assertEquals(UiState.Success(listOf(music)), viewModel.state.value.content)
    }

    @Test
    fun copyThenPasteStartsATransferHereAndClearsTheClipboard() = runTest {
        val transfers = FakeTransferQueue()
        val viewModel = viewModel(transfers = transfers)
        collect(viewModel)

        viewModel.onEvent(BrowseEvent.Copy("/elsewhere/a.txt"))
        assertEquals(
            Clipboard(listOf("/elsewhere/a.txt"), TransferKind.Copy),
            viewModel.state.value.clipboard,
        )

        viewModel.onEvent(BrowseEvent.Open(music))
        viewModel.onEvent(BrowseEvent.Paste)

        assertEquals(
            listOf(Triple(listOf("/elsewhere/a.txt"), "/storage/Music", TransferKind.Copy)),
            transfers.started,
        )
        assertEquals(null, viewModel.state.value.clipboard)
    }

    @Test
    fun theClipboardSurvivesProcessDeath() = runTest {
        val saved = SavedStateHandle()
        viewModel(saved = saved).onEvent(BrowseEvent.Move("/storage/a.txt"))

        val restored = viewModel(saved = saved)
        collect(restored)

        assertEquals(
            Clipboard(listOf("/storage/a.txt"), TransferKind.Move),
            restored.state.value.clipboard,
        )
    }

    @Test
    fun aFinishedTransferIsReportedOnceAndOldOnesAreNot() = runTest {
        val transfers = FakeTransferQueue()
        transfers.updates.value =
            listOf(TransferUpdate("old", TransferKind.Copy, false, TransferOutcome.Done))
        val viewModel = viewModel(transfers = transfers)
        collect(viewModel)
        val effects = mutableListOf<BrowseEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        transfers.updates.value =
            transfers.updates.value + TransferUpdate("new", TransferKind.Move, true, null)
        assertTrue(viewModel.state.value.transferring)
        transfers.updates.value =
            transfers.updates.value.dropLast(1) +
                TransferUpdate("new", TransferKind.Move, false, TransferOutcome.Done)
        transfers.updates.value = transfers.updates.value.toList()

        assertEquals(
            listOf(BrowseEffect.TransferFinished(TransferKind.Move, TransferOutcome.Done)),
            effects,
        )
        assertFalse(viewModel.state.value.transferring)
    }

    @Test
    fun theStarTogglesTheOpenFolder() = runTest {
        val favourites = FakeFavouritesRepository()
        val viewModel = viewModel(favourites = favourites)
        collect(viewModel)
        viewModel.onEvent(BrowseEvent.Open(music))

        viewModel.onEvent(BrowseEvent.ToggleFavourite)
        assertTrue(viewModel.state.value.favourite)

        viewModel.onEvent(BrowseEvent.ToggleFavourite)
        assertFalse(viewModel.state.value.favourite)
        assertEquals(emptyList<String>(), favourites.saved.value)
    }

    @Test
    fun goToOpensAFolderInsideTheRootOnly() = runTest {
        val viewModel = viewModel()
        collect(viewModel)

        viewModel.onEvent(BrowseEvent.GoTo("/elsewhere"))
        assertEquals(null, viewModel.state.value.folderName)

        viewModel.onEvent(BrowseEvent.GoTo("/storage/Music"))
        assertEquals("Music", viewModel.state.value.folderName)
    }

    private fun viewModel(
        root: String? = "/storage",
        listings: FakeDirectoryRepository = directories,
        operations: FakeFileOperationsRepository = FakeFileOperationsRepository(),
        saved: SavedStateHandle = SavedStateHandle(),
        trash: FakeTrashRepository = FakeTrashRepository(),
        transfers: FakeTransferQueue = FakeTransferQueue(),
        favourites: FakeFavouritesRepository = FakeFavouritesRepository(),
    ) =
        BrowseViewModel(
            saved,
            FakeStorageRepository(root),
            listings,
            FakeDisplayOptionsRepository(),
            operations,
            trash,
            transfers,
            favourites,
        )

    private fun TestScope.collect(viewModel: BrowseViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
    }

    private class FakeStorageRepository(private val root: String?) : StorageRepository {
        override fun primaryRootPath(): String? = root

        override fun volumes(): List<Volume> =
            listOfNotNull(
                root?.let {
                    Volume(it, "Internal", primary = true, mediaStoreName = "external_primary")
                }
            )
    }

    private class FakeDirectoryRepository(var listings: Map<String, DirectoryListing>) :
        DirectoryRepository {
        val hiddenRequests = mutableListOf<Boolean>()

        override suspend fun list(path: String, includeHidden: Boolean): DirectoryListing {
            hiddenRequests += includeHidden
            return listings[path] ?: DirectoryListing.NotFound
        }
    }

    private class FakeFileOperationsRepository(
        private val result: FileOperationResult = FileOperationResult.Done
    ) : FileOperationsRepository {
        val created = mutableListOf<Pair<String, String>>()
        val renamed = mutableListOf<Pair<String, String>>()

        override suspend fun createFolder(parentPath: String, name: String): FileOperationResult {
            created += parentPath to name
            return result
        }

        override suspend fun rename(path: String, newName: String): FileOperationResult {
            renamed += path to newName
            return result
        }
    }

    private class FakeFavouritesRepository : FavouritesRepository {
        val saved = MutableStateFlow<List<String>>(emptyList())
        override val favourites: Flow<List<String>> = saved

        override suspend fun setFavourite(path: String, favourite: Boolean) {
            saved.update { if (favourite) it + path else it - path }
        }
    }

    private class FakeTransferQueue : TransferQueue {
        override val updates = MutableStateFlow<List<TransferUpdate>>(emptyList())
        val started = mutableListOf<Triple<List<String>, String, TransferKind>>()

        override fun start(sources: List<String>, destination: String, kind: TransferKind) {
            started += Triple(sources, destination, kind)
        }
    }

    private class FakeTrashRepository(private val id: String? = "id-1") : TrashRepository {
        val trashed = mutableListOf<String>()
        val restored = mutableListOf<String>()

        override suspend fun moveToTrash(path: String): String? {
            trashed += path
            return id
        }

        override suspend fun items(): List<TrashedItem>? = emptyList()

        override suspend fun restore(id: String): FileOperationResult {
            restored += id
            return FileOperationResult.Done
        }

        override suspend fun deleteForever(id: String): FileOperationResult =
            FileOperationResult.Done

        override fun isTrash(path: String): Boolean = path == "/storage/.trash-files"
    }

    private class FakeDisplayOptionsRepository : DisplayOptionsRepository {
        private val state = MutableStateFlow(DisplayOptions())
        override val options: Flow<DisplayOptions> = state

        override suspend fun update(transform: (DisplayOptions) -> DisplayOptions) {
            state.update(transform)
        }
    }
}
