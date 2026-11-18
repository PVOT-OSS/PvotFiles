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

class FileLauncher(
    private val context: Context,
    private val scope: CoroutineScope,
    private val snackbar: SnackbarHostState,
    private val noAppMessage: String,
    private val unavailableMessage: String,
) {
    fun open(file: FileEntry.File) {
        val uri = uriFor(file) ?: return
        start(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, mimeTypeOf(file))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

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
