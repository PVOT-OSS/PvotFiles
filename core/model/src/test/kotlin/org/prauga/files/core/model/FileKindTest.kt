// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FileKindTest {
    @Test
    fun readsTheKindFromTheExtensionInAnyCase() {
        assertEquals(FileKind.Pdf, FileKind.of("Report.PDF"))
        assertEquals(FileKind.Spreadsheet, FileKind.of("budget.xlsx"))
        assertEquals(FileKind.Archive, FileKind.of("backup.tar.gz"))
        assertEquals(FileKind.App, FileKind.of("app-release.apk"))
    }

    @Test
    fun namesWithoutAKnownExtensionAreOther() {
        assertEquals(FileKind.Other, FileKind.of("README"))
        assertEquals(FileKind.Other, FileKind.of(".nomedia"))
        assertEquals(FileKind.Other, FileKind.of("notes."))
        assertEquals(FileKind.Other, FileKind.of("data.bin"))
    }
}
