// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import android.provider.MediaStore.Files.FileColumns
import org.prauga.files.core.model.SearchFilters

internal object SearchSelection {
    private const val SECONDS_PER_DAY = 24L * 60 * 60

    fun of(query: String, filters: SearchFilters, nowSeconds: Long): CategorySelection {
        val clauses = mutableListOf<String>()
        val arguments = mutableListOf<String>()
        val category = filters.category
        if (category == null) {
            clauses += "${FileColumns.MIME_TYPE} IS NOT NULL"
        } else {
            val selection = CategorySelection.of(category)
            clauses += "(${selection.selection})"
            arguments += selection.arguments
        }
        if (query.isNotBlank()) {
            clauses += "${FileColumns.DISPLAY_NAME} LIKE ? ESCAPE '${NameQuery.ESCAPE}'"
            arguments += NameQuery.likePattern(query)
        }
        filters.modified.days?.let { days ->
            clauses += "${FileColumns.DATE_MODIFIED} >= ?"
            arguments += (nowSeconds - days * SECONDS_PER_DAY).toString()
        }
        filters.size.minBytes?.let {
            clauses += "${FileColumns.SIZE} >= ?"
            arguments += it.toString()
        }
        filters.size.maxBytes?.let {
            clauses += "${FileColumns.SIZE} < ?"
            arguments += it.toString()
        }
        return CategorySelection(clauses.joinToString(" AND "), arguments.toTypedArray())
    }
}
