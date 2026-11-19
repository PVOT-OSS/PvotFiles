// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.clean

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.prauga.files.R
import org.prauga.files.core.model.CleanCategory

@get:StringRes
val CleanCategory.titleRes: Int
    get() =
        when (this) {
            CleanCategory.LargeFiles -> R.string.clean_large
            CleanCategory.Duplicates -> R.string.clean_duplicates
            CleanCategory.OldScreenshots -> R.string.clean_screenshots
            CleanCategory.OldDownloads -> R.string.clean_downloads
            CleanCategory.EmptyFolders -> R.string.clean_empty_folders
        }

@get:DrawableRes
val CleanCategory.iconRes: Int
    get() =
        when (this) {
            CleanCategory.LargeFiles -> R.drawable.ic_file
            CleanCategory.Duplicates -> R.drawable.ic_copy
            CleanCategory.OldScreenshots -> R.drawable.ic_photo
            CleanCategory.OldDownloads -> R.drawable.ic_download
            CleanCategory.EmptyFolders -> R.drawable.ic_folder
        }
