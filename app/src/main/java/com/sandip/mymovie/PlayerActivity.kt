package com.sandip.mymovie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra("title") ?: ""
        val link = intent.getStringExtra("link") ?: ""
        setContent { PlayerScreen(title, link) { finish() } }
    }
}

@Composable
fun PlayerScreen(title: String, link: String, onBack: () -> Unit) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(link))
            prepare()
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    var brightness by remember { mutableFloatStateOf(0.7f) }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!locked) {
            Row(
                Modifier.fillMaxWidth().align(Alignment.TopStart).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = { locked = true }) {
                    Icon(Icons.Default.LockOpen, contentDescription = "Lock", tint = Color.White)
                }
            }

            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .height(160.dp)
                    .width(30.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { _, dragAmount ->
                            brightness = (brightness - dragAmount / 300f).coerceIn(0f, 1f)
                        }
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    Modifier
                        .fillMaxHeight(brightness)
                        .width(6.dp)
                        .background(Color.White)
                )
            }

            Row(
                Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                IconButton(onClick = {
                    if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                    isPlaying = !isPlaying
                }) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                IconButton(onClick = {
                    exoPlayer.seekTo(exoPlayer.currentPosition + 10_000)
                }) {
                    Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }

            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(12.dp)
            ) {
                Slider(
                    value = exoPlayer.currentPosition.toFloat(),
                    onValueChange = { exoPlayer.seekTo(it.toLong()) },
                    valueRange = 0f..(exoPlayer.duration.coerceAtLeast(1).toFloat())
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {}) { Text("Quality", color = Color.White) }
                    TextButton(onClick = {}) { Text("Audio & Subtitles", color = Color.White) }
                    TextButton(onClick = {}) { Text("Speed", color = Color.White) }
                }
            }
        } else {
            IconButton(
                onClick = { locked = false },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Unlock", tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }
    }
}
