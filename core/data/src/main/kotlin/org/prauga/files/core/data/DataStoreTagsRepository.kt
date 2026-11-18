// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.FileNames
import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.SortOrder
import org.prauga.files.core.model.Tag
import org.prauga.files.core.model.TagColor
import org.prauga.files.core.model.sortedFor

internal class DataStoreTagsRepository
@Inject
constructor(
    @TagsStore private val store: DataStore<Preferences>,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : TagsRepository {

    override val tags: Flow<List<Tag>> =
        store.data.map { preferences ->
            preferences[TAGS]
                .orEmpty()
                .mapNotNull(::decode)
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, Tag::name))
        }

    override fun tagsOf(path: String): Flow<Set<String>> =
        store.data.map { preferences ->
            preferences[TAGS]
                .orEmpty()
                .mapNotNull(::decode)
                .map { it.name }
                .filter { path in preferences[pathsKey(it)].orEmpty() }
                .toSet()
        }

    override suspend fun createTag(tag: Tag): FileOperationResult {
        if (!FileNames.isValid(tag.name)) return FileOperationResult.InvalidName
        var result: FileOperationResult = FileOperationResult.Done
        store.edit { preferences ->
            val current = preferences[TAGS].orEmpty().mapNotNull(::decode)
            if (current.any { it.name.equals(tag.name, ignoreCase = true) }) {
                result = FileOperationResult.AlreadyExists
            } else {
                preferences[TAGS] = preferences[TAGS].orEmpty() + encode(tag)
            }
        }
        return result
    }

    override suspend fun deleteTag(name: String) {
        store.edit { preferences ->
            preferences[TAGS] =
                preferences[TAGS].orEmpty().filterNot { decode(it)?.name == name }.toSet()
            preferences.remove(pathsKey(name))
        }
    }

    override suspend fun setTagged(path: String, tagName: String, tagged: Boolean) {
        store.edit { preferences ->
            val key = pathsKey(tagName)
            val current = preferences[key].orEmpty()
            preferences[key] = if (tagged) current + path else current - path
        }
    }

    override suspend fun entriesTagged(tagName: String): List<FileEntry> {
        val paths = store.data.first()[pathsKey(tagName)].orEmpty()
        return withContext(dispatcher) {
            paths.map(::File).filter { it.exists() }.map { it.toEntry() }.sortedFor(SortOrder.Name)
        }
    }

    private fun File.toEntry(): FileEntry =
        if (isDirectory) {
            FileEntry.Directory(
                path,
                name,
                lastModified(),
                listFiles()?.count { !it.isHidden } ?: 0,
            )
        } else {
            FileEntry.File(path, name, lastModified(), length())
        }

    private companion object {
        val TAGS = stringSetPreferencesKey("tags")

        fun pathsKey(name: String) = stringSetPreferencesKey("tagged/$name")

        fun encode(tag: Tag) = "${tag.color.name}:${tag.name}"

        fun decode(value: String): Tag? {
            val color =
                TagColor.entries.firstOrNull { it.name == value.substringBefore(':') }
                    ?: return null
            return Tag(value.substringAfter(':'), color)
        }
    }
}
