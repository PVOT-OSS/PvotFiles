// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.viewer

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.prauga.pvot.designsystem.platform.PvotBaseActivity
import dagger.hilt.android.AndroidEntryPoint
import org.prauga.files.FilesTheme
import org.prauga.files.core.model.FileKind

@AndroidEntryPoint
class ViewerActivity : PvotBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val data = intent.data
        if (intent.action == Intent.ACTION_VIEW && data?.scheme == ContentResolver.SCHEME_CONTENT) {
            intent.putExtra(EXTRA_URI, data.toString())
        }
        super.onCreate(savedInstanceState)
        setPvotContent { FilesTheme { ViewerScreen(onClose = ::finish) } }
    }

    companion object {
        const val EXTRA_PATH = "path"
        const val EXTRA_URI = "uri"

        private val supported =
            setOf(
                FileKind.Image,
                FileKind.Pdf,
                FileKind.Text,
                FileKind.Video,
                FileKind.Audio,
                FileKind.Archive,
            )

        fun canView(name: String): Boolean {
            val kind = FileKind.of(name)
            return kind in supported &&
                (kind != FileKind.Archive || name.endsWith(".zip", ignoreCase = true))
        }

        fun intent(context: Context, path: String): Intent =
            Intent(context, ViewerActivity::class.java).putExtra(EXTRA_PATH, path)
    }
}
