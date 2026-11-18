// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.tags

sealed interface TaggedEvent {
    data object Refresh : TaggedEvent

    data object DeleteTag : TaggedEvent
}
