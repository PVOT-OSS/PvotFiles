// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.content.Context
import android.content.Intent
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.prauga.files.core.model.DocumentEntry
import org.prauga.files.core.model.SavedLocation

internal class SafDocumentTreeRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
    @LocationsStore private val store: DataStore<Preferences>,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : DocumentTreeRepository {

    override val locations: Flow<List<SavedLocation>> =
        store.data.map { preferences ->
            preferences[TREES].orEmpty().mapNotNull(SavedLocationCodec::decode).sortedBy {
                it.label.lowercase()
            }
        }

    override suspend fun add(treeUri: String): SavedLocation? =
        withContext(dispatcher) {
            val uri = treeUri.toUri()
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            } catch (e: SecurityException) {
                return@withContext null
            }
            val location = SavedLocation(treeUri, labelOf(treeUri) ?: uri.lastPathSegment.orEmpty())
            store.edit { preferences ->
                val others =
                    preferences[TREES].orEmpty().filterNot {
                        SavedLocationCodec.decode(it)?.uri == treeUri
                    }
                preferences[TREES] = others.toSet() + SavedLocationCodec.encode(location)
            }
            location
        }

    override suspend fun remove(treeUri: String) {
        withContext(dispatcher) {
            val held =
                context.contentResolver.persistedUriPermissions.any { it.uri.toString() == treeUri }
            if (held) {
                context.contentResolver.releasePersistableUriPermission(
                    treeUri.toUri(),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
        }
        store.edit { preferences ->
            preferences[TREES] =
                preferences[TREES]
                    .orEmpty()
                    .filterNot { SavedLocationCodec.decode(it)?.uri == treeUri }
                    .toSet()
        }
    }

    override suspend fun children(treeUri: String, documentId: String?): List<DocumentEntry>? =
        withContext(dispatcher) {
            val tree = treeUri.toUri()
            val parent = documentId ?: DocumentsContract.getTreeDocumentId(tree)
            val uri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parent)
            try {
                context.contentResolver.query(uri, COLUMNS, null, null, null)?.use { cursor ->
                    buildList {
                            while (cursor.moveToNext()) {
                                val mime = cursor.getString(2).orEmpty()
                                add(
                                    DocumentEntry(
                                        documentId = cursor.getString(0) ?: continue,
                                        name = cursor.getString(1).orEmpty(),
                                        mimeType = mime,
                                        isDirectory = mime == Document.MIME_TYPE_DIR,
                                        sizeBytes = if (cursor.isNull(3)) 0 else cursor.getLong(3),
                                        lastModifiedMillis =
                                            if (cursor.isNull(4)) 0 else cursor.getLong(4),
                                    )
                                )
                            }
                        }
                        .sortedWith(
                            compareBy<DocumentEntry> { !it.isDirectory }
                                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                        )
                }
            } catch (e: SecurityException) {
                null
            } catch (e: IllegalArgumentException) {
                null
            }
        }

    override fun documentUri(treeUri: String, documentId: String): String =
        DocumentsContract.buildDocumentUriUsingTree(treeUri.toUri(), documentId).toString()

    private fun labelOf(treeUri: String): String? {
        val tree = treeUri.toUri()
        val document =
            DocumentsContract.buildDocumentUriUsingTree(
                tree,
                DocumentsContract.getTreeDocumentId(tree),
            )
        return try {
            context.contentResolver
                .query(document, arrayOf(Document.COLUMN_DISPLAY_NAME), null, null, null)
                ?.use { if (it.moveToFirst()) it.getString(0) else null }
        } catch (e: SecurityException) {
            null
        }
    }

    private companion object {
        val TREES = stringSetPreferencesKey("trees")
        val COLUMNS =
            arrayOf(
                Document.COLUMN_DOCUMENT_ID,
                Document.COLUMN_DISPLAY_NAME,
                Document.COLUMN_MIME_TYPE,
                Document.COLUMN_SIZE,
                Document.COLUMN_LAST_MODIFIED,
            )
    }
}
