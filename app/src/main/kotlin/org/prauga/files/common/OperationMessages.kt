// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.prauga.files.R
import org.prauga.files.core.model.FileOperationResult

@Composable
fun operationMessages(): Map<FileOperationResult, String> =
    mapOf(
        FileOperationResult.InvalidName to stringResource(R.string.name_invalid),
        FileOperationResult.AlreadyExists to stringResource(R.string.operation_exists),
        FileOperationResult.Failed to stringResource(R.string.operation_failed),
    )
