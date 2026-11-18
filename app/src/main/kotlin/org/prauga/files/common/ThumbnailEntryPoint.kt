// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.common

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.prauga.files.core.data.ThumbnailRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ThumbnailEntryPoint {
    fun thumbnails(): ThumbnailRepository
}
