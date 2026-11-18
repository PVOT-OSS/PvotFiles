// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.trash

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.prauga.files.core.data.TrashRepository
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.TrashedItem

@OptIn(ExperimentalCoroutinesApi::class)
class TrashViewModelTest {
    private val item = TrashedItem("id-1", "Trips", "/storage/Trips", 0L, isDirectory = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun listsTheTrashedItems() = runTest {
        val viewModel = TrashViewModel(FakeTrashRepository(mutableListOf(item)))
        collect(viewModel)

        assertEquals(UiState.Success(listOf(item)), viewModel.state.value)
    }

    @Test
    fun restoringRemovesTheItemAndSaysSo() = runTest {
        val viewModel = TrashViewModel(FakeTrashRepository(mutableListOf(item)))
        collect(viewModel)
        val effects = effects(viewModel)

        viewModel.onEvent(TrashEvent.Restore("id-1"))

        assertEquals(UiState.Success(emptyList<TrashedItem>()), viewModel.state.value)
        assertEquals(listOf(TrashEffect.Restored("Trips")), effects)
    }

    @Test
    fun aRestoreOntoAnExistingNameIsReported() = runTest {
        val repository =
            FakeTrashRepository(mutableListOf(item), restore = FileOperationResult.AlreadyExists)
        val viewModel = TrashViewModel(repository)
        collect(viewModel)
        val effects = effects(viewModel)

        viewModel.onEvent(TrashEvent.Restore("id-1"))

        assertEquals(
            listOf(TrashEffect.OperationFailed(FileOperationResult.AlreadyExists)),
            effects,
        )
        assertEquals(UiState.Success(listOf(item)), viewModel.state.value)
    }

    @Test
    fun deleteForeverRemovesTheItem() = runTest {
        val viewModel = TrashViewModel(FakeTrashRepository(mutableListOf(item)))
        collect(viewModel)

        viewModel.onEvent(TrashEvent.DeleteForever("id-1"))

        assertEquals(UiState.Success(emptyList<TrashedItem>()), viewModel.state.value)
    }

    @Test
    fun unavailableStorageIsAnError() = runTest {
        val viewModel = TrashViewModel(FakeTrashRepository(null))
        collect(viewModel)

        assertTrue(viewModel.state.value is UiState.Error)
    }

    private fun TestScope.collect(viewModel: TrashViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
    }

    private fun TestScope.effects(viewModel: TrashViewModel): List<TrashEffect> {
        val effects = mutableListOf<TrashEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }
        return effects
    }

    private class FakeTrashRepository(
        private val items: MutableList<TrashedItem>?,
        private val restore: FileOperationResult = FileOperationResult.Done,
    ) : TrashRepository {
        override suspend fun moveToTrash(path: String): String? = null

        override suspend fun items(): List<TrashedItem>? = items?.toList()

        override suspend fun restore(id: String): FileOperationResult {
            if (restore == FileOperationResult.Done) items?.removeAll { it.id == id }
            return restore
        }

        override suspend fun deleteForever(id: String): FileOperationResult {
            items?.removeAll { it.id == id }
            return FileOperationResult.Done
        }

        override fun isTrash(path: String): Boolean = false
    }
}
