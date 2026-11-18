// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import kotlinx.coroutines.flow.Flow
import org.prauga.files.core.model.DisplayOptions

interface DisplayOptionsRepository {
    val options: Flow<DisplayOptions>

    suspend fun update(transform: (DisplayOptions) -> DisplayOptions)
}
