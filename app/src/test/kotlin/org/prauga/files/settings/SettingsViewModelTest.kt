// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.prauga.files.core.data.DisplayOptionsRepository
import org.prauga.files.core.model.DisplayOptions

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun theSwitchChangesTheSharedDisplayOptions() = runTest {
        val repository = FakeDisplayOptionsRepository()
        val viewModel = SettingsViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.setShowHidden(true)

        assertTrue(viewModel.state.value.showHidden)
        assertTrue(repository.state.value.showHidden)
    }

    private class FakeDisplayOptionsRepository : DisplayOptionsRepository {
        val state = MutableStateFlow(DisplayOptions())
        override val options: Flow<DisplayOptions> = state

        override suspend fun update(transform: (DisplayOptions) -> DisplayOptions) {
            state.update(transform)
        }
    }
}
