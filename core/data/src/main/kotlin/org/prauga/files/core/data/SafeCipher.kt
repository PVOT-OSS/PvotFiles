// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import java.io.InputStream
import java.io.OutputStream

internal interface SafeCipher {
    fun encrypting(output: OutputStream, associatedData: ByteArray): OutputStream

    fun decrypting(input: InputStream, associatedData: ByteArray): InputStream
}
