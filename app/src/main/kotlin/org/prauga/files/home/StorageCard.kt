// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.home

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.prauga.pvot.designsystem.components.PvotCard
import com.prauga.pvot.designsystem.components.progress.PvotProportionBar
import com.prauga.pvot.designsystem.components.progress.PvotProportionBarColors
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.files.R
import org.prauga.files.core.model.StorageUsage

@Composable
fun StorageCard(
    usage: StorageUsage,
    colors: PvotProportionBarColors,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    PvotCard(modifier) {
        Column(
            Modifier.padding(PvotSpacing.L),
            verticalArrangement = Arrangement.spacedBy(PvotSpacing.M),
        ) {
            Text(
                stringResource(R.string.home_storage_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                stringResource(
                    R.string.home_storage_used,
                    Formatter.formatShortFileSize(context, usage.usedBytes),
                    Formatter.formatShortFileSize(context, usage.totalBytes),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PvotProportionBar(
                segments =
                    usage.categories.map { it.sizeBytes.fractionOf(usage.totalBytes) } +
                        usage.otherUsedBytes.fractionOf(usage.totalBytes),
                modifier = Modifier.fillMaxWidth(),
                colors =
                    colors.copy(
                        segmentColors =
                            colors.segmentColors.take(usage.categories.size) +
                                MaterialTheme.colorScheme.outline
                    ),
            )
        }
    }
}

internal fun Long.fractionOf(total: Long): Float =
    if (total <= 0) 0f else (toDouble() / total).toFloat().coerceIn(0f, 1f)
