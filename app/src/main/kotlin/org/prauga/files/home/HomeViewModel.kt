// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.prauga.files.core.data.DocumentTreeRepository
import org.prauga.files.core.data.FavouritesRepository
import org.prauga.files.core.data.RecentFilesRepository
import org.prauga.files.core.data.StorageRepository
import org.prauga.files.core.data.StorageUsageRepository
import org.prauga.files.core.data.TagsRepository
import org.prauga.files.core.model.FileEntry
import org.prauga.files.core.model.SavedLocation
import org.prauga.files.core.model.StorageUsage
import org.prauga.files.core.model.Tag
import org.prauga.files.core.model.Volume

@HiltViewModel
class HomeViewModel
@Inject
constructor(
    private val repository: StorageUsageRepository,
    private val recentFiles: RecentFilesRepository,
    favourites: FavouritesRepository,
    tags: TagsRepository,
    private val storage: StorageRepository,
    private val locations: DocumentTreeRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow<UiState<StorageUsage>>(UiState.Loading)
    val state: StateFlow<UiState<StorageUsage>> = mutableState.asStateFlow()
    private val mutableRecent = MutableStateFlow<List<FileEntry.File>>(emptyList())
    val recent: StateFlow<List<FileEntry.File>> = mutableRecent.asStateFlow()
    val favouriteFolders: StateFlow<List<String>> =
        favourites.favourites.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )
    val tags: StateFlow<List<Tag>> =
        tags.tags.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val mutableVolumes = MutableStateFlow<List<Volume>>(emptyList())
    val removableVolumes: StateFlow<List<Volume>> = mutableVolumes.asStateFlow()
    val downloadsPath: String? = storage.primaryRootPath()?.let { "$it/$DOWNLOADS_FOLDER" }
    val savedLocations: StateFlow<List<SavedLocation>> =
        locations.locations.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )
    private var measuring: Job? = null

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> refresh()
            is HomeEvent.AddLocation -> viewModelScope.launch { locations.add(event.treeUri) }
        }
    }

    private fun refresh() {
        measuring?.cancel()
        mutableVolumes.value = storage.volumes().filterNot { it.primary }
        if (mutableState.value !is UiState.Success) mutableState.value = UiState.Loading
        measuring =
            viewModelScope.launch {
                mutableState.value =
                    repository.measure()?.let { UiState.Success(it) } ?: UiState.Error(message = "")
                mutableRecent.value = recentFiles.recent(RECENT_LIMIT).orEmpty()
            }
    }

    private companion object {
        const val RECENT_LIMIT = 6
        const val DOWNLOADS_FOLDER = "Download"
    }
}
