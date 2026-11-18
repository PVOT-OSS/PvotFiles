// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.prauga.pvot.designsystem.theme.PvotShapes
import dagger.hilt.android.EntryPointAccessors
import org.prauga.files.core.model.FileEntry

@Composable
fun FileThumbnail(file: FileEntry.File, size: Dp, fallback: @Composable () -> Unit) {
    val context = LocalContext.current
    val thumbnails =
        remember(context) {
            EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    ThumbnailEntryPoint::class.java,
                )
                .thumbnails()
        }
    if (!thumbnails.hasThumbnail(file)) {
        fallback()
        return
    }
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val image by
        produceState<ImageBitmap?>(null, file.path, file.lastModifiedMillis, sizePx) {
            value = thumbnails.thumbnail(file, sizePx)?.asImageBitmap()
        }
    val loaded = image
    if (loaded == null) {
        fallback()
    } else {
        Image(
            bitmap = loaded,
            contentDescription = null,
            modifier = Modifier.size(size).clip(PvotShapes.Small),
            contentScale = ContentScale.Crop,
        )
    }
}
