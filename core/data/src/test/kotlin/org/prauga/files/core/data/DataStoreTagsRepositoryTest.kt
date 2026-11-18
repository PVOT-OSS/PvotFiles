// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.Tag
import org.prauga.files.core.model.TagColor

class DataStoreTagsRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun createsTagsSortedByNameAndRefusesDuplicates() = runTest {
        val repository = repository()

        assertEquals(FileOperationResult.Done, repository.createTag(Tag("Work", TagColor.Blue)))
        assertEquals(
            FileOperationResult.Done,
            repository.createTag(Tag("home: stuff", TagColor.Green)),
        )
        assertEquals(
            FileOperationResult.AlreadyExists,
            repository.createTag(Tag("work", TagColor.Red)),
        )
        assertEquals(FileOperationResult.InvalidName, repository.createTag(Tag(" ", TagColor.Red)))

        assertEquals(
            listOf(Tag("home: stuff", TagColor.Green), Tag("Work", TagColor.Blue)),
            repository.tags.first(),
        )
    }

    @Test
    fun tagsFilesAndListsTheOnesThatStillExist() = runTest {
        val repository = repository()
        repository.createTag(Tag("Work", TagColor.Blue))
        val report = File(folder.root, "report.pdf").apply { writeText("r") }
        val gone = File(folder.root, "gone.txt")

        repository.setTagged(report.path, "Work", tagged = true)
        repository.setTagged(gone.path, "Work", tagged = true)

        assertEquals(setOf("Work"), repository.tagsOf(report.path).first())
        assertEquals(listOf("report.pdf"), repository.entriesTagged("Work").map { it.name })

        repository.setTagged(report.path, "Work", tagged = false)
        assertEquals(emptySet<String>(), repository.tagsOf(report.path).first())
    }

    @Test
    fun deletingATagForgetsItsFiles() = runTest {
        val repository = repository()
        repository.createTag(Tag("Work", TagColor.Blue))
        val report = File(folder.root, "report.pdf").apply { writeText("r") }
        repository.setTagged(report.path, "Work", tagged = true)

        repository.deleteTag("Work")
        repository.createTag(Tag("Work", TagColor.Red))

        assertEquals(emptyList<String>(), repository.entriesTagged("Work").map { it.name })
    }

    private fun TestScope.repository() =
        DataStoreTagsRepository(
            PreferenceDataStoreFactory.create(scope = backgroundScope) {
                File(folder.root, "tags.preferences_pb")
            },
            StandardTestDispatcher(testScheduler),
        )
}
