// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.safe

import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotEmptyContent
import com.prauga.pvot.coreui.PvotErrorContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotDialog
import com.prauga.pvot.designsystem.components.PvotDialogAction
import com.prauga.pvot.designsystem.components.PvotScreen
import com.prauga.pvot.designsystem.components.PvotScreenDefaults
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSheetItem
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.components.button.PvotButton
import com.prauga.pvot.designsystem.components.list.PvotListGroup
import com.prauga.pvot.designsystem.components.list.PvotListRow
import com.prauga.pvot.designsystem.components.textfield.PvotTextField
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import com.prauga.pvot.designsystem.navigation.rememberPvotTopBarCompact
import com.prauga.pvot.designsystem.theme.PvotSpacing
import java.io.File
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.common.FileKindIcon
import org.prauga.files.common.FilesBottomSheet
import org.prauga.files.common.rememberFileLauncher
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.SafeItem

@Composable
fun SafeFolderScreen(
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    viewModel: SafeFolderViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onEvent = viewModel::onEvent
    BackHandler(onBack = onBack)
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { onEvent(SafeEvent.Stopped) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { onEvent(SafeEvent.Started) }
    DisposableEffect(viewModel) { onDispose { onEvent(SafeEvent.Lock) } }
    val snackbar = remember { SnackbarHostState() }
    val launcher = rememberFileLauncher(snackbar)
    val failed = stringResource(R.string.operation_failed)
    val movedOut = stringResource(R.string.safe_moved_out)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is SafeEffect.OpenFile -> {
                    val file = File(effect.path)
                    launcher.open(
                        FileEntry.File(
                            file.path,
                            file.name,
                            file.lastModified(),
                            sizeBytes = file.length(),
                        )
                    )
                }
                is SafeEffect.MovedOut -> snackbar.showSnackbar(movedOut.format(effect.name))
                SafeEffect.Failed -> snackbar.showSnackbar(failed)
            }
        }
    }
    var actionsId by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val items = ((state as? SafeUiState.Unlocked)?.items as? UiState.Success)?.data.orEmpty()
    val listState = rememberLazyListState()
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = stringResource(R.string.safe_title),
                onBack = onBack,
                compact = rememberPvotTopBarCompact(listState),
            ) {
                if (state is SafeUiState.Unlocked) {
                    PvotTopBarAction(
                        painterResource(R.drawable.ic_lock),
                        stringResource(R.string.safe_lock),
                        onClick = { onEvent(SafeEvent.Lock) },
                    )
                }
            }
        },
        bottomBar = bottomBar,
        snackbarHost = { PvotSnackbarHost(snackbar) },
        overlay = {
            val target = items.firstOrNull { it.id == actionsId }
            FilesBottomSheet(
                visible = target != null,
                onDismissRequest = { actionsId = null },
                title = target?.name,
            ) {
                if (target != null) {
                    PvotSheetItem(
                        title = stringResource(R.string.safe_move_out),
                        onClick = {
                            actionsId = null
                            onEvent(SafeEvent.MoveOut(target.id))
                        },
                        icon = {
                            Icon(painterResource(R.drawable.ic_move), contentDescription = null)
                        },
                    )
                    PvotSheetItem(
                        title = stringResource(R.string.trash_delete_forever),
                        onClick = {
                            actionsId = null
                            confirmDeleteId = target.id
                        },
                        icon = {
                            Icon(painterResource(R.drawable.ic_delete), contentDescription = null)
                        },
                    )
                }
            }
            val deleting = items.firstOrNull { it.id == confirmDeleteId }
            PvotDialog(
                visible = deleting != null,
                onDismissRequest = { confirmDeleteId = null },
                title = stringResource(R.string.safe_delete_title, deleting?.name.orEmpty()),
                message = stringResource(R.string.safe_delete_message),
                destructive = true,
            ) {
                PvotDialogAction(
                    label = stringResource(R.string.trash_delete_forever),
                    onClick = {
                        deleting?.let { onEvent(SafeEvent.Delete(it.id)) }
                        confirmDeleteId = null
                    },
                )
            }
        },
    ) { padding ->
        when (val current = state) {
            SafeUiState.Loading -> CenteredContent(padding) { PvotLoadingContent() }
            is SafeUiState.Setup ->
                CenteredContent(padding) {
                    PinForm(
                        title = stringResource(R.string.safe_setup_title),
                        message =
                            when {
                                current.mismatch -> stringResource(R.string.safe_pin_mismatch)
                                current.tooShort -> stringResource(R.string.safe_pin_too_short)
                                else -> stringResource(R.string.safe_setup_message)
                            },
                        confirm = true,
                        action = stringResource(R.string.safe_setup_action),
                    ) { pin, confirmation ->
                        onEvent(SafeEvent.CreatePin(pin, confirmation))
                    }
                }
            is SafeUiState.Locked ->
                CenteredContent(padding) {
                    val context = LocalContext.current
                    PinForm(
                        title = stringResource(R.string.safe_locked_title),
                        message =
                            when {
                                current.lockedUntilMillis != null ->
                                    stringResource(
                                        R.string.safe_locked_out,
                                        DateUtils.formatDateTime(
                                            context,
                                            current.lockedUntilMillis,
                                            DateUtils.FORMAT_SHOW_TIME,
                                        ),
                                    )
                                current.attemptsLeft != null ->
                                    pluralStringResource(
                                        R.plurals.safe_wrong_pin,
                                        current.attemptsLeft,
                                        current.attemptsLeft,
                                    )
                                else -> stringResource(R.string.safe_locked_message)
                            },
                        confirm = false,
                        action = stringResource(R.string.safe_unlock),
                    ) { pin, _ ->
                        onEvent(SafeEvent.Unlock(pin))
                    }
                }
            is SafeUiState.Unlocked ->
                when (val loaded = current.items) {
                    UiState.Loading -> CenteredContent(padding) { PvotLoadingContent() }
                    is UiState.Error ->
                        CenteredContent(padding) {
                            PvotErrorContent(
                                title = stringResource(R.string.safe_error_title),
                                message = stringResource(R.string.safe_error_message),
                                onRetry = { onEvent(SafeEvent.Lock) },
                            )
                        }
                    is UiState.Success ->
                        if (loaded.data.isEmpty()) {
                            CenteredContent(padding) {
                                PvotEmptyContent(stringResource(R.string.safe_empty))
                            }
                        } else {
                            PvotScreen(
                                modifier = Modifier.fillMaxSize(),
                                state = listState,
                                contentPadding =
                                    PvotScreenDefaults.contentPadding(
                                        top = padding.calculateTopPadding()
                                    ),
                            ) {
                                item {
                                    PvotListGroup(Modifier.fillMaxWidth()) {
                                        loaded.data.forEach { item ->
                                            item {
                                                ItemRow(
                                                    item,
                                                    onOpen = { onEvent(SafeEvent.Open(item.id)) },
                                                    onMore = { actionsId = item.id },
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                }
        }
    }
}

@Composable
private fun ItemRow(item: SafeItem, onOpen: () -> Unit, onMore: () -> Unit) {
    val context = LocalContext.current
    PvotListRow(
        title = item.name,
        modifier = Modifier.fillMaxWidth(),
        supportingText = Formatter.formatShortFileSize(context, item.sizeBytes),
        leading = { FileKindIcon(item.name) },
        onClick = onOpen,
        onLongClick = onMore,
        onClickLabel = stringResource(R.string.file_entry_open),
    )
}

@Composable
private fun PinForm(
    title: String,
    message: String,
    confirm: Boolean,
    action: String,
    onSubmit: (String, String) -> Unit,
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    val submit = {
        onSubmit(pin, confirmation)
        pin = ""
        confirmation = ""
    }
    Column(
        Modifier.fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PvotSpacing.L),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PvotSpacing.M),
    ) {
        Icon(
            painterResource(R.drawable.ic_lock),
            contentDescription = null,
            modifier = Modifier.size(LockIconSize),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PinField(pin, stringResource(R.string.safe_pin), last = !confirm, onDone = submit) {
            pin = it
        }
        if (confirm) {
            PinField(
                confirmation,
                stringResource(R.string.safe_pin_confirm),
                last = true,
                onDone = submit,
            ) {
                confirmation = it
            }
        }
        PvotButton(text = action, onClick = submit)
    }
}

@Composable
private fun PinField(
    value: String,
    placeholder: String,
    last: Boolean,
    onDone: () -> Unit,
    onValueChange: (String) -> Unit,
) {
    PvotTextField(
        value = value,
        onValueChange = { text -> onValueChange(text.filter { it.isDigit() }.take(MaxPinLength)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = placeholder,
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = if (last) ImeAction.Done else ImeAction.Next,
            ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        visualTransformation = PasswordVisualTransformation(),
    )
}

private val LockIconSize = 56.dp
private const val MaxPinLength = 16
