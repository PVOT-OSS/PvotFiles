// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.home

import android.text.format.Formatter
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.components.progress.PvotLinearProgress
import com.prauga.pvot.designsystem.components.progress.PvotLinearProgressColors
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.files.R
import org.prauga.files.core.model.CategoryUsage
import org.prauga.files.core.model.FileCategory

@Composable
fun CategoryRow(usage: CategoryUsage, totalBytes: Long, color: Color, trackColor: Color) {
    val context = LocalContext.current
    PvotListRow(
        title = { Text(stringResource(usage.category.labelRes)) },
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            Column(verticalArrangement = Arrangement.spacedBy(PvotSpacing.XS)) {
                Text(
                    stringResource(
                        R.string.home_category_details,
                        Formatter.formatShortFileSize(context, usage.sizeBytes),
                        pluralStringResource(
                            R.plurals.home_file_count,
                            usage.fileCount,
                            usage.fileCount,
                        ),
                    )
                )
                PvotLinearProgress(
                    progress = { usage.sizeBytes.fractionOf(totalBytes) },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        PvotLinearProgressColors(indicatorColor = color, trackColor = trackColor),
                )
            }
        },
        leading = {
            Icon(painterResource(usage.category.iconRes), contentDescription = null, tint = color)
        },
    )
}

@get:StringRes
internal val FileCategory.labelRes: Int
    get() =
        when (this) {
            FileCategory.Other -> R.string.category_other
            FileCategory.Videos -> R.string.category_videos
            FileCategory.Photos -> R.string.category_photos
            FileCategory.Audio -> R.string.category_audio
        }

@get:DrawableRes
internal val FileCategory.iconRes: Int
    get() =
        when (this) {
            FileCategory.Other -> R.drawable.ic_file
            FileCategory.Videos -> R.drawable.ic_video
            FileCategory.Photos -> R.drawable.ic_photo
            FileCategory.Audio -> R.drawable.ic_audio
        }
