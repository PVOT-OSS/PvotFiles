// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.core.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun directoryRepository(repository: LocalDirectoryRepository): DirectoryRepository

    @Binds abstract fun storageRepository(repository: VolumeStorageRepository): StorageRepository

    @Binds
    abstract fun storageUsageRepository(
        repository: MediaStoreStorageUsageRepository
    ): StorageUsageRepository

    @Binds
    abstract fun categoryFilesRepository(
        repository: MediaStoreCategoryFilesRepository
    ): CategoryFilesRepository

    @Binds
    abstract fun displayOptionsRepository(
        repository: DataStoreDisplayOptionsRepository
    ): DisplayOptionsRepository
}
