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
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import org.example.frontend.quizzes.quiz1.components.LiquidProgressBar
import java.util.Locale

@Composable
fun QuizQuestionL4_Shell(
    questionData: QuizQuestion,
    currentProgress: Float,
    questionNumber: Int,
    onAnswerSubmitted: (ByteArray?) -> Unit
) {
    val ip = NetworkConfig.SERVER_IP
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val darkSpaceNavy = Color(0xFF0A192F)

    val overlayBoolean = remember { mutableStateOf(false) }
    var isMainAudioPlaying by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) { add(ImageDecoderDecoder.Factory()) }
                else { add(GifDecoder.Factory()) }
            }.build()
    }

    val primaryTargetClean = remember(questionData.targetWord) {
        questionData.targetWord.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
    }

    // Dynamic pool generation ensuring 4 distinct targets are rendered
    val shuffledArrows = remember(primaryTargetClean) {
        val masterPool = listOf("Up", "Down", "Left", "Right", "NE", "NW", "SE", "SW")
        val distractors = masterPool.filter { it.equals(primaryTargetClean, ignoreCase = true).not() }
            .shuffled().take(3)
        (listOf(primaryTargetClean) + distractors).shuffled()
    }

    val shuffledWords = remember(primaryTargetClean) {
        shuffledArrows.shuffled()
    }

    var selectedArrowToken by remember { mutableStateOf<String?>(null) }
    var selectedWordToken by remember { mutableStateOf<String?>(null) }

    // Silently tracks item state resolution without interrupting quiz progress
    val matchedPairs = remember { mutableStateListOf<String>() }
    val wrongArrows = remember { mutableStateListOf<String>() }
    val wrongWords = remember { mutableStateListOf<String>() }

    // Evaluates localized matching states before dispatching payload upstream
    fun processMatchingAttempt() {
        val currentArrow = selectedArrowToken
        val currentWord = selectedWordToken

        if (currentArrow != null && currentWord != null) {
            if (currentArrow.equals(currentWord, ignoreCase = true)) {
                // Correct pairing logic
                if (!matchedPairs.contains(currentArrow)) {
                    matchedPairs.add(currentArrow)
                }
            } else {
                // Silently record error states to lock the matched items out
                if (!wrongArrows.contains(currentArrow)) wrongArrows.add(currentArrow)
                if (!wrongWords.contains(currentWord)) wrongWords.add(currentWord)
            }

            // Check if all 4 target items on the board have been resolved (correctly or incorrectly)
            val totalResolved = matchedPairs.size + wrongArrows.size
            if (totalResolved >= 4) {
                isVerifying = true
                scope.launch {
                    delay(800) // Brief pause so the user sees their final pairing complete

                    // Determine final payload transmission based on primary target resolution
                    val finalSubmissionPayload = if (matchedPairs.contains(primaryTargetClean)) {
                        primaryTargetClean // Graded as Correct
                    } else {
                        "incorrect_match_submission" // Fails the str comparison check in Flask backend
                    }

                    onAnswerSubmitted(finalSubmissionPayload.toByteArray())
                }
            }

            // Reset active selection token buffers
            selectedArrowToken = null
            selectedWordToken = null
        }
    }

    fun streamOptionAudio(word: String) {
        try {
            val cleanStr = word.lowercase(Locale.ROOT)
            val streamUrl = "http://$ip/static/audio/cached_word_v2_$cleanStr.wav"

            MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(streamUrl)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener { release() }
                setOnErrorListener { _, _, _ ->
                    release()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("Audio_QuizL4", "Option stream error", e)
        }
    }

    LaunchedEffect(overlayBoolean.value) {
        if (overlayBoolean.value && questionData.audioUrl != null) {
            isMainAudioPlaying = true
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
                        isMainAudioPlaying = false
                        overlayBoolean.value = false
                    }
                    setOnErrorListener { _, _, _ ->
                        release()
                        isMainAudioPlaying = false
                        overlayBoolean.value = false
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e("Audio_QuizL4", "Main Playback error", e)
                isMainAudioPlaying = false
                overlayBoolean.value = false
            }
        } else if (overlayBoolean.value) {
            delay(3000)
            overlayBoolean.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.quiz1_q4),
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
                        enabled = !isMainAudioPlaying && !isVerifying,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.quiz1_speaker),
                            contentDescription = "Instruction Speaker Trigger"
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            color = darkSpaceNavy,
                            modifier = Modifier.size(45.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(35.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Row 1: Rotated Graphic Vectors
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                shuffledArrows.forEach { directionStr ->
                                    val rotationDegrees = when (directionStr.lowercase(Locale.ROOT)) {
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

                                    val isMatched = matchedPairs.contains(directionStr)
                                    val isWrong = wrongArrows.contains(directionStr)
                                    val isSelected = selectedArrowToken == directionStr

                                    val borderColor = when {
                                        isMatched -> Color(0xFF27B51A) // Correct Pair (Green)
                                        isWrong -> Color(0xFFE53935)   // Mismatched Pair (Red)
                                        isSelected -> Color(0xFF00E5FF)// Active Highlight (Cyan)
                                        else -> Color(0x4400E5FF)      // Default Idle State
                                    }
                                    val cardBgColor = if (isSelected || isMatched || isWrong) Color(0x3300E5FF) else Color(0x33FFFFFF)

                                    ArrowItemRotated(
                                        rotationZ = rotationDegrees,
                                        borderColor = borderColor,
                                        cardBgColor = cardBgColor,
                                        onClick = {
                                            // Blocks selection if item is already mapped correctly or incorrectly
                                            if (!isVerifying && !isMatched && !isWrong) {
                                                selectedArrowToken = directionStr
                                                processMatchingAttempt()
                                            }
                                        }
                                    )
                                }
                            }

                            // Row 2: Text Tags mapping independent options securely
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                shuffledWords.forEach { wordStr ->
                                    val isMatched = matchedPairs.contains(wordStr)
                                    val isWrong = wrongWords.contains(wordStr)
                                    val isSelected = selectedWordToken == wordStr

                                    val borderColor = when {
                                        isMatched -> Color(0xFF27B51A)
                                        isWrong -> Color(0xFFE53935)
                                        isSelected -> Color(0xFF00E5FF)
                                        else -> Color(0x4400E5FF)
                                    }
                                    val cardBgColor = if (isSelected || isMatched || isWrong) Color(0x3300E5FF) else Color(0x33FFFFFF)

                                    OptionCircleDynamic(
                                        text = wordStr.uppercase(Locale.ROOT),
                                        borderColor = borderColor,
                                        cardBgColor = cardBgColor,
                                        onOptionClick = {
                                            if (!isVerifying && !isMatched && !isWrong) {
                                                selectedWordToken = wordStr
                                                processMatchingAttempt()
                                            }
                                        },
                                        onSoundClick = { streamOptionAudio(wordStr) }
                                    )
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
                        contentDescription = "Instruction Dialog Prompt"
                    )
                    Text(
                        text = questionData.instructionText,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            color = darkSpaceNavy,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    )
                }
                AsyncImage(
                    model = ImageRequest.Builder(context).data(R.drawable.doraemon).build(),
                    imageLoader = imageLoader,
                    contentDescription = "Helper Guidance Animation",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(327.dp).offset(y = (-120).dp).align(Alignment.BottomStart)
                )
            }
        }
    }
}

