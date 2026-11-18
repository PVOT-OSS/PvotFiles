// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.trash

import org.prauga.files.core.model.FileOperationResult

sealed interface TrashEffect {
    data class Restored(val name: String) : TrashEffect

    data class OperationFailed(val result: FileOperationResult) : TrashEffect
}
