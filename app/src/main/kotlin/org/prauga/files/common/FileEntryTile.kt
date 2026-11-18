// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.prauga.pvot.designsystem.theme.PvotShapes
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.files.R
import org.prauga.files.core.model.FileEntry

@Composable
fun FileEntryTile(
    entry: FileEntry,
    onClick: (FileEntry) -> Unit,
    onLongClick: (FileEntry) -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val clickable =
        Modifier.combinedClickable(
            onClickLabel = stringResource(R.string.file_entry_open),
            onLongClick = { onLongClick(entry) },
            onClick = { onClick(entry) },
        )
    Column(
        modifier
            .clip(PvotShapes.Card)
            .then(
                if (selected)
                    Modifier.background(
                        MaterialTheme.colorScheme.primary.copy(alpha = SelectedAlpha)
                    )
                else Modifier
            )
            .then(clickable)
            .padding(PvotSpacing.S),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PvotSpacing.XS),
    ) {
        val icon: @Composable () -> Unit = {
            Icon(
                painterResource(
                    if (entry is FileEntry.Directory) R.drawable.ic_folder else R.drawable.ic_file
                ),
                contentDescription = null,
                modifier = Modifier.size(TileIconSize),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        when {
            selected ->
                Box(Modifier.size(TileIconSize), contentAlignment = Alignment.Center) {
                    SelectedMark()
                }
            entry is FileEntry.File -> FileThumbnail(entry, TileIconSize, icon)
            else -> icon()
        }
        Text(
            entry.name,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            entry.summary(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private val TileIconSize = 56.dp

private const val SelectedAlpha = 0.14f