@Composable
private fun ArrowItemRotated(
    rotationZ: Float,
    borderColor: Color,
    cardBgColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x2000E5FF),
                spotColor = Color(0x4000E5FF)
            )
            .width(58.dp)
            .height(58.dp)
            .background(color = cardBgColor, shape = RoundedCornerShape(22.dp))
            .border(width = 2.5.dp, color = borderColor, shape = RoundedCornerShape(22.dp))
            .clipToBounds()
            .clickable { onClick() }
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.down2),
            contentDescription = "Target Graphic Vector",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { this.rotationZ = rotationZ }
        )
    }
}

@Composable
private fun OptionCircleDynamic(
    text: String,
    borderColor: Color,
    cardBgColor: Color,
    onOptionClick: () -> Unit,
    onSoundClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x2000E5FF),
                spotColor = Color(0x4000E5FF)
            )
            .width(58.dp)
            .height(58.dp)
            .background(color = cardBgColor, shape = RoundedCornerShape(22.dp))
            .border(width = 2.5.dp, color = borderColor, shape = RoundedCornerShape(22.dp))
            .clipToBounds()
            .clickable { onOptionClick() }
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.height(18.dp).fillMaxWidth(),
            text = text,
            style = TextStyle(
                fontSize = 11.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A192F),
                textAlign = TextAlign.Center
            )
        )
        IconButton(
            onClick = { onSoundClick() },
            modifier = Modifier.size(18.dp)
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.quiz1_speaker),
                contentDescription = "Stream individual option voiceover",
                contentScale = ContentScale.Fit
            )
        }
    }
}