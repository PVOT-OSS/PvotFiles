// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.tags

import org.prauga.files.core.model.Tag

sealed interface TagPickerEvent {
    data class Show(val path: String?) : TagPickerEvent

    data class Toggle(val tagName: String) : TagPickerEvent

    data class Create(val tag: Tag) : TagPickerEvent
}
