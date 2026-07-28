package org.example.frontend.quizzes.quiz1

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.frontend.R
import org.example.frontend.quizzes.quiz1.components.LiquidProgressBar

@Composable
fun QuizQuestionL2_Shell(
    questionData: QuizQuestion,
    currentProgress: Float,
    questionNumber: Int,
    onAnswerSubmitted: (ByteArray?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkSpaceNavy = Color(0xFF0A192F)

    val overlayBoolean = remember { mutableStateOf(false) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    val randomizedDirections = remember(questionData) {
        listOf("Up", "Down", "Left", "Right", "NE", "NW", "SE", "SW").shuffled()
    }
    var selectedIndex by remember { mutableStateOf<Int?>(-1) }

    fun getDrawable(dir: String): Int {
        return when (dir.uppercase()) {
            "UP" -> R.drawable.up
            "DOWN" -> R.drawable.down
            "LEFT" -> R.drawable.left
            "RIGHT" -> R.drawable.right
            "NE" -> R.drawable.northeast_arrow
            "NW" -> R.drawable.nothwest_arrow
            "SE" -> R.drawable.southeast_arrow
            "SW" -> R.drawable.southwest_arrow
            else -> R.drawable.left
        }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) { add(ImageDecoderDecoder.Factory()) }
                else { add(GifDecoder.Factory()) }
            }.build()
    }

    LaunchedEffect(overlayBoolean.value) {
        if (overlayBoolean.value && questionData.audioUrl != null) {
            isAudioPlaying = true
            try {
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(questionData.audioUrl)
                    prepareAsync()
                    setOnPreparedListener { start() }
                    setOnCompletionListener {
                        release()
                        isAudioPlaying = false
                        overlayBoolean.value = false
                    }
                    setOnErrorListener { _, _, _ ->
                        release()
                        isAudioPlaying = false
                        overlayBoolean.value = false
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e("Audio", "Playback error", e)
                isAudioPlaying = false
                overlayBoolean.value = false
            }
        } else if (overlayBoolean.value) {
            delay(3000)
            overlayBoolean.value = false
        }
    }

    // MAIN VIEWPLANE
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.quiz1_q2),
            contentDescription = "Cosmic Testing Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .align(Alignment.TopCenter)
        ) {
            LiquidProgressBar(
                progress = currentProgress,
                liquidColor = Color(0xFF00E5FF),
                backgroundColor = darkSpaceNavy.copy(alpha = 0.6f)
            )
        }

        Box(
            modifier = Modifier
                .width(330.dp)
                .height(535.dp)
                .shadow(
                    elevation = 25.dp,
                    shape = RoundedCornerShape(38.dp),
                    ambientColor = Color(0x3300E5FF),
                    spotColor = Color(0x4400E5FF)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEEFFFFFF),
                            Color(0xCCFFFFFF),
                            Color(0xAAFFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xAAFFFFFF),
                            Color(0x6600E5FF),
                            Color(0xAAFFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Display Area
                Text(
                    text = "Question $questionNumber",
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Bold,
                        color = darkSpaceNavy,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 20.dp)
                )

                // Prompt Text & Speaker Trigger Array
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = questionData.instructionText,
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.SemiBold,
                            color = darkSpaceNavy,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.weight(1f).padding(vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = { overlayBoolean.value = true },
                        enabled = !isAudioPlaying,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.quiz1_speaker),
                            contentDescription = "Speaker Trigger"
                        )
                    }
                }

                // Dynamic Arrow Grid Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            color = darkSpaceNavy,
                            modifier = Modifier.size(45.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            randomizedDirections.chunked(3).forEachIndexed { rowIndex, rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowItems.forEachIndexed { colIndex, direction ->
                                        val itemIndex = rowIndex * 3 + colIndex
                                        val isSelected = selectedIndex == itemIndex

                                        val borderColor = if (isSelected) Color(0xFF00E5FF) else Color(0x4400E5FF)
                                        val cardBackground = if (isSelected) Color(0x3300E5FF) else Color(0x66FFFFFF)

                                        Box(
                                            modifier = Modifier
                                                .shadow(
                                                    elevation = 6.dp,
                                                    shape = RoundedCornerShape(22.dp),
                                                    ambientColor = Color(0x2000E5FF),
                                                    spotColor = Color(0x4000E5FF)
                                                )
                                                .width(75.dp)
                                                .height(75.dp)
                                                .background(
                                                    color = cardBackground,
                                                    shape = RoundedCornerShape(22.dp)
                                                )
                                                .border(
                                                    width = 2.5.dp,
                                                    color = borderColor,
                                                    shape = RoundedCornerShape(22.dp)
                                                )
                                                .clipToBounds()
                                                .clickable {
                                                    if (selectedIndex == -1 && !isVerifying) {
                                                        selectedIndex = itemIndex
                                                        isVerifying = true
                                                        scope.launch {
                                                            delay(500)

                                                            val answerPayload = direction.toByteArray()
                                                            onAnswerSubmitted(answerPayload)
                                                        }
                                                    }
                                                }
                                                .padding(14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = getDrawable(direction)),
                                                contentDescription = direction,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (overlayBoolean.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0x4FFFFFFF))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.quiz1_speechbubble),
                        contentDescription = "Speech Bubble"
                    )
                    Text(
                        text = questionData.instructionText,
                        modifier = Modifier.padding(horizontal = 28.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            color = darkSpaceNavy,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    )
                }
                AsyncImage(
                    model = ImageRequest.Builder(context).data(R.drawable.doraemon2).build(),
                    imageLoader = imageLoader,
                    contentDescription = "Character Overlay GIF",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(327.dp).offset(y = (-120).dp).align(Alignment.BottomStart)
                )
            }
        }
    }
}