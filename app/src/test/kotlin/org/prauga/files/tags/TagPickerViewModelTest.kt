// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.tags

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
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
import org.prauga.files.core.model.TagColor

@OptIn(ExperimentalCoroutinesApi::class)
class TagPickerViewModelTest {
    private val work = Tag("Work", TagColor.Blue)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun togglingTagsAndUntagsTheShownItem() = runTest {
        val repository = FakeTagsRepository(listOf(work))
        val viewModel = TagPickerViewModel(SavedStateHandle(), repository)
        collect(viewModel)

        viewModel.onEvent(TagPickerEvent.Show(listOf("/storage/a.pdf")))
        viewModel.onEvent(TagPickerEvent.Toggle("Work"))
        assertEquals(setOf("Work"), viewModel.state.value.selected)

        viewModel.onEvent(TagPickerEvent.Toggle("Work"))
        assertEquals(emptySet<String>(), viewModel.state.value.selected)
    }

    @Test
    fun aNewTagIsAppliedToTheShownItem() = runTest {
        val repository = FakeTagsRepository(emptyList())
        val viewModel = TagPickerViewModel(SavedStateHandle(), repository)
        collect(viewModel)

        viewModel.onEvent(TagPickerEvent.Show(listOf("/storage/a.pdf")))
        viewModel.onEvent(TagPickerEvent.Create(work))

        assertEquals(listOf(work), viewModel.state.value.tags)
        assertEquals(setOf("Work"), viewModel.state.value.selected)
    }

    @Test
    fun severalItemsShareOnlyTheTagsAllOfThemHave() = runTest {
        val repository = FakeTagsRepository(listOf(work))
        val viewModel = TagPickerViewModel(SavedStateHandle(), repository)
        collect(viewModel)
        viewModel.onEvent(TagPickerEvent.Show(listOf("/storage/a.pdf")))
        viewModel.onEvent(TagPickerEvent.Toggle("Work"))

        viewModel.onEvent(TagPickerEvent.Show(listOf("/storage/a.pdf", "/storage/b.pdf")))
        assertEquals(emptySet<String>(), viewModel.state.value.selected)

        viewModel.onEvent(TagPickerEvent.Toggle("Work"))
        assertEquals(setOf("Work"), viewModel.state.value.selected)
    }

    @Test
    fun aDuplicateTagIsReported() = runTest {
        val repository = FakeTagsRepository(listOf(work))
        val viewModel = TagPickerViewModel(SavedStateHandle(), repository)
        collect(viewModel)
        val failures = mutableListOf<FileOperationResult>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.failures.collect { failures += it }
        }

        viewModel.onEvent(TagPickerEvent.Create(Tag("Work", TagColor.Red)))

        assertEquals(listOf(FileOperationResult.AlreadyExists), failures)
    }

    private fun TestScope.collect(viewModel: TagPickerViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }
    }

    private class FakeTagsRepository(initial: List<Tag>) : TagsRepository {
        private val allTags = MutableStateFlow(initial)
        private val tagged = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
        override val tags: Flow<List<Tag>> = allTags

        override fun tagsOf(path: String): Flow<Set<String>> =
            tagged.map { all -> all.filterValues { path in it }.keys }

        override suspend fun createTag(tag: Tag): FileOperationResult {
            if (allTags.value.any { it.name == tag.name }) return FileOperationResult.AlreadyExists
            allTags.update { it + tag }
            return FileOperationResult.Done
        }

        override suspend fun deleteTag(name: String) = Unit

        override suspend fun setTagged(path: String, tagName: String, tagged: Boolean) {
            this.tagged.update { all ->
                val paths = all[tagName].orEmpty()
                all + (tagName to if (tagged) paths + path else paths - path)
            }
        }

        override suspend fun entriesTagged(tagName: String): List<FileEntry> = emptyList()
    }
}
