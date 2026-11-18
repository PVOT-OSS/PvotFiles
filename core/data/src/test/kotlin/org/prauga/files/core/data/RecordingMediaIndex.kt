// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

class RecordingMediaIndex : MediaIndex {
    val updates = mutableListOf<List<String>>()

    override fun update(paths: List<String>) {
        updates += paths
    }
}
