// SPDX-FileCopyrightText: 2026 Saalim Quadri <danascape@gmail.com>
// SPDX-License-Identifier: Apache-2.0

package org.prauga.files.viewer

import android.net.Uri
import android.text.format.DateUtils
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import com.prauga.pvot.designsystem.components.progress.PvotLinearProgress
import com.prauga.pvot.designsystem.theme.PvotSpacing
import java.io.File
import kotlinx.coroutines.delay
import org.prauga.files.R

@Composable
fun MediaContent(content: ViewerContent.Media, padding: PaddingValues) {
    val player = rememberPlayer(content.path)
    var ratio by remember(player) { mutableFloatStateOf(player.videoSize.ratio()) }
    DisposableEffect(player) {
        val listener =
            object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    videoSize.ratio().takeIf { it > 0f }?.let { ratio = it }
                }
            }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { player.pause() }
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (content.video) {
                    Surface(
                        player,
                        if (ratio > 0f) Modifier.fillMaxWidth().aspectRatio(ratio)
                        else Modifier.fillMaxSize(),
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(PvotSpacing.M),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_audio),
                            contentDescription = null,
                            modifier = Modifier.size(AudioIconSize),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            File(content.path).nameWithoutExtension,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
            Controls(player)
        }
    }
}

private fun VideoSize.ratio(): Float =
    if (width > 0 && height > 0) width * pixelWidthHeightRatio / height else 0f

@Composable
private fun rememberPlayer(path: String): ExoPlayer {
    val context = LocalContext.current
    val player =
        remember(path) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.fromFile(File(path))))
                prepare()
                playWhenReady = true
            }
        }
    DisposableEffect(player) { onDispose { player.release() } }
    return player
}

@OptIn(UnstableApi::class)
@Composable
private fun Surface(player: ExoPlayer, modifier: Modifier) {
    PlayerSurface(player, modifier, surfaceType = SURFACE_TYPE_TEXTURE_VIEW)
}

@Composable
private fun Controls(player: ExoPlayer) {
    var playing by remember { mutableStateOf(player.isPlaying) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    DisposableEffect(player) {
        val listener =
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    playing = isPlaying
                }
            }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }
    LaunchedEffect(player) {
        while (true) {
            position = player.currentPosition
            duration = player.duration.coerceAtLeast(0L)
            delay(PositionPollMillis)
        }
    }
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(PvotSpacing.M),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PvotSpacing.S),
    ) {
        IconButton(
            onClick = {
                when {
                    playing -> player.pause()
                    player.playbackState == Player.STATE_ENDED -> {
                        player.seekTo(0)
                        player.play()
                    }
                    else -> player.play()
                }
            }
        ) {
            Icon(
                painterResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription =
                    stringResource(if (playing) R.string.action_pause else R.string.action_play),
            )
        }
        Text(
            DateUtils.formatElapsedTime(position / 1_000),
            style = MaterialTheme.typography.labelMedium,
        )
        PvotLinearProgress(
            progress = {
                if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
            },
            onSeek = { fraction ->
                if (duration > 0) player.seekTo((fraction * duration).toLong())
            },
            modifier = Modifier.weight(1f),
        )
        Text(
            DateUtils.formatElapsedTime(duration / 1_000),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private val AudioIconSize = 96.dp
private const val PositionPollMillis = 250L
