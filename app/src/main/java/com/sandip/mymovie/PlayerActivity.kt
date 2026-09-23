package com.sandip.mymovie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

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

    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(link))
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    isBuffering = state == Player.STATE_BUFFERING
                }
                override fun onPlayerError(error: PlaybackException) {
                    errorMessage = "वीडियो नहीं चल पा रहा — लिंक ग़लत हो सकता है या फ़ॉर्मेट सपोर्टेड नहीं है।\n(${error.errorCodeName})"
                }
            })
            prepare()
            playWhenReady = true
        }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var locked by remember { mutableStateOf(false) }
    var brightness by remember { mutableFloatStateOf(0.7f) }
    var controlsVisible by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            progress = exoPlayer.currentPosition.toFloat()
            delay(500)
        }
    }

    LaunchedEffect(controlsVisible, isPlaying) {
        if (controlsVisible && isPlaying && !locked) {
            delay(3000)
            controlsVisible = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                if (!locked) controlsVisible = !controlsVisible
            }
    ) {

        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // बफ़रिंग स्पिनर
        if (isBuffering && errorMessage == null) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // एरर मैसेज
        errorMessage?.let { msg ->
            Column(
                Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text(msg, color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onBack) { Text("वापस जाएं", color = Color.White) }
            }
        }

        if (locked) {
            IconButton(
                onClick = { locked = false; controlsVisible = true },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Unlock", tint = Color.White, modifier = Modifier.size(40.dp))
            }
        } else if (controlsVisible && errorMessage == null) {

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
                    .width(24.dp)
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
                        .width(3.dp)
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
                    controlsVisible = true
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
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Slider(
                    value = progress,
                    onValueChange = {
                        progress = it
                        exoPlayer.seekTo(it.toLong())
                    },
                    valueRange = 0f..(exoPlayer.duration.coerceAtLeast(1).toFloat()),
                    modifier = Modifier.height(20.dp),
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                        thumbColor = Color.White
                    )
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {}) { Text("Quality", color = Color.White, fontSize = 12.sp) }
                    TextButton(onClick = {}) { Text("Subtitles", color = Color.White, fontSize = 12.sp) }
                    TextButton(onClick = {}) { Text("Speed", color = Color.White, fontSize = 12.sp) }
                }
            }
        }
    }
}
