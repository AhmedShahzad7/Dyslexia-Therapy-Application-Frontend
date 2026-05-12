package org.example.frontend.quizzes.quiz1

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
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
import androidx.compose.ui.graphics.graphicsLayer
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
import java.util.Locale

@Composable
fun QuizQuestionL3_Shell(
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

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    // --- 1. EXPANDED ROTATIONAL ENGINE ---
    // Evaluates both cardinal and intermediate target strings to rotate the base arrow accurately
    val targetRotationDegrees = remember(questionData.targetWord) {
        when (questionData.targetWord.trim().lowercase(Locale.ROOT)) {
            "down" -> 0f
            "sw" -> 45f
            "left" -> 90f
            "nw" -> 135f
            "up" -> 180f
            "ne" -> 225f
            "right" -> 270f
            "se" -> 315f
            else -> 0f
        }
    }

    // --- 2. DYNAMIC DISTRACTOR GENERATOR ---
    // Maps exact conceptual opposites for all 8 spatial paths to generate challenge options
    val buttonOptions = remember(questionData.targetWord) {
        val correctWord = questionData.targetWord.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }

        val distractorWord = when (correctWord.lowercase(Locale.ROOT)) {
            "up" -> "Down"
            "down" -> "Up"
            "left" -> "Right"
            "right" -> "Left"
            "ne" -> "SW"
            "nw" -> "SE"
            "se" -> "NW"
            "sw" -> "NE"
            else -> "Down"
        }.uppercase(Locale.ROOT) // Enforce clean capitalization mapping

        listOf(correctWord.uppercase(Locale.ROOT), distractorWord).shuffled()
    }

    val buttonColors = remember(buttonOptions) {
        mutableStateListOf<Color>().apply {
            buttonOptions.forEach { _ -> add(Color(0x4400E5FF)) } // Subtly tinted base outline
        }
    }

    val buttonBackgrounds = remember(buttonOptions) {
        mutableStateListOf<Color>().apply {
            buttonOptions.forEach { _ -> add(Color(0x33FFFFFF)) }
        }
    }

    // Dynamic Text-to-Speech loop
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
                Log.e("Audio_QuizL3", "Playback error", e)
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
        // --- LAYER 1: Deep Space Aesthetic ---
        Image(
            painter = painterResource(R.drawable.quiz1_q3), // Reuses uniform space backdrop safely
            contentDescription = "Cosmic Testing Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // --- LAYER 2: Absolute Overhead Progress Display ---
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

        // --- LAYER 3: Polished "White Glass" Container ---
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header Area
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

                // Prompt Text & Speaker Module
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
                        enabled = !isAudioPlaying && !isVerifying,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.quiz1_speaker),
                            contentDescription = "Speaker Trigger"
                        )
                    }
                }

                // Rotated Arrow Mount
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            color = darkSpaceNavy,
                            modifier = Modifier.size(45.dp)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.down2),
                            contentDescription = "Target Evaluation Vector",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = targetRotationDegrees
                                }
                        )
                    }
                }

                // Interaction Option Array Stack
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    buttonOptions.forEachIndexed { index, optionText ->
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(25.dp),
                                    ambientColor = Color(0x2000E5FF),
                                    spotColor = Color(0x4000E5FF)
                                )
                                .width(180.dp)
                                .height(55.dp)
                                .background(
                                    color = buttonBackgrounds[index],
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .border(
                                    width = 2.5.dp,
                                    color = buttonColors[index],
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .clipToBounds()
                                .clickable {
                                    if (!isVerifying) {
                                        isVerifying = true
                                        // Cosmic Space Glow: Emphasizes user entry selection cleanly
                                        buttonColors[index] = Color(0xFF00E5FF)
                                        buttonBackgrounds[index] = Color(0x3300E5FF)
                                        scope.launch {
                                            delay(500)
                                            val answerPayload = optionText.toByteArray()
                                            onAnswerSubmitted(answerPayload)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = optionText,
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight.Bold,
                                    color = darkSpaceNavy,
                                    textAlign = TextAlign.Center,
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- LAYER 4: Companion Guidance Frame ---
        if (overlayBoolean.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0x4FFFFFFF))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = (-120).dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.quiz1_speechbubble),
                        contentDescription = "Instruction Dialog Prompt",
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
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.doraemon)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Helper Guidance Animation",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .width(327.dp)
                        .height(327.dp)
                        .offset(y = (-120).dp)
                        .align(Alignment.BottomStart)
                )
            }
        }
    }
}