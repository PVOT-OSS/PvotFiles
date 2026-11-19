// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.viewer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntSize
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prauga.pvot.coreui.PvotErrorContent
import com.prauga.pvot.coreui.PvotLoadingContent
import com.prauga.pvot.coreui.UiState
import com.prauga.pvot.designsystem.components.PvotScreenScaffold
import com.prauga.pvot.designsystem.components.PvotSnackbarHost
import com.prauga.pvot.designsystem.navigation.PvotTopBar
import com.prauga.pvot.designsystem.navigation.PvotTopBarAction
import com.prauga.pvot.designsystem.theme.PvotSpacing
import java.io.File
import kotlin.math.max
import org.prauga.files.R
import org.prauga.files.common.CenteredContent
import org.prauga.files.common.rememberFileLauncher
import org.prauga.files.core.model.FileEntry

@Composable
fun ViewerScreen(onClose: () -> Unit, viewModel: ViewerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val launcher = rememberFileLauncher(snackbar)
    val content = (state as? UiState.Success)?.data
    val images = content as? ViewerContent.Images
    val pager =
        rememberPagerState(initialPage = images?.startIndex ?: 0) { images?.paths?.size ?: 0 }
    val currentPath =
        when (content) {
            is ViewerContent.Images -> content.paths.getOrNull(pager.currentPage)
            is ViewerContent.Text -> content.path
            is ViewerContent.Pdf -> content.path
            is ViewerContent.Media -> content.path
            null -> null
        }
    PvotScreenScaffold(
        topBar = {
            PvotTopBar(
                title = currentPath?.let { File(it).name }.orEmpty(),
                onBack = onClose,
                compact = true,
            ) {
                if (currentPath != null) {
                    val file = entryOf(currentPath)
                    PvotTopBarAction(
                        painterResource(R.drawable.ic_share),
                        stringResource(R.string.action_share),
                        onClick = { launcher.share(file) },
                    )
                    PvotTopBarAction(
                        painterResource(R.drawable.ic_open_with),
                        stringResource(R.string.action_open_with),
                        onClick = { launcher.openWith(file) },
                    )
                }
            }
        },
        bottomBar = {},
        snackbarHost = { PvotSnackbarHost(snackbar) },
    ) { padding ->
        when (val loaded = state) {
            UiState.Loading -> CenteredContent(padding) { PvotLoadingContent() }
            is UiState.Error ->
                CenteredContent(padding) {
                    PvotErrorContent(
                        title = stringResource(R.string.viewer_error_title),
                        message = stringResource(R.string.viewer_error_message),
                        onRetry = viewModel::retry,
                    )
                }
            is UiState.Success ->
                when (val data = loaded.data) {
                    is ViewerContent.Images -> {
                        LaunchedEffect(data.startIndex) { pager.scrollToPage(data.startIndex) }
                        ImagePager(data.paths, pager, padding, viewModel::image)
                    }
                    is ViewerContent.Text -> TextContent(data, padding)
                    is ViewerContent.Pdf -> PdfContent(data, padding, viewModel::page)
                    is ViewerContent.Media -> MediaContent(data, padding)
                }
        }
    }
}

@Composable
private fun ImagePager(
    paths: List<String>,
    pager: androidx.compose.foundation.pager.PagerState,
    padding: PaddingValues,
    load: suspend (String, Int) -> Bitmap?,
) {
    val window = LocalWindowInfo.current.containerSize
    val maxSide = max(window.width, window.height).coerceAtLeast(1)
    var zoomed by remember { mutableStateOf(false) }
    HorizontalPager(
        state = pager,
        modifier = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
        userScrollEnabled = !zoomed,
        beyondViewportPageCount = 1,
        key = { paths[it] },
    ) { page ->
        val path = paths[page]
        val image by
            produceState<ImageBitmap?>(null, path, maxSide) {
                value = load(path, maxSide)?.asImageBitmap()
            }
        val bitmap = image
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (bitmap == null) {
                PvotLoadingContent()
            } else {
                ZoomableImage(
                    bitmap,
                    File(path).name,
                    onZoomChange = { if (page == pager.currentPage) zoomed = it },
                )
            }
        }
    }
}

@Composable
private fun ZoomableImage(
    bitmap: ImageBitmap,
    description: String,
    onZoomChange: (Boolean) -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    fun clamp(value: Offset, zoom: Float): Offset {
        val maxX = size.width * (zoom - 1) / 2
        val maxY = size.height * (zoom - 1) / 2
        return Offset(value.x.coerceIn(-maxX, maxX), value.y.coerceIn(-maxY, maxY))
    }
    val transform = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, MaxZoom)
        offset = if (scale == 1f) Offset.Zero else clamp(offset + pan * scale, scale)
    }
    LaunchedEffect(scale) { onZoomChange(scale > 1f) }
    Image(
        bitmap,
        contentDescription = description,
        modifier =
            Modifier.fillMaxSize()
                .onSizeChanged { size = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tap ->
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = DoubleTapZoom
                                val centre = Offset(size.width / 2f, size.height / 2f)
                                offset = clamp((centre - tap) * (DoubleTapZoom - 1), DoubleTapZoom)
                            }
                        }
                    )
                }
                .transformable(transform, canPan = { scale > 1f })
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun TextContent(content: ViewerContent.Text, padding: PaddingValues) {
    Column(
        Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = padding.calculateTopPadding())
            .padding(PvotSpacing.M),
        verticalArrangement = Arrangement.spacedBy(PvotSpacing.M),
    ) {
        SelectionContainer {
            Text(
                content.preview.text,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
            )
        }
        if (content.preview.truncated) {
            Text(
                stringResource(R.string.viewer_text_truncated),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PdfContent(
    content: ViewerContent.Pdf,
    padding: PaddingValues,
    render: suspend (Int, Int) -> Bitmap?,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                top = padding.calculateTopPadding() + PvotSpacing.S,
                bottom = PvotSpacing.M,
            ),
        verticalArrangement = Arrangement.spacedBy(PvotSpacing.S),
    ) {
        items(content.aspectRatios.size) { index ->
            val ratio = content.aspectRatios[index].coerceAtLeast(MinPageRatio)
            BoxWithConstraints(
                Modifier.fillMaxWidth().padding(horizontal = PvotSpacing.S).aspectRatio(1f / ratio),
                contentAlignment = Alignment.Center,
            ) {
                val width = constraints.maxWidth
                val page by
                    produceState<ImageBitmap?>(null, index, width) {
                        value = render(index, width)?.asImageBitmap()
                    }
                val bitmap = page
                if (bitmap == null) {
                    PvotLoadingContent()
                } else {
                    Image(
                        bitmap,
                        contentDescription =
                            stringResource(
                                R.string.viewer_page,
                                index + 1,
                                content.aspectRatios.size,
                            ),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

private fun entryOf(path: String): FileEntry.File {
    val file = File(path)
    return FileEntry.File(file.path, file.name, file.lastModified(), sizeBytes = file.length())
}

private const val MaxZoom = 5f
private const val DoubleTapZoom = 2.5f
private const val MinPageRatio = 0.1f
