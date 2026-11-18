// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.tags

import org.prauga.files.core.model.Tag

data class TagPickerUiState(
    val paths: List<String>,
    val tags: List<Tag>,
    val selected: Set<String>,
)
