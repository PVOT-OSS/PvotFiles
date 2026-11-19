// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.prauga.files.R
import org.prauga.files.core.model.FileEntry
import org.prauga.files.viewer.ViewerActivity

class FileLauncher(
    private val context: Context,
    private val scope: CoroutineScope,
    private val snackbar: SnackbarHostState,
    private val noAppMessage: String,
    private val unavailableMessage: String,
) {
    fun open(file: FileEntry.File) {
        if (ViewerActivity.canView(file.name)) {
            start(ViewerActivity.intent(context, file.path))
            return
        }
        start(viewIntent(file) ?: return)
    }

    fun openWith(file: FileEntry.File) {
        start(Intent.createChooser(viewIntent(file) ?: return, null))
    }

    fun share(file: FileEntry.File) {
        shareUri(uriFor(file) ?: return, mimeTypeOf(file))
    }

    fun shareAll(files: List<FileEntry.File>) {
        val uris = files.mapNotNull(::uriFor)
        if (uris.isEmpty()) return
        val send =
            Intent(Intent.ACTION_SEND_MULTIPLE)
                .setType(
                    if (files.map(::mimeTypeOf).distinct().size == 1) mimeTypeOf(files.first())
                    else "*/*"
                )
                .putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        start(Intent.createChooser(send, null))
    }

    fun openUri(uri: Uri, mimeType: String) {
        start(viewIntent(uri, mimeType))
    }

    fun shareUri(uri: Uri, mimeType: String) {
        val send =
            Intent(Intent.ACTION_SEND)
                .setType(mimeType)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        start(Intent.createChooser(send, null))
    }

    private fun viewIntent(file: FileEntry.File): Intent? =
        uriFor(file)?.let { viewIntent(it, mimeTypeOf(file)) }

    private fun viewIntent(uri: Uri, mimeType: String): Intent =
        Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    private fun uriFor(file: FileEntry.File): Uri? =
        try {
            FileProvider.getUriForFile(context, "${context.packageName}.files", File(file.path))
        } catch (e: IllegalArgumentException) {
            tell(unavailableMessage)
            null
        }

    private fun start(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            tell(noAppMessage)
        }
    }

    private fun tell(message: String) {
        scope.launch { snackbar.showSnackbar(message) }
    }

    private fun mimeTypeOf(file: FileEntry.File): String =
        MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.name.substringAfterLast('.', "").lowercase())
            ?: "application/octet-stream"
}

@Composable
fun rememberFileLauncher(snackbar: SnackbarHostState): FileLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val noApp = stringResource(R.string.open_no_app)
    val unavailable = stringResource(R.string.open_unavailable)
    return remember(context, scope, snackbar, noApp, unavailable) {
        FileLauncher(context, scope, snackbar, noApp, unavailable)
    }
}
