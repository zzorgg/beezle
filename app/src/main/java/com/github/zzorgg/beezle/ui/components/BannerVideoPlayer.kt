package com.github.zzorgg.beezle.ui.components

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun BannerVideoPlayer(
    assetFile: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = true,
    loop: Boolean = true,
    playing: Boolean = true,
) {
    val context = LocalContext.current
    val exoPlayer = remember(assetFile) {
        ExoPlayer.Builder(context).build().apply {
            val encoded = Uri.encode(assetFile)
            val uri = Uri.parse("asset:///$encoded")
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = autoplay && playing
            repeatMode = if (loop) ExoPlayer.REPEAT_MODE_ALL else ExoPlayer.REPEAT_MODE_OFF
        }
    }
    LaunchedEffect(playing) {
        exoPlayer.playWhenReady = playing && autoplay
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                player = exoPlayer
            }
        }
    )
}
