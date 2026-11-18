// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.access

sealed interface AccessUiState {
    data object Required : AccessUiState

    data object Granted : AccessUiState
}
