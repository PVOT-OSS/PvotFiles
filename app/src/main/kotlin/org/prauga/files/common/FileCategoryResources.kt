// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.prauga.files.R
import org.prauga.files.core.model.FileCategory

@get:StringRes
val FileCategory.labelRes: Int
    get() =
        when (this) {
            FileCategory.Other -> R.string.category_other
            FileCategory.Videos -> R.string.category_videos
            FileCategory.Photos -> R.string.category_photos
            FileCategory.Audio -> R.string.category_audio
            FileCategory.Documents -> R.string.category_documents
            FileCategory.Apps -> R.string.category_apps
        }

@get:DrawableRes
val FileCategory.iconRes: Int
    get() =
        when (this) {
            FileCategory.Other -> R.drawable.ic_file
            FileCategory.Videos -> R.drawable.ic_video
            FileCategory.Photos -> R.drawable.ic_photo
            FileCategory.Audio -> R.drawable.ic_audio
            FileCategory.Documents -> R.drawable.ic_document
            FileCategory.Apps -> R.drawable.ic_android
        }
