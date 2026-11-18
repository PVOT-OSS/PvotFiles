// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import org.prauga.files.core.model.FileOperationResult

sealed interface BrowseEffect {
    data class OperationFailed(val result: FileOperationResult) : BrowseEffect
}
