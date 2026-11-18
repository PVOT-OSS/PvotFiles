// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.info

import android.Manifest
import android.content.pm.PackageManager
import android.text.format.DateUtils
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.list.PvotListRow
import java.io.File
import java.util.Locale
import org.prauga.files.R
import org.prauga.files.common.FilesBottomSheet
import org.prauga.files.core.model.FileInfo

@Composable
fun InfoSheet(path: String?, onDismiss: () -> Unit, viewModel: InfoViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(path) { if (path != null) viewModel.load(path) }
    val context = LocalContext.current
    val askLocation =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted && path != null) viewModel.load(path)
        }
    val canReadLocation =
        context.checkSelfPermission(Manifest.permission.ACCESS_MEDIA_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    FilesBottomSheet(
        visible = path != null,
        onDismissRequest = onDismiss,
        title = path?.let { File(it).name },
    ) {
        when (val info = state) {
            UiState.Loading -> PvotLoadingContent()
            is UiState.Error -> Row(stringResource(R.string.info_unavailable), null)
            is UiState.Success ->
                Details(info.data, canReadLocation) {
                    askLocation.launch(Manifest.permission.ACCESS_MEDIA_LOCATION)
                }
        }
    }
}

@Composable
private fun Details(info: FileInfo, canReadLocation: Boolean, onAskLocation: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val size = Formatter.formatFileSize(context, info.sizeBytes)
    Row(
        stringResource(R.string.info_kind),
        info.mimeType
            ?: stringResource(
                if (info.isDirectory) R.string.location_folder else R.string.info_unknown
            ),
    )
    Row(stringResource(R.string.info_location), File(info.path).parent.orEmpty())
    if (info.isDirectory) {
        val count = info.itemCount ?: 0
        Row(
            stringResource(R.string.info_contents),
            pluralStringResource(R.plurals.file_entry_folder_items, count, count) + " · " + size,
        )
    } else {
        Row(
            stringResource(R.string.info_size),
            pluralStringResource(
                R.plurals.info_size_bytes,
                info.sizeBytes.toQuantity(),
                size,
                info.sizeBytes,
            ),
        )
    }
    Row(stringResource(R.string.info_modified), dateTime(context, info.lastModifiedMillis))
    info.takenMillis?.let { Row(stringResource(R.string.info_taken), dateTime(context, it)) }
    val width = info.width
    val height = info.height
    if (width != null && height != null) {
        Row(
            stringResource(R.string.info_dimensions),
            stringResource(R.string.info_dimensions_value, width, height),
        )
    }
    info.durationMillis?.let {
        Row(stringResource(R.string.info_duration), DateUtils.formatElapsedTime(it / 1_000))
    }
    val latitude = info.latitude
    val longitude = info.longitude
    when {
        latitude != null && longitude != null -> {
            val text = String.format(Locale.ROOT, "%.5f, %.5f", latitude, longitude)
            Row(stringResource(R.string.info_coordinates), text) {
                uriHandler.openUri("geo:$latitude,$longitude?q=$latitude,$longitude")
            }
        }
        info.mimeType?.startsWith("image/") == true && !canReadLocation ->
            Row(
                stringResource(R.string.info_coordinates),
                stringResource(R.string.info_show_location),
                onAskLocation,
            )
    }
}

@Composable
private fun Row(title: String, value: String?, onClick: (() -> Unit)? = null) {
    PvotListRow(
        title = title,
        modifier = Modifier.fillMaxWidth(),
        supportingText = value,
        onClick = onClick,
    )
}

private fun dateTime(context: android.content.Context, millis: Long): String =
    DateUtils.formatDateTime(
        context,
        millis,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_TIME,
    )

private fun Long.toQuantity(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
