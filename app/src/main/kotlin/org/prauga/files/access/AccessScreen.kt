// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.access

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotEmptyContent
import org.prauga.files.R

@Composable
fun AccessScreen(viewModel: AccessViewModel = hiltViewModel(), content: @Composable () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LifecycleResumeEffect(viewModel) {
        viewModel.onEvent(AccessEvent.Refresh)
        onPauseOrDispose {}
    }
    when (state) {
        AccessUiState.Granted -> content()
        AccessUiState.Required ->
            AccessRequiredContent(onAllow = { context.openAllFilesAccessSettings() })
    }
}

@Composable
private fun AccessRequiredContent(onAllow: () -> Unit) {
    PvotEmptyContent(
        message = stringResource(R.string.access_message),
        actionLabel = stringResource(R.string.access_allow),
        onAction = onAllow,
    )
}

private fun Context.openAllFilesAccessSettings() {
    val appSettings =
        Intent(
            Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
            "package:$packageName".toUri(),
        )
    try {
        startActivity(appSettings)
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
    }
}
