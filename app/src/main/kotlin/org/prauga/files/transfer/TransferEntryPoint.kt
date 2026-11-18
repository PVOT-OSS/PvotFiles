// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.transfer

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.prauga.files.core.data.TransferRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TransferEntryPoint {
    fun transfers(): TransferRepository
}
