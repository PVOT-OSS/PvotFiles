// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

sealed interface FileOperationResult {
    data object Done : FileOperationResult

    data object InvalidName : FileOperationResult

    data object AlreadyExists : FileOperationResult

    data object Failed : FileOperationResult
}
