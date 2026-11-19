// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.viewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.prauga.pvot.designsystem.platform.PvotBaseActivity
import com.prauga.pvot.designsystem.theme.PvotAppTheme
import dagger.hilt.android.AndroidEntryPoint
import org.prauga.files.core.model.FileKind

@AndroidEntryPoint
class ViewerActivity : PvotBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPvotContent { PvotAppTheme { ViewerScreen(onClose = ::finish) } }
    }

    companion object {
        const val EXTRA_PATH = "path"

        private val supported =
            setOf(FileKind.Image, FileKind.Pdf, FileKind.Text, FileKind.Video, FileKind.Audio)

        fun canView(name: String): Boolean = FileKind.of(name) in supported

        fun intent(context: Context, path: String): Intent =
            Intent(context, ViewerActivity::class.java).putExtra(EXTRA_PATH, path)
    }
}
