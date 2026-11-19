// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import org.prauga.files.R
import org.prauga.files.core.model.FileKind

@Composable
fun FileKindIcon(name: String, size: Dp? = null) {
    val kind = FileKind.of(name)
    val tint = kind.colorRes?.let { colorResource(it) } ?: MaterialTheme.colorScheme.primary
    Icon(
        painterResource(kind.iconRes),
        contentDescription = null,
        modifier = if (size != null) Modifier.size(size) else Modifier,
        tint = tint,
    )
}

@get:DrawableRes
private val FileKind.iconRes: Int
    get() =
        when (this) {
            FileKind.Pdf,
            FileKind.Document,
            FileKind.Spreadsheet,
            FileKind.Presentation,
            FileKind.Web,
            FileKind.Text -> R.drawable.ic_document
            FileKind.Archive -> R.drawable.ic_archive
            FileKind.App -> R.drawable.ic_android
            FileKind.Image -> R.drawable.ic_photo
            FileKind.Video -> R.drawable.ic_video
            FileKind.Audio -> R.drawable.ic_audio
            FileKind.Other -> R.drawable.ic_file
        }

@get:ColorRes
private val FileKind.colorRes: Int?
    get() =
        when (this) {
            FileKind.Pdf -> R.color.kind_pdf
            FileKind.Document -> R.color.kind_document
            FileKind.Spreadsheet -> R.color.kind_spreadsheet
            FileKind.Presentation -> R.color.kind_presentation
            FileKind.Web -> R.color.kind_web
            FileKind.Text -> R.color.kind_text
            FileKind.Archive,
            FileKind.App,
            FileKind.Image,
            FileKind.Video,
            FileKind.Audio,
            FileKind.Other -> null
        }
