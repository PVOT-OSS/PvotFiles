// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.access

import org.junit.Assert.assertEquals
import org.junit.Test

class AccessViewModelTest {
    @Test
    fun missingAccessIsRequired() {
        val viewModel = AccessViewModel(FakeStorageAccessChecker(granted = false))

        assertEquals(AccessUiState.Required, viewModel.state.value)
    }

    @Test
    fun refreshPicksUpGrantedAccess() {
        val checker = FakeStorageAccessChecker(granted = false)
        val viewModel = AccessViewModel(checker)

        checker.granted = true
        viewModel.onEvent(AccessEvent.Refresh)

        assertEquals(AccessUiState.Granted, viewModel.state.value)
    }

    private class FakeStorageAccessChecker(var granted: Boolean) : StorageAccessChecker {
        override fun isGranted(): Boolean = granted
    }
}
