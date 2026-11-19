// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import android.content.Context
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.components.list.PvotListRowDefaults
import org.prauga.files.R
import org.prauga.files.core.model.FileEntry

@Composable
fun FileEntryRow(
    entry: FileEntry,
    onClick: (FileEntry) -> Unit,
    onLongClick: (FileEntry) -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onMore: ((FileEntry) -> Unit)? = null,
) {
    PvotListRow(
        title = entry.name,
        modifier = modifier,
        supportingText = entry.summary(),
        leading = {
            when {
                selected -> SelectedMark()
                entry is FileEntry.File ->
                    FileThumbnail(entry, PvotListRowDefaults.LeadingSize) {
                        FileKindIcon(entry.name)
                    }
                else -> EntryIcon(R.drawable.ic_folder)
            }
        },
        trailing =
            onMore?.let { more ->
                {
                    IconButton(onClick = { more(entry) }) {
                        Icon(
                            painterResource(R.drawable.ic_more),
                            contentDescription = stringResource(R.string.more_actions),
                        )
                    }
                }
            },
        onClick = { onClick(entry) },
        onLongClick = { onLongClick(entry) },
        onClickLabel = stringResource(R.string.file_entry_open),
        selected = selected,
    )
}

@Composable
fun SelectedMark() {
    Icon(
        painterResource(R.drawable.ic_check),
        contentDescription = stringResource(R.string.entry_selected),
        tint = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun FileEntry.summary(): String =
    when (this) {
        is FileEntry.Directory ->
            pluralStringResource(R.plurals.file_entry_folder_items, childCount, childCount)
        is FileEntry.File -> {
            val context = LocalContext.current
            stringResource(
                R.string.file_entry_details,
                context.formatModified(lastModifiedMillis),
                Formatter.formatShortFileSize(context, sizeBytes),
            )
        }
    }

@Composable
private fun EntryIcon(@DrawableRes icon: Int) {
    Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
}

private fun Context.formatModified(millis: Long): String {
    val flags =
        if (DateUtils.isToday(millis)) DateUtils.FORMAT_SHOW_TIME
        else DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH
    return DateUtils.formatDateTime(this, millis, flags)
}
