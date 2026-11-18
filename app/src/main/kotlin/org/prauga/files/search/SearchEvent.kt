// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.search

sealed interface SearchEvent {
    data class QueryChanged(val query: String) : SearchEvent

    data object Clear : SearchEvent

    data object Retry : SearchEvent
}
