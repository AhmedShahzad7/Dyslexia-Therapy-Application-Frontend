package org.example.frontend.therapy.Level2

import android.graphics.Color.parseColor
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
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

// Your custom font
val BabyGemoyFont = FontFamily(Font(R.font.baby_gemoy))

@OptIn(UnstableApi::class)
@Composable
fun BouncyLevel2Screen(onNextScreen: ()->Unit) {
    val context = LocalContext.current

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Point to the video in your raw folder
            val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.level2_intro}")
            setMediaItem(MediaItem.fromUri(videoUri))

            // Loop the video indefinitely and mute it so it acts purely as a background
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }

    // Auto navigate after animation + 3 seconds
    LaunchedEffect(Unit) {

        // Wait for text animation to complete
        // 150ms delay between letters + spring animation time
        delay(2500)

        // Wait additional 3 seconds
        delay(3000)

        onNextScreen()
    }

    // Clean up the player when the screen is destroyed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Use a Box to layer the text ON TOP of the video
    Box(modifier = Modifier.fillMaxSize()) {

        // --- BOTTOM LAYER: Background Video ---
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Hide play/pause buttons
                    // Crop/Zoom the video to fill the entire screen
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // --- TOP LAYER: Your Bouncy Text ---
        // (This is your exact bouncy text code, just placed inside the Box)
        BouncyTextOverlayLevel2()
    }
}

@Composable
fun BouncyTextOverlayLevel2() {
    val textToAnimate = "Level 2"
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

    // Align the text in the center of the screen over the video
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
                    fontSize = 80.sp, // Made slightly bigger for impact against a video
                    color = Color(parseColor("#BF72D8")), // White often reads better over video backgrounds
                    modifier = modifier
                )
            }
        }
    }
}