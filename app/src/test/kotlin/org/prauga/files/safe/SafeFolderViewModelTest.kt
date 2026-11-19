// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.safe

import com.prauga.pvot.coreui.UiState
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.prauga.files.core.data.SafeFolderRepository
import org.prauga.files.core.data.StorageRepository
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.SafeItem
import org.prauga.files.core.model.UnlockResult
import org.prauga.files.core.model.Volume

@OptIn(ExperimentalCoroutinesApi::class)
class SafeFolderViewModelTest {
    private var now = 0L
    private val clock =
        object : Clock() {
            override fun getZone() = ZoneOffset.UTC

            override fun withZone(zone: java.time.ZoneId?): Clock = this

            override fun instant(): Instant = Instant.ofEpochMilli(now)
        }
    private val item = SafeItem("a", "a.txt", 1, 0)
    private val repository =
        object : SafeFolderRepository {
            val pin = MutableStateFlow(false)
            var stored = "1234"
            override val hasPin: Flow<Boolean> = pin

            override suspend fun setPin(pin: String): Boolean {
                stored = pin
                this.pin.value = true
                return true
            }

            override suspend fun unlock(pin: String): UnlockResult =
                if (pin == stored) UnlockResult.Unlocked else UnlockResult.Wrong(4)

            override suspend fun items(): List<SafeItem> = listOf(item)

            override suspend fun add(paths: List<String>) = FileOperationResult.Done

            override suspend fun moveOut(id: String, folder: String) = FileOperationResult.Done

            override suspend fun open(id: String): String? = null

            override suspend fun delete(id: String) = FileOperationResult.Done

            override suspend fun clearOpened() = Unit
        }
    private val storage =
        object : StorageRepository {
            override fun primaryRootPath(): String = "/storage"

            override fun volumes(): List<Volume> = emptyList()
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
    fun setupRejectsMismatchedPinsThenUnlocks() = runTest {
        val viewModel = SafeFolderViewModel(repository, storage, clock)
        assertEquals(SafeUiState.Setup(), viewModel.state.value)

        viewModel.onEvent(SafeEvent.CreatePin("1234", "1243"))
        assertEquals(SafeUiState.Setup(mismatch = true), viewModel.state.value)

        viewModel.onEvent(SafeEvent.CreatePin("1234", "1234"))
        assertEquals(SafeUiState.Unlocked(UiState.Success(listOf(item))), viewModel.state.value)
    }

    @Test
    fun aWrongPinStaysLockedAndAWaitAwayLocksAgain() = runTest {
        repository.pin.value = true
        val viewModel = SafeFolderViewModel(repository, storage, clock)

        viewModel.onEvent(SafeEvent.Unlock("0000"))
        assertEquals(SafeUiState.Locked(attemptsLeft = 4), viewModel.state.value)

        viewModel.onEvent(SafeEvent.Unlock("1234"))
        viewModel.onEvent(SafeEvent.Stopped)
        now += 61_000
        viewModel.onEvent(SafeEvent.Started)
        assertEquals(SafeUiState.Locked(), viewModel.state.value)
    }
}
