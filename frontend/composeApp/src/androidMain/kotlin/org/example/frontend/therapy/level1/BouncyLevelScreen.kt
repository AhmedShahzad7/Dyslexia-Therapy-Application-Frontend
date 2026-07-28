package org.example.frontend.therapy.level1

import android.graphics.Color.parseColor
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.frontend.R

val BabyGemoyFont = FontFamily(Font(R.font.baby_gemoy))

@OptIn(UnstableApi::class)
@Composable
fun BouncyLevelScreen(
    viewModel: TherapyViewModel,
    onIntroFinished: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initSession()
    }

    var animationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(3500L)
        animationFinished = true
    }

    LaunchedEffect(animationFinished, viewModel.isLoading.value) {
        if (animationFinished && !viewModel.isLoading.value) {
            if (viewModel.sessionQuestions.isNotEmpty()) {
                onIntroFinished()
            }
        }
    }

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.level1_intro}")
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        BouncyTextOverlay()

        if (animationFinished && viewModel.isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)), // Semi-transparent dim
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF27B51A))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Preparing personalized therapy...", color = Color.White)
                }
            }
        }

        viewModel.errorMessage.value?.let { error ->
            if (animationFinished) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = "Error: $error",
                        color = Color.Red,
                        modifier = Modifier.background(Color.White).padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BouncyTextOverlay() {
    val textToAnimate = "Level 1"
    val yOffsets = remember { textToAnimate.map { Animatable(-1000f) } }

    LaunchedEffect(Unit) {
        yOffsets.forEachIndexed { index, animatable ->
            launch {
                delay(index * 150L)
                animatable.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row {
            textToAnimate.forEachIndexed { index, char ->
                val modifier = if (char == ' ') Modifier else Modifier.offset(y = yOffsets[index].value.dp)
                Text(
                    text = char.toString(),
                    fontFamily = BabyGemoyFont,
                    fontSize = 80.sp,
                    color = Color(parseColor("#FB0FFF")),
                    modifier = modifier
                )
            }
        }
    }
}