// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.prauga.pvot.designsystem.components.PvotSegmentedControl
import com.prauga.pvot.designsystem.components.list.PvotSwitchRow
import com.prauga.pvot.designsystem.theme.PvotSpacing
import org.prauga.files.R
import org.prauga.files.common.FilesBottomSheet
import org.prauga.files.core.model.DisplayOptions
import org.prauga.files.core.model.SortOrder
import org.prauga.files.core.model.ViewLayout

@Composable
fun ViewOptionsSheet(
    visible: Boolean,
    options: DisplayOptions,
    onDismiss: () -> Unit,
    onEvent: (BrowseEvent) -> Unit,
) {
    FilesBottomSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.options_title),
    ) {
        SectionTitle(stringResource(R.string.options_sort))
        PvotSegmentedControl(
            options = SortOrder.entries.map { stringResource(it.labelRes) },
            selectedIndex = options.sortOrder.ordinal,
            onSelect = { onEvent(BrowseEvent.SetSortOrder(SortOrder.entries[it])) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = PvotSpacing.L),
        )
        SectionTitle(stringResource(R.string.options_layout))
        PvotSegmentedControl(
            options = ViewLayout.entries.map { stringResource(it.labelRes) },
            selectedIndex = options.layout.ordinal,
            onSelect = { onEvent(BrowseEvent.SetLayout(ViewLayout.entries[it])) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = PvotSpacing.L),
        )
        PvotSwitchRow(
            title = stringResource(R.string.options_show_hidden),
            checked = options.showHidden,
            onCheckedChange = { onEvent(BrowseEvent.SetShowHidden(it)) },
            modifier = Modifier.fillMaxWidth().padding(top = PvotSpacing.M),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier.padding(
                    start = PvotSpacing.L,
                    end = PvotSpacing.L,
                    top = PvotSpacing.M,
                    bottom = PvotSpacing.S,
                )
                .semantics { heading() },
    )
}

private val SortOrder.labelRes: Int
    get() =
        when (this) {
            SortOrder.Name -> R.string.sort_name
            SortOrder.Modified -> R.string.sort_modified
            SortOrder.Size -> R.string.sort_size
            SortOrder.Type -> R.string.sort_type
        }

private val ViewLayout.labelRes: Int
    get() =
        when (this) {
            ViewLayout.List -> R.string.layout_list
            ViewLayout.Grid -> R.string.layout_grid
        }
