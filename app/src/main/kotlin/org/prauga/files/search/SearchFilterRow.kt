// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.search

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.prauga.pvot.designsystem.components.PvotSheetItem
import com.prauga.pvot.designsystem.theme.PvotSpacing
import java.io.File
import org.prauga.files.R
import org.prauga.files.common.FilesBottomSheet
import org.prauga.files.common.labelRes
import org.prauga.files.core.model.FileCategory
import org.prauga.files.core.model.ModifiedWithin
import org.prauga.files.core.model.SearchFilters
import org.prauga.files.core.model.SizeRange

private enum class FilterSheet {
    Type,
    Modified,
    Size,
}

@Composable
fun SearchFilterRow(filters: SearchFilters, onEvent: (SearchEvent) -> Unit) {
    var open by rememberSaveable { mutableStateOf<FilterSheet?>(null) }
    Row(
        Modifier.fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = PvotSpacing.L),
        horizontalArrangement = Arrangement.spacedBy(PvotSpacing.S),
    ) {
        filters.folder?.let { folder ->
            FilterChip(
                selected = true,
                onClick = { onEvent(SearchEvent.FolderChanged(null)) },
                label = {
                    Text(
                        stringResource(
                            R.string.search_in_folder,
                            File(folder).name.ifEmpty { folder },
                        )
                    )
                },
                trailingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_clear),
                        contentDescription = stringResource(R.string.search_clear_folder),
                    )
                },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        }
        Chip(
            filters.category?.let { stringResource(it.labelRes) }
                ?: stringResource(R.string.search_filter_type),
            filters.category != null,
        ) {
            open = FilterSheet.Type
        }
        Chip(stringResource(filters.modified.labelRes), filters.modified != ModifiedWithin.Any) {
            open = FilterSheet.Modified
        }
        Chip(stringResource(filters.size.labelRes), filters.size != SizeRange.Any) {
            open = FilterSheet.Size
        }
    }
    val dismiss = { open = null }
    FilesBottomSheet(
        visible = open != null,
        onDismissRequest = dismiss,
        title = open?.let { stringResource(it.titleRes) },
    ) {
        when (open) {
            FilterSheet.Type -> {
                Option(stringResource(R.string.search_filter_any), filters.category == null) {
                    dismiss()
                    onEvent(SearchEvent.CategoryChanged(null))
                }
                FileCategory.entries.forEach { category ->
                    Option(stringResource(category.labelRes), filters.category == category) {
                        dismiss()
                        onEvent(SearchEvent.CategoryChanged(category))
                    }
                }
            }
            FilterSheet.Modified ->
                ModifiedWithin.entries.forEach { modified ->
                    Option(stringResource(modified.labelRes), filters.modified == modified) {
                        dismiss()
                        onEvent(SearchEvent.ModifiedChanged(modified))
                    }
                }
            FilterSheet.Size ->
                SizeRange.entries.forEach { size ->
                    Option(stringResource(size.labelRes), filters.size == size) {
                        dismiss()
                        onEvent(SearchEvent.SizeChanged(size))
                    }
                }
            null -> Unit
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        trailingIcon = { Icon(painterResource(R.drawable.ic_expand), contentDescription = null) },
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    )
}

@Composable
private fun Option(title: String, selected: Boolean, onClick: () -> Unit) {
    PvotSheetItem(
        title = title,
        onClick = onClick,
        icon = {
            if (selected) {
                Icon(
                    painterResource(R.drawable.ic_check),
                    contentDescription = stringResource(R.string.entry_selected),
                )
            } else {
                Box(Modifier.size(24.dp))
            }
        },
    )
}

@get:StringRes
private val FilterSheet.titleRes: Int
    get() =
        when (this) {
            FilterSheet.Type -> R.string.search_filter_type
            FilterSheet.Modified -> R.string.search_filter_modified
            FilterSheet.Size -> R.string.search_filter_size
        }

@get:StringRes
private val ModifiedWithin.labelRes: Int
    get() =
        when (this) {
            ModifiedWithin.Any -> R.string.search_filter_modified
            ModifiedWithin.Week -> R.string.search_modified_week
            ModifiedWithin.Month -> R.string.search_modified_month
            ModifiedWithin.Year -> R.string.search_modified_year
        }

@get:StringRes
private val SizeRange.labelRes: Int
    get() =
        when (this) {
            SizeRange.Any -> R.string.search_filter_size
            SizeRange.Small -> R.string.search_size_small
            SizeRange.Medium -> R.string.search_size_medium
            SizeRange.Large -> R.string.search_size_large
        }
