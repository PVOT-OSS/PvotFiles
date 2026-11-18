// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.category

import com.prauga.pvot.coreui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.prauga.files.core.data.CategoryFilesRepository
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.FileEntry

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryViewModelTest {
    private val photo = FileEntry.File("/storage/DCIM/a.jpg", "a.jpg", 0L, sizeBytes = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun listsTheFilesOfItsCategory() = runTest {
        val repository = FakeCategoryFilesRepository(mapOf(FileCategory.Photos to listOf(photo)))
        val viewModel = CategoryViewModel(FileCategory.Photos, repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        assertEquals(UiState.Success(listOf(photo)), viewModel.state.value)
        assertEquals(listOf(FileCategory.Photos), repository.requested)
    }

    @Test
    fun unavailableFilesAreAnErrorAndRetryReloads() = runTest {
        val repository = FakeCategoryFilesRepository(emptyMap())
        val viewModel = CategoryViewModel(FileCategory.Audio, repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
        assertTrue(viewModel.state.value is UiState.Error)

        repository.files = mapOf(FileCategory.Audio to emptyList())
        viewModel.onEvent(CategoryEvent.Retry)

        assertEquals(UiState.Success(emptyList<FileEntry.File>()), viewModel.state.value)
    }

    private class FakeCategoryFilesRepository(var files: Map<FileCategory, List<FileEntry.File>>) :
        CategoryFilesRepository {
        val requested = mutableListOf<FileCategory>()

        override suspend fun files(category: FileCategory): List<FileEntry.File>? {
            requested += category
            return files[category]
        }
    }
}
