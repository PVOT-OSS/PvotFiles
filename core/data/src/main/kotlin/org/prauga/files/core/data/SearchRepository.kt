// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import org.prauga.files.core.model.FileEntry

interface SearchRepository {
    suspend fun search(query: String): List<FileEntry.File>?
}
