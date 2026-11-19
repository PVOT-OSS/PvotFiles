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

    @Binds abstract fun mediaIndex(index: ScannerMediaIndex): MediaIndex

    @Binds
    abstract fun fileOperationsRepository(
        repository: LocalFileOperationsRepository
    ): FileOperationsRepository

    @Binds abstract fun trashRepository(repository: LocalTrashRepository): TrashRepository

    @Binds abstract fun transferRepository(repository: LocalTransferRepository): TransferRepository

    @Binds abstract fun searchRepository(repository: MediaStoreSearchRepository): SearchRepository

    @Binds
    abstract fun recentFilesRepository(
        repository: MediaStoreRecentFilesRepository
    ): RecentFilesRepository

    @Binds
    abstract fun favouritesRepository(
        repository: DataStoreFavouritesRepository
    ): FavouritesRepository

    @Binds abstract fun tagsRepository(repository: DataStoreTagsRepository): TagsRepository

    @Binds
    abstract fun thumbnailRepository(repository: LocalThumbnailRepository): ThumbnailRepository

    @Binds
    abstract fun documentTreeRepository(
        repository: SafDocumentTreeRepository
    ): DocumentTreeRepository

    @Binds abstract fun fileInfoRepository(repository: LocalFileInfoRepository): FileInfoRepository

    @Binds abstract fun archiveRepository(repository: LocalArchiveRepository): ArchiveRepository

    @Binds abstract fun previewRepository(repository: LocalPreviewRepository): PreviewRepository

    @Binds abstract fun cleanRepository(repository: LocalCleanRepository): CleanRepository

    @Binds
    abstract fun searchHistoryRepository(
        repository: DataStoreSearchHistoryRepository
    ): SearchHistoryRepository

    @Binds abstract fun appsRepository(repository: LocalAppsRepository): AppsRepository

    @Binds abstract fun safeCipher(cipher: TinkSafeCipher): SafeCipher

    @Binds
    abstract fun safeFolderRepository(repository: LocalSafeFolderRepository): SafeFolderRepository
}
