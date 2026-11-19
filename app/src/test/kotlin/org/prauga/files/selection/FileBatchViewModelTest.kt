// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.selection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.prauga.files.core.data.SafeFolderRepository
import org.prauga.files.core.data.TrashRepository
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.SafeItem
import org.prauga.files.core.model.TrashedItem
import org.prauga.files.core.model.UnlockResult

@OptIn(ExperimentalCoroutinesApi::class)
class FileBatchViewModelTest {
    private val trash =
        object : TrashRepository {
            override suspend fun moveToTrash(path: String): String? =
                if (path == "/gone") null else "id:$path"

            override suspend fun items(): List<TrashedItem> = emptyList()

            override suspend fun restore(id: String) = FileOperationResult.Done

            override suspend fun deleteForever(id: String) = FileOperationResult.Done

            override fun isTrash(path: String) = false
        }

    private fun safe(pin: Boolean) =
        object : SafeFolderRepository {
            override val hasPin: Flow<Boolean> = flowOf(pin)

            override suspend fun setPin(pin: String) = true

            override suspend fun unlock(pin: String): UnlockResult = UnlockResult.Unlocked

            override suspend fun items(): List<SafeItem> = emptyList()

            override suspend fun add(paths: List<String>) = FileOperationResult.Done

            override suspend fun moveOut(id: String, folder: String) = FileOperationResult.Done

            override suspend fun open(id: String): String? = null

            override suspend fun delete(id: String) = FileOperationResult.Done

            override suspend fun clearOpened() = Unit
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
    fun deletingReportsTrashedItemsAndFailures() = runTest {
        val viewModel = FileBatchViewModel(trash, safe(pin = true))
        val effects = mutableListOf<BatchEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.delete(listOf("/a", "/gone"))

        assertEquals(
            listOf(BatchEffect.Trashed(listOf("id:/a")), BatchEffect.Failed, BatchEffect.Changed),
            effects,
        )
    }

    @Test
    fun theSafeFolderNeedsAPin() = runTest {
        val viewModel = FileBatchViewModel(trash, safe(pin = false))
        val effects = mutableListOf<BatchEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }

        viewModel.moveToSafe(listOf("/a"))

        assertEquals(listOf<BatchEffect>(BatchEffect.SafeFolderNeedsSetup), effects)
    }
}
