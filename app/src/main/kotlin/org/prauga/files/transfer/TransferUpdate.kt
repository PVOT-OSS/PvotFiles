// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.transfer

import org.prauga.files.core.model.TransferKind

data class TransferUpdate(
    val id: String,
    val kind: TransferKind,
    val running: Boolean,
    val outcome: TransferOutcome?,
)
