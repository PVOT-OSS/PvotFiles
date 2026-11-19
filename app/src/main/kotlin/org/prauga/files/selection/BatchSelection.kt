// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.selection

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.prauga.files.R
import org.prauga.files.common.FileLauncher
import org.prauga.files.core.model.FileEntry
import org.prauga.files.tags.TagPickerSheet

@Stable
class BatchSelection
internal constructor(
    private val entries: () -> List<FileEntry>,
    private val read: () -> List<String>,
    private val write: (List<String>) -> Unit,
    internal val viewModel: FileBatchViewModel,
    internal val launcher: FileLauncher,
    internal val snackbar: SnackbarHostState,
) {
    internal var sheetOpen by mutableStateOf(false)
    internal var tagsOpen by mutableStateOf(false)

    val selected: Set<String>
        get() = read().toSet()

    val active: Boolean
        get() = read().isNotEmpty()

    fun toggle(path: String) {
        val current = read()
        write(if (path in current) current - path else current + path)
    }

    fun selectAll() = write(entries().map { it.path })

    fun clear() = write(emptyList())

    internal fun selectedFiles(): List<FileEntry.File> =
        entries().filterIsInstance<FileEntry.File>().filter { it.path in selected }
}

@Composable
fun rememberBatchSelection(
    entries: List<FileEntry>,
    launcher: FileLauncher,
    snackbar: SnackbarHostState,
    viewModel: FileBatchViewModel = hiltViewModel(),
    onChanged: () -> Unit,
): BatchSelection {
    var paths by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val present = entries.map { it.path }.toSet()
    if (paths.any { it !in present } && entries.isNotEmpty()) paths = paths.filter { it in present }
    val resources = LocalResources.current
    val undo = stringResource(R.string.undo)
    val failed = stringResource(R.string.operation_failed)
    val movedToSafe = stringResource(R.string.safe_moved_in)
    val needsSetup = stringResource(R.string.safe_needs_setup)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is BatchEffect.Trashed -> {
                    val message =
                        resources.getQuantityString(
                            R.plurals.moved_to_trash_count,
                            effect.trashIds.size,
                            effect.trashIds.size,
                        )
                    if (
                        snackbar.showSnackbar(message, actionLabel = undo) ==
                            SnackbarResult.ActionPerformed
                    ) {
                        viewModel.restore(effect.trashIds)
                    }
                }
                BatchEffect.MovedToSafe -> snackbar.showSnackbar(movedToSafe)
                BatchEffect.SafeFolderNeedsSetup -> snackbar.showSnackbar(needsSetup)
                BatchEffect.Failed -> snackbar.showSnackbar(failed)
                BatchEffect.Changed -> onChanged()
            }
        }
    }
    val latestEntries by rememberUpdatedState(entries)
    return remember(viewModel, launcher, snackbar) {
        BatchSelection({ latestEntries }, { paths }, { paths = it }, viewModel, launcher, snackbar)
    }
}

@Composable
fun BatchTopBar(selection: BatchSelection, compact: Boolean) {
    SelectionTopBar(
        count = selection.selected.size,
        compact = compact,
        onClear = selection::clear,
        onSelectAll = selection::selectAll,
        onShare = { selection.launcher.shareAll(selection.selectedFiles()) },
        onDelete = {
            val paths = selection.selected.toList()
            selection.clear()
            selection.viewModel.delete(paths)
        },
        onMore = { selection.sheetOpen = true },
    )
}

@Composable
fun BatchOverlay(selection: BatchSelection) {
    BatchActionsSheet(
        visible = selection.sheetOpen,
        count = selection.selected.size,
        onDismiss = { selection.sheetOpen = false },
        onTags = { selection.tagsOpen = true },
        onMoveToSafe = {
            val paths = selection.selected.toList()
            selection.clear()
            selection.viewModel.moveToSafe(paths)
        },
    )
    TagPickerSheet(
        paths = if (selection.tagsOpen) selection.selected.toList() else emptyList(),
        title = null,
        snackbar = selection.snackbar,
        onDismiss = { selection.tagsOpen = false },
    )
}
