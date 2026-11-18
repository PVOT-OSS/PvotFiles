// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.tags

import com.prauga.pvot.coreui.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.prauga.files.core.data.TagsRepository
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.Tag

@OptIn(ExperimentalCoroutinesApi::class)
class TaggedViewModelTest {
    private val report = FileEntry.File("/storage/report.pdf", "report.pdf", 0L, sizeBytes = 1)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshListsWhatHasTheTag() = runTest {
        val viewModel = TaggedViewModel("Work", FakeTagsRepository(mapOf("Work" to listOf(report))))

        viewModel.onEvent(TaggedEvent.Refresh)

        assertEquals(UiState.Success(listOf(report)), viewModel.state.value)
    }

    @Test
    fun deletingRemovesThatTag() = runTest {
        val repository = FakeTagsRepository(emptyMap())
        val viewModel = TaggedViewModel("Work", repository)

        viewModel.onEvent(TaggedEvent.DeleteTag)

        assertEquals(listOf("Work"), repository.deleted)
    }

    private class FakeTagsRepository(private val entries: Map<String, List<FileEntry>>) :
        TagsRepository {
        val deleted = mutableListOf<String>()
        override val tags: Flow<List<Tag>> = flowOf(emptyList())

        override fun tagsOf(path: String): Flow<Set<String>> = flowOf(emptySet())

        override suspend fun createTag(tag: Tag): FileOperationResult = FileOperationResult.Done

        override suspend fun deleteTag(name: String) {
            deleted += name
        }

        override suspend fun setTagged(path: String, tagName: String, tagged: Boolean) = Unit

        override suspend fun entriesTagged(tagName: String): List<FileEntry> =
            entries[tagName].orEmpty()
    }
}
