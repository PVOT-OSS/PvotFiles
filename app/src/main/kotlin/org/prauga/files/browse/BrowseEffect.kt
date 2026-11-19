// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import org.prauga.files.core.model.FileOperationResult
import org.prauga.files.core.model.TransferKind
import org.prauga.files.transfer.TransferOutcome

sealed interface BrowseEffect {
    data class OperationFailed(val result: FileOperationResult) : BrowseEffect

    data object MovedToSafe : BrowseEffect

    data object SafeFolderNeedsSetup : BrowseEffect

    data class Trashed(val trashIds: List<String>, val names: List<String>) : BrowseEffect

    data class TransferFinished(val kind: TransferKind, val outcome: TransferOutcome) :
        BrowseEffect
}
