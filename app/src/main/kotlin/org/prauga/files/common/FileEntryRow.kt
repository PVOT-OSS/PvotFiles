// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import android.content.Context
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.list.PvotListRow
import org.prauga.files.R
import org.prauga.files.core.model.FileEntry

@Composable
fun FileEntryRow(
    entry: FileEntry,
    onOpenDirectory: (FileEntry.Directory) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (entry) {
        is FileEntry.Directory ->
            PvotListRow(
                title = entry.name,
                modifier = modifier,
                supportingText =
                    pluralStringResource(
                        R.plurals.file_entry_folder_items,
                        entry.childCount,
                        entry.childCount,
                    ),
                leading = { EntryIcon(R.drawable.ic_folder) },
                onClick = { onOpenDirectory(entry) },
                onClickLabel = stringResource(R.string.file_entry_open),
            )
        is FileEntry.File -> {
            val context = LocalContext.current
            PvotListRow(
                title = entry.name,
                modifier = modifier,
                supportingText =
                    stringResource(
                        R.string.file_entry_details,
                        context.formatModified(entry.lastModifiedMillis),
                        Formatter.formatShortFileSize(context, entry.sizeBytes),
                    ),
                leading = { EntryIcon(R.drawable.ic_file) },
            )
        }
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
