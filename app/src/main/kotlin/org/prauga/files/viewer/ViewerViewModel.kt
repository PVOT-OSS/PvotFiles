// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.viewer

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.prauga.files.core.data.ArchiveRepository
import org.prauga.files.core.data.PdfPages
import org.prauga.files.core.data.PreviewRepository
import org.prauga.files.core.data.StorageRepository
import org.prauga.files.core.model.FileKind
import org.prauga.files.core.model.FileOperationResult

@HiltViewModel
class ViewerViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val previews: PreviewRepository,
    private val archives: ArchiveRepository,
    private val storage: StorageRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow<UiState<ViewerContent>>(UiState.Loading)
    val state: StateFlow<UiState<ViewerContent>> = mutableState.asStateFlow()
    private var pdf: PdfPages? = null
    private val effectChannel = Channel<ViewerEffect>(Channel.BUFFERED)
    val effects: Flow<ViewerEffect> = effectChannel.receiveAsFlow()

    private val path: String? = savedStateHandle[ViewerActivity.EXTRA_PATH]
    private val uri: String? = savedStateHandle[ViewerActivity.EXTRA_URI]

    init {
        retry()
    }

    fun retry() {
        mutableState.value = UiState.Loading
        viewModelScope.launch {
            pdf?.close()
            pdf = null
            mutableState.value =
                when {
                    path != null -> load(path)
                    uri != null -> loadUri(uri)
                    else -> UiState.Error(message = "")
                }
        }
    }

    fun extract() {
        val archive =
            (mutableState.value as? UiState.Success)?.data as? ViewerContent.Archive ?: return
        val folder =
            if (archive.toDownloads) storage.primaryRootPath()?.let { "$it/$DOWNLOADS_FOLDER" }
            else File(archive.path).parent
        viewModelScope.launch {
            val result = folder?.let { archives.extractTo(archive.path, it) }
            effectChannel.send(
                if (result == FileOperationResult.Done && folder != null)
                    ViewerEffect.Extracted(File(folder).name)
                else ViewerEffect.Failed
            )
        }
    }

    suspend fun image(path: String, maxSidePx: Int): Bitmap? = previews.image(path, maxSidePx)

    suspend fun page(index: Int, widthPx: Int): Bitmap? = pdf?.render(index, widthPx)

    override fun onCleared() {
        pdf?.close()
    }

    private suspend fun loadUri(uri: String): UiState<ViewerContent> {
        val name = previews.displayName(uri)
        return when (FileKind.of(name)) {
            FileKind.Video -> UiState.Success(ViewerContent.Media(null, uri, name, video = true))
            FileKind.Audio -> UiState.Success(ViewerContent.Media(null, uri, name, video = false))
            else ->
                previews.copyToCache(uri, name)?.let {
                    load(it, single = true, fromOtherApp = true)
                } ?: UiState.Error(message = "")
        }
    }

    private suspend fun load(
        path: String,
        single: Boolean = false,
        fromOtherApp: Boolean = false,
    ): UiState<ViewerContent> {
        val content: ViewerContent? =
            when (FileKind.of(File(path).name)) {
                FileKind.Image -> {
                    val paths = if (single) listOf(path) else previews.siblingImages(path)
                    ViewerContent.Images(paths, paths.indexOf(path).coerceAtLeast(0))
                }
                FileKind.Pdf ->
                    previews.openPdf(path)?.let { pages ->
                        pdf = pages
                        ViewerContent.Pdf(path, List(pages.pageCount, pages::aspectRatio))
                    }
                FileKind.Archive ->
                    archives.entries(path)?.let {
                        ViewerContent.Archive(path, it, toDownloads = fromOtherApp)
                    }
                FileKind.Video ->
                    ViewerContent.Media(
                        path,
                        Uri.fromFile(File(path)).toString(),
                        File(path).name,
                        video = true,
                    )
                FileKind.Audio ->
                    ViewerContent.Media(
                        path,
                        Uri.fromFile(File(path)).toString(),
                        File(path).name,
                        video = false,
                    )
                else -> previews.text(path)?.let { ViewerContent.Text(path, it) }
            }
        return content?.let { UiState.Success(it) } ?: UiState.Error(message = "")
    }

    private companion object {
        const val DOWNLOADS_FOLDER = "Download"
    }
}
