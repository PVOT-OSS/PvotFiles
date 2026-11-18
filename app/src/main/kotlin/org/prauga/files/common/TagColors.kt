// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.prauga.pvot.designsystem.theme.PvotTheme
import org.prauga.files.R
import org.prauga.files.core.model.TagColor

@Composable
fun TagColor.color(): Color =
    when (this) {
        TagColor.Red -> MaterialTheme.colorScheme.error
        TagColor.Yellow -> PvotTheme.extendedColors.warning
        TagColor.Green -> PvotTheme.extendedColors.success
        TagColor.Blue -> MaterialTheme.colorScheme.primary
        TagColor.Purple -> MaterialTheme.colorScheme.tertiary
        TagColor.Grey -> MaterialTheme.colorScheme.outline
    }

@get:StringRes
val TagColor.labelRes: Int
    get() =
        when (this) {
            TagColor.Red -> R.string.tag_color_red
            TagColor.Yellow -> R.string.tag_color_yellow
            TagColor.Green -> R.string.tag_color_green
            TagColor.Blue -> R.string.tag_color_blue
            TagColor.Purple -> R.string.tag_color_purple
            TagColor.Grey -> R.string.tag_color_grey
        }

@Composable
fun TagDot(color: TagColor, modifier: Modifier = Modifier, size: Dp = 12.dp) {
    Box(modifier.size(size).background(color.color(), CircleShape))
}
