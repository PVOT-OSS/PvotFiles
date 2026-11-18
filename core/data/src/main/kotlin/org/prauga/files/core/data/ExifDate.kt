// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

internal object ExifDate {
    private val format = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

    fun parse(value: String?, zone: ZoneId): Long? {
        if (value.isNullOrBlank()) return null
        return try {
            LocalDateTime.parse(value.trim(), format).atZone(zone).toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
