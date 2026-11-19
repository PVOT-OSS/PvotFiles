// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.viewer

import android.text.format.Formatter
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.button.PvotButton
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import org.prauga.files.R
import org.prauga.files.common.FileKindIcon

@Composable
fun ArchiveContent(content: ViewerContent.Archive, padding: PaddingValues, onExtract: () -> Unit) {
    val context = LocalContext.current
    PvotScreen(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PvotScreenDefaults.contentPadding(top = padding.calculateTopPadding()),
    ) {
        item {
            PvotButton(
                text =
                    stringResource(
                        if (content.toDownloads) R.string.viewer_extract_downloads
                        else R.string.viewer_extract_here
                    ),
                onClick = onExtract,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            PvotListGroup(
                Modifier.fillMaxWidth(),
                title =
                    pluralStringResource(
                        R.plurals.viewer_archive_count,
                        content.entries.size,
                        content.entries.size,
                    ),
            ) {
                content.entries.forEach { entry ->
                    item {
                        PvotListRow(
                            title = entry.path.trimEnd('/').substringAfterLast('/'),
                            modifier = Modifier.fillMaxWidth(),
                            supportingText =
                                if (entry.isDirectory) entry.path
                                else
                                    Formatter.formatShortFileSize(context, entry.sizeBytes) +
                                        " · " +
                                        entry.path,
                            leading = {
                                if (entry.isDirectory) {
                                    Icon(
                                        painterResource(R.drawable.ic_folder),
                                        contentDescription = null,
                                    )
                                } else {
                                    FileKindIcon(entry.path)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
