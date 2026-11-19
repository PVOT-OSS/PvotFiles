// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.content.ContentResolver
import android.content.Context
import android.provider.OpenableColumns
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class LocalIncomingRepository
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val mediaIndex: MediaIndex,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : IncomingRepository {

    override suspend fun save(uris: List<String>, folder: String): Int =
        withContext(dispatcher) {
            val parent = File(folder).apply { mkdirs() }
            val saved =
                uris.mapNotNull { value ->
                    val uri = value.toUri()
                    if (uri.scheme != ContentResolver.SCHEME_CONTENT) return@mapNotNull null
                    val name = SafeName.of(nameOf(value))
                    val target =
                        File(parent, TransferEngine.uniqueName(parent, name, isDirectory = false))
                    try {
                        val input =
                            context.contentResolver.openInputStream(uri) ?: return@mapNotNull null
                        input.use { source -> target.outputStream().use { source.copyTo(it) } }
                        target.path
                    } catch (e: IOException) {
                        target.delete()
                        null
                    } catch (e: SecurityException) {
                        target.delete()
                        null
                    }
                }
            mediaIndex.update(saved)
            saved.size
        }

    private fun nameOf(uri: String): String? =
        try {
            context.contentResolver
                .query(uri.toUri(), arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { if (it.moveToFirst()) it.getString(0) else null }
        } catch (e: SecurityException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        } ?: uri.toUri().lastPathSegment
}
