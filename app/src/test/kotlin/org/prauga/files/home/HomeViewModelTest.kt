// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.home

import com.prauga.pvot.coreui.UiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.prauga.files.core.data.StorageUsageRepository
import org.prauga.files.core.model.CategoryUsage
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.StorageUsage

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
        val viewModel = HomeViewModel(FakeStorageUsageRepository { usage })

        viewModel.onEvent(HomeEvent.Refresh)

        assertEquals(UiState.Success(usage), viewModel.state.value)
    }

    @Test
    fun unavailableStorageIsAnError() = runTest {
        val viewModel = HomeViewModel(FakeStorageUsageRepository { null })

        viewModel.onEvent(HomeEvent.Refresh)

        assertTrue(viewModel.state.value is UiState.Error)
    }

    @Test
    fun refreshingKeepsTheLastUsageOnScreen() = runTest {
        val pending = CompletableDeferred<StorageUsage?>()
        var calls = 0
        val viewModel =
            HomeViewModel(
                FakeStorageUsageRepository { if (calls++ == 0) usage else pending.await() }
            )
        viewModel.onEvent(HomeEvent.Refresh)

        viewModel.onEvent(HomeEvent.Refresh)
        assertEquals(UiState.Success(usage), viewModel.state.value)

        val updated = usage.copy(freeBytes = 30)
        pending.complete(updated)
        assertEquals(UiState.Success(updated), viewModel.state.value)
    }

    private class FakeStorageUsageRepository(private val measure: suspend () -> StorageUsage?) :
        StorageUsageRepository {
        override suspend fun measure(): StorageUsage? = measure.invoke()
    }
}
