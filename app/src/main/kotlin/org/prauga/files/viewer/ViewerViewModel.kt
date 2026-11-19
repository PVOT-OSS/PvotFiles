// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.viewer

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prauga.pvot.coreui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.prauga.files.core.data.PdfPages
import org.prauga.files.core.data.PreviewRepository
import org.prauga.files.core.model.FileKind

@HiltViewModel
class ViewerViewModel
@Inject
constructor(savedStateHandle: SavedStateHandle, private val previews: PreviewRepository) :
    ViewModel() {
    private val mutableState = MutableStateFlow<UiState<ViewerContent>>(UiState.Loading)
    val state: StateFlow<UiState<ViewerContent>> = mutableState.asStateFlow()
    private var pdf: PdfPages? = null

    private val path: String? = savedStateHandle[ViewerActivity.EXTRA_PATH]

    init {
        retry()
    }

    fun retry() {
        mutableState.value = UiState.Loading
        viewModelScope.launch {
            pdf?.close()
            pdf = null
            mutableState.value = path?.let { load(it) } ?: UiState.Error(message = "")
        }
    }

    suspend fun image(path: String, maxSidePx: Int): Bitmap? = previews.image(path, maxSidePx)

    suspend fun page(index: Int, widthPx: Int): Bitmap? = pdf?.render(index, widthPx)

    override fun onCleared() {
        pdf?.close()
    }

    private suspend fun load(path: String): UiState<ViewerContent> {
        val content: ViewerContent? =
            when (FileKind.of(File(path).name)) {
                FileKind.Image -> {
                    val paths = previews.siblingImages(path)
                    ViewerContent.Images(paths, paths.indexOf(path).coerceAtLeast(0))
                }
                FileKind.Pdf ->
                    previews.openPdf(path)?.let { pages ->
                        pdf = pages
                        ViewerContent.Pdf(path, List(pages.pageCount, pages::aspectRatio))
                    }
                FileKind.Video -> ViewerContent.Media(path, video = true)
                FileKind.Audio -> ViewerContent.Media(path, video = false)
                else -> previews.text(path)?.let { ViewerContent.Text(path, it) }
            }
        return content?.let { UiState.Success(it) } ?: UiState.Error(message = "")
    }
}
