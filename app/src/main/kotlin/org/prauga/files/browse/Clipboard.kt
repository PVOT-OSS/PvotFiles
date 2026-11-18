// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.browse

import org.prauga.files.core.model.TransferKind

data class Clipboard(val paths: List<String>, val kind: TransferKind)
