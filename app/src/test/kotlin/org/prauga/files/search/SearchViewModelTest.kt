// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.search

import androidx.lifecycle.SavedStateHandle
import com.prauga.pvot.coreui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.prauga.files.core.data.SearchHistoryRepository
import org.prauga.files.core.data.SearchRepository
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.SearchFilters

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val report =
        FileEntry.File("/storage/Documents/report.pdf", "report.pdf", 0L, sizeBytes = 1)

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun aBlankQueryShowsNoResults() = runTest {
        val viewModel = viewModel(FakeSearchRepository())
        collect(viewModel)

        viewModel.onEvent(SearchEvent.QueryChanged("   "))
        advanceTimeBy(500)

        assertNull(viewModel.state.value.results)
    }

    @Test
    fun typingSearchesOnlyTheLastQueryAfterAPause() = runTest {
        val repository = FakeSearchRepository(mapOf("rep" to listOf(report)))
        val viewModel = viewModel(repository)
        collect(viewModel)

        viewModel.onEvent(SearchEvent.QueryChanged("r"))
        viewModel.onEvent(SearchEvent.QueryChanged("re"))
        viewModel.onEvent(SearchEvent.QueryChanged("rep "))
        advanceTimeBy(500)

        assertEquals(listOf("rep"), repository.queries)
        assertEquals(UiState.Success(listOf(report)), viewModel.state.value.results)
        assertEquals("rep ", viewModel.state.value.query)
    }

    @Test
    fun unavailableStorageIsAnErrorAndRetrySearchesAgain() = runTest {
        val repository = FakeSearchRepository(available = false)
        val viewModel = viewModel(repository)
        collect(viewModel)
        viewModel.onEvent(SearchEvent.QueryChanged("rep"))
        advanceTimeBy(500)
        assertTrue(viewModel.state.value.results is UiState.Error)

        viewModel.onEvent(SearchEvent.Retry)
        advanceTimeBy(500)

        assertEquals(listOf("rep", "rep"), repository.queries)
    }

    @Test
    fun clearingEmptiesTheQuery() = runTest {
        val viewModel = viewModel(FakeSearchRepository())
        collect(viewModel)
        viewModel.onEvent(SearchEvent.QueryChanged("rep"))

        viewModel.onEvent(SearchEvent.Clear)
        advanceTimeBy(500)

        assertEquals("", viewModel.state.value.query)
        assertNull(viewModel.state.value.results)
    }

    private fun TestScope.viewModel(repository: SearchRepository): SearchViewModel {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return SearchViewModel(SavedStateHandle(), repository, history)
    }

    @Test
    fun aFilterAloneSearchesWithoutAQuery() = runTest {
        val repository = FakeSearchRepository(mapOf("" to listOf(report)))
        val viewModel = viewModel(repository)
        collect(viewModel)

        viewModel.onEvent(SearchEvent.CategoryChanged(FileCategory.Documents))
        advanceTimeBy(500)

        assertEquals(SearchFilters(category = FileCategory.Documents), repository.filters.last())
        assertEquals(UiState.Success(listOf(report)), viewModel.state.value.results)
    }

    @Test
    fun aFolderScopeAloneSearchesThatFolder() = runTest {
        val repository = FakeSearchRepository(mapOf("" to listOf(report)))
        val viewModel = viewModel(repository)
        collect(viewModel)

        viewModel.onEvent(SearchEvent.FolderChanged("/storage/Download"))
        advanceTimeBy(500)

        assertEquals(SearchFilters(folder = "/storage/Download"), repository.filters.last())
        assertEquals(UiState.Success(listOf(report)), viewModel.state.value.results)
    }

    @Test
    fun openingAResultRemembersTheQuery() = runTest {
        val viewModel = viewModel(FakeSearchRepository())
        collect(viewModel)

        viewModel.onEvent(SearchEvent.QueryChanged("trip"))
        viewModel.onEvent(SearchEvent.ResultOpened)
        advanceTimeBy(500)
        assertEquals(listOf("trip"), viewModel.state.value.history)

        viewModel.onEvent(SearchEvent.ClearHistory)
        advanceTimeBy(500)
        assertEquals(emptyList<String>(), viewModel.state.value.history)
    }

    private val history =
        object : SearchHistoryRepository {
            val saved = MutableStateFlow(emptyList<String>())
            override val queries: Flow<List<String>> = saved

            override suspend fun add(query: String) {
                saved.value = listOf(query) + saved.value
            }

            override suspend fun clear() {
                saved.value = emptyList()
            }
        }

    private fun TestScope.collect(viewModel: SearchViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
    }

    private class FakeSearchRepository(
        private val results: Map<String, List<FileEntry.File>> = emptyMap(),
        private val available: Boolean = true,
    ) : SearchRepository {
        val queries = mutableListOf<String>()
        val filters = mutableListOf<SearchFilters>()

        override suspend fun search(query: String, filters: SearchFilters): List<FileEntry.File>? {
            this.filters += filters
            queries += query
            return if (available) results[query].orEmpty() else null
        }
    }
}
