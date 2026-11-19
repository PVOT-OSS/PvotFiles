// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.clean

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.prauga.files.core.data.CleanRepository
import org.prauga.files.core.data.TrashRepository
import org.prauga.files.core.model.CleanCategory
import org.prauga.files.core.model.CleanSummary
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.TrashedItem

@OptIn(ExperimentalCoroutinesApi::class)
class CleanListViewModelTest {
    private val original = FileEntry.File("/s/a.jpg", "a.jpg", 1, sizeBytes = 5)
    private val copy = FileEntry.File("/s/b/a.jpg", "a.jpg", 2, sizeBytes = 5)
    private val repository =
        object : CleanRepository {
            var groups: List<List<FileEntry>> = listOf(listOf(original, copy))

            override suspend fun summary(): List<CleanSummary> = emptyList()

            override suspend fun groups(category: CleanCategory): List<List<FileEntry>> = groups
        }
    private val trash =
        object : TrashRepository {
            val trashed = mutableListOf<String>()

            override suspend fun moveToTrash(path: String): String {
                trashed += path
                repository.groups = emptyList()
                return "id-$path"
            }

            override suspend fun items(): List<TrashedItem> = emptyList()

            override suspend fun restore(id: String): FileOperationResult = FileOperationResult.Done

            override suspend fun deleteForever(id: String): FileOperationResult =
                FileOperationResult.Done

            override fun isTrash(path: String): Boolean = false
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectAllKeepsTheOldestCopyOfEachDuplicate() = runTest {
        val viewModel =
            CleanListViewModel(CleanCategory.Duplicates, SavedStateHandle(), repository, trash)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.onEvent(CleanListEvent.SelectAll)

        assertEquals(setOf(copy.path), viewModel.state.value.selection)
        assertEquals(5L, viewModel.state.value.selectedBytes)
    }

    @Test
    fun deletingMovesTheSelectionToTrashAndReloads() = runTest {
        val viewModel =
            CleanListViewModel(CleanCategory.LargeFiles, SavedStateHandle(), repository, trash)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.onEvent(CleanListEvent.SelectAll)
        viewModel.onEvent(CleanListEvent.DeleteSelection)

        assertEquals(listOf(original.path, copy.path), trash.trashed)
        assertEquals(emptySet<String>(), viewModel.state.value.selection)
    }
}
