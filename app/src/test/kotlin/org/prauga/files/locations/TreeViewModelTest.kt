// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.locations

import androidx.lifecycle.SavedStateHandle
import com.prauga.pvot.coreui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
import org.prauga.files.core.data.DocumentTreeRepository
import org.prauga.files.core.model.DocumentEntry
import org.prauga.files.core.model.SavedLocation

@OptIn(ExperimentalCoroutinesApi::class)
class TreeViewModelTest {
    private val location = SavedLocation("content://drive/tree/root", "My Drive")
    private val work = DocumentEntry("work", "Work", "vnd.android.document/directory", true, 0, 0)
    private val report = DocumentEntry("report", "report.pdf", "application/pdf", false, 10, 0)
    private val repository =
        FakeTreeRepository(mapOf(null to listOf(work), "work" to listOf(report)))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun opensFoldersAndGoesBackUp() = runTest {
        val viewModel = TreeViewModel(location, SavedStateHandle(), repository)
        collect(viewModel)
        assertEquals("My Drive", viewModel.state.value.title)
        assertFalse(viewModel.state.value.canGoUp)

        viewModel.onEvent(TreeEvent.Open(work))
        assertEquals("Work", viewModel.state.value.title)
        assertTrue(viewModel.state.value.canGoUp)
        assertEquals(UiState.Success(listOf(report)), viewModel.state.value.content)

        viewModel.onEvent(TreeEvent.Up)
        assertEquals(UiState.Success(listOf(work)), viewModel.state.value.content)
    }

    @Test
    fun theOpenFolderSurvivesProcessDeath() = runTest {
        val saved = SavedStateHandle()
        TreeViewModel(location, saved, repository).onEvent(TreeEvent.Open(work))

        val restored = TreeViewModel(location, saved, repository)
        collect(restored)

        assertEquals("Work", restored.state.value.title)
    }

    @Test
    fun anUnreadableLocationIsAnError() = runTest {
        val viewModel = TreeViewModel(location, SavedStateHandle(), FakeTreeRepository(emptyMap()))
        collect(viewModel)

        assertTrue(viewModel.state.value.content is UiState.Error)
    }

    private fun TestScope.collect(viewModel: TreeViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
    }

    private class FakeTreeRepository(private val children: Map<String?, List<DocumentEntry>>) :
        DocumentTreeRepository {
        override val locations: Flow<List<SavedLocation>> = flowOf(emptyList())

        override suspend fun add(treeUri: String): SavedLocation? = null

        override suspend fun remove(treeUri: String) = Unit

        override suspend fun children(treeUri: String, documentId: String?): List<DocumentEntry>? =
            children[documentId]

        override fun documentUri(treeUri: String, documentId: String): String =
            "$treeUri/$documentId"
    }
}
