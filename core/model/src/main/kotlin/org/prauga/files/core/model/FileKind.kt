// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

enum class FileKind {
    Pdf,
    Document,
    Spreadsheet,
    Presentation,
    Web,
    Text,
    Archive,
    App,
    Image,
    Video,
    Audio,
    Other;

    companion object {
        private val byExtension: Map<String, FileKind> = buildMap {
            put("pdf", Pdf)
            listOf("doc", "docx", "odt", "rtf").forEach { put(it, Document) }
            listOf("xls", "xlsx", "xlsm", "ods", "csv", "tsv").forEach { put(it, Spreadsheet) }
            listOf("ppt", "pptx", "odp").forEach { put(it, Presentation) }
            listOf("html", "htm", "xhtml", "xml").forEach { put(it, Web) }
            listOf("txt", "md", "json", "log", "ini", "yaml", "yml").forEach { put(it, Text) }
            listOf("zip", "rar", "7z", "tar", "gz", "tgz", "bz2", "xz").forEach { put(it, Archive) }
            listOf("apk", "apks", "xapk").forEach { put(it, App) }
            listOf("jpg", "jpeg", "png", "gif", "webp", "heic", "heif", "bmp", "avif", "dng")
                .forEach { put(it, Image) }
            listOf("mp4", "mkv", "webm", "3gp", "mov", "avi", "m4v").forEach { put(it, Video) }
            listOf("mp3", "m4a", "aac", "ogg", "opus", "flac", "wav", "amr", "mid").forEach {
                put(it, Audio)
            }
        }

        fun of(name: String): FileKind {
            val dot = name.lastIndexOf('.')
            if (dot <= 0 || dot == name.lastIndex) return Other
            return byExtension[name.substring(dot + 1).lowercase()] ?: Other
        }
    }
}
