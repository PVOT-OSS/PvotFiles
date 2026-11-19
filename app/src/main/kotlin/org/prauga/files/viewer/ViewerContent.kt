// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.viewer

import org.prauga.files.core.model.ArchiveEntry
import org.prauga.files.core.model.TextPreview

sealed interface ViewerContent {
    data class Images(val paths: List<String>, val startIndex: Int) : ViewerContent

    data class Text(val path: String, val preview: TextPreview) : ViewerContent

    data class Pdf(val path: String, val aspectRatios: List<Float>) : ViewerContent

    data class Archive(
        val path: String,
        val entries: List<ArchiveEntry>,
        val toDownloads: Boolean,
    ) : ViewerContent

    data class Media(val path: String?, val uri: String, val name: String, val video: Boolean) :
        ViewerContent
}
