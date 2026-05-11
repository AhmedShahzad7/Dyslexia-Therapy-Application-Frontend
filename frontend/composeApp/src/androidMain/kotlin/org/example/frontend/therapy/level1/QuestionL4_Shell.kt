package org.example.frontend.therapy.level1

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import java.io.IOException
import java.util.Locale

// Unified Thematic Colors
private val ThemePinkText = Color(0xFFFF8FC4)
private val ThemeDeepPurple = Color(0xFF7A3E66)
private val SelectionGreen = Color(0xFF33CC66)
private val OptionBorderDefault = Color(0x88FFD6EA)
private val OptionCardDefault = Color(0x66FFFFFF)
private val ErrorRed = Color(0xFFFF3333)

@Composable
fun QuestionL4_Shell(
    sessionItem: SessionQuestion,
    uiSequenceNumber: Int,
    onNext: () -> Unit
) {
    val ip = NetworkConfig.SERVER_IP
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

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

    val primaryTargetClean = remember(sessionItem.targetWord) {
        sessionItem.targetWord.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
    }

    val shuffledArrows = remember(primaryTargetClean) {
        val masterPool = listOf("Up", "Down", "Left", "Right")
        val distractors = masterPool.filter { it != primaryTargetClean }.shuffled().take(3)
        (listOf(primaryTargetClean) + distractors).shuffled()
    }

    val shuffledWords = remember(primaryTargetClean) {
        shuffledArrows.shuffled()
    }

    var selectedArrow by remember { mutableStateOf<String?>(null) }
    var selectedWord by remember { mutableStateOf<String?>(null) }

    val solvedMatches = remember { mutableStateListOf<String>() }
    val wrongArrows = remember { mutableStateListOf<String>() }
    val wrongWords = remember { mutableStateListOf<String>() }

    // --- SILENT BACKEND VERIFICATION HANDLER ---
    fun verifySelection(selectedDirection: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        isVerifying = true

        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", currentUser.uid)
            .addFormDataPart("question_number", sessionItem.dbQuestionNumber.toString())
            .addFormDataPart("target_word", primaryTargetClean)
            .addFormDataPart("arrow_selected", selectedDirection)
            .build()

        val request = Request.Builder()
            .url("http://$ip/verify_therapy_q4")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FlaskAPI_L4", "Verify failed", e)
                Handler(Looper.getMainLooper()).post {
                    isVerifying = false
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Handler(Looper.getMainLooper()).post {
                    isVerifying = false
                }
            }
        })
    }

    fun evaluatePair() {
        val currentArrow = selectedArrow
        val currentWord = selectedWord

        if (currentArrow != null && currentWord != null) {
            if (currentArrow == currentWord) {
                solvedMatches.add(currentArrow)
                Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()

                if (currentArrow == primaryTargetClean && !isVerifying) {
                    scope.launch {
                        delay(500)
                        verifySelection(currentArrow)
                    }
                }
            } else {
                wrongArrows.add(currentArrow)
                wrongWords.add(currentWord)
                Toast.makeText(context, "Incorrect!", Toast.LENGTH_SHORT).show()

                if ((currentArrow == primaryTargetClean || currentWord == primaryTargetClean) && !isVerifying) {
                    scope.launch {
                        delay(500)
                        verifySelection(currentArrow)
                    }
                }
            }
            selectedArrow = null
            selectedWord = null
        }
    }

    fun getBorderColor(itemType: String, direction: String): Color {
        if (solvedMatches.contains(direction)) return ThemeDeepPurple
        if (itemType == "arrow" && wrongArrows.contains(direction)) return ErrorRed
        if (itemType == "word" && wrongWords.contains(direction)) return ErrorRed
        if (itemType == "arrow" && selectedArrow == direction) return SelectionGreen
        if (itemType == "word" && selectedWord == direction) return SelectionGreen
        return OptionBorderDefault
    }

    fun isItemLocked(itemType: String, direction: String): Boolean {
        if (solvedMatches.contains(direction)) return true
        if (itemType == "arrow" && wrongArrows.contains(direction)) return true
        if (itemType == "word" && wrongWords.contains(direction)) return true
        return false
    }

    LaunchedEffect(overlayBoolean.value) {
        if (overlayBoolean.value && sessionItem.audioUrl != null) {
            isMainAudioPlaying = true
            try {
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(sessionItem.audioUrl)
                    prepareAsync()
                    setOnPreparedListener { start() }
                    setOnCompletionListener {
                        release()
                        isMainAudioPlaying = false
                        overlayBoolean.value = false
                    }
                }
            } catch (e: Exception) {
                isMainAudioPlaying = false
                overlayBoolean.value = false
            }
        } else if (overlayBoolean.value) {
            delay(3000)
            overlayBoolean.value = false
        }
    }

    fun streamIndividualWordAudio(word: String) {
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
            Log.e("Audio_L4", "Individual stream failure", e)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ---> UNIFORM LEVEL 1 THEME: Pastel Felt Flat-Lay Background <---
        Image(
            painter = painterResource(id = R.drawable.level1_q1),
            contentDescription = "Thematic Pastel Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // ==========================================
        // UNIFORM GLASSMORPHIC CARD (LEVEL 1 THEME)
        // ==========================================
        Box(
            modifier = Modifier
                .width(330.dp)
                .height(535.dp)
                .shadow(
                    elevation = 25.dp,
                    shape = RoundedCornerShape(38.dp),
                    ambientColor = Color(0x40FFFFFF),
                    spotColor = Color(0x55FF99CC)
                )
                // OUTER GLASS GLOW
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66FFFFFF),
                            Color(0x44FFFFFF),
                            Color(0x22FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                // GLASS BORDER
                .border(
                    width = 1.8.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xAAFFFFFF),
                            Color(0x55FFB6D9),
                            Color(0x44FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                // TRANSLUCENT GLASS EFFECT (40% Milky Opacity)
                .background(
                    color = Color(0x66FFFFFF),
                    shape = RoundedCornerShape(38.dp)
                )
                .blur(0.3.dp)
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ==========================
                // HEADER
                // ==========================
                Text(
                    text = "Question $uiSequenceNumber",
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Bold,
                        color = ThemePinkText,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 20.dp)
                )

                // ==========================
                // QUESTION TEXT AREA
                // ==========================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sessionItem.instructionText,
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF9ECF),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.weight(1f).padding(vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // ===========================================
                    // AUDIO BUTTON (UNIFORM LEVEL 1 SPEAKER ASSET)
                    // ===========================================
                    IconButton(
                        onClick = { overlayBoolean.value = true },
                        enabled = !isMainAudioPlaying && !isVerifying,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.level1_speaker),
                            contentDescription = "Speaker"
                        )
                    }
                }

                // ==========================
                // DYNAMIC ARROW/WORD POOL
                // ==========================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            color = ThemePinkText,
                            modifier = Modifier.size(45.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(35.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Row 1: Rotated Arrows
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                shuffledArrows.forEach { directionStr ->
                                    val rotationDegrees = when (directionStr.lowercase(Locale.ROOT)) {
                                        "down" -> 0f
                                        "left" -> 90f
                                        "up" -> 180f
                                        "right" -> 270f
                                        else -> 0f
                                    }

                                    ArrowItemRotated(
                                        rotationZ = rotationDegrees,
                                        borderColor = getBorderColor("arrow", directionStr),
                                        cardBgColor = if (selectedArrow == directionStr) Color(0xAAFFFFFF) else OptionCardDefault,
                                        onClick = {
                                            if (!isItemLocked("arrow", directionStr) && !isVerifying) {
                                                selectedArrow = directionStr
                                                evaluatePair()
                                            }
                                        }
                                    )
                                }
                            }

                            // Row 2: Dynamic Word Tokens
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                shuffledWords.forEach { wordStr ->
                                    OptionCircleDynamic(
                                        text = wordStr,
                                        borderColor = getBorderColor("word", wordStr),
                                        cardBgColor = if (selectedWord == wordStr) Color(0xAAFFFFFF) else OptionCardDefault,
                                        onOptionClick = {
                                            if (!isItemLocked("word", wordStr) && !isVerifying) {
                                                selectedWord = wordStr
                                                evaluatePair()
                                            }
                                        },
                                        onSoundClick = {
                                            streamIndividualWordAudio(wordStr)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                val totalFinishedPairs = solvedMatches.size + wrongArrows.size

                // ==========================
                // NEXT BUTTON GATEKEEPER
                // ==========================
                Box(
                    modifier = Modifier
                        .padding(bottom = 22.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (totalFinishedPairs >= 4) {
                                    listOf(Color(0xFFFFA7D1), Color(0xFFFF84BF))
                                } else {
                                    listOf(Color.Gray, Color.LightGray)
                                }
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(width = 1.dp, color = Color(0xAAFFFFFF), shape = RoundedCornerShape(24.dp))
                        .clickable {
                            if (totalFinishedPairs >= 4 && !isVerifying) {
                                onNext()
                            } else {
                                Toast.makeText(context, "Finish matching all items first!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 45.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Next",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        // ==================================================
        // UNIFORM CHARACTER OVERLAY (OVERFLOW FIXED)
        // ==================================================
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
                        painter = painterResource(R.drawable.level1_speechbubble),
                        contentDescription = "Speech Bubble"
                    )
                    Text(
                        text = sessionItem.instructionText,
                        // ---> CRITICAL FIX: Increased padding to 32.dp and optimized font size to 15.sp prevents text clipping <---
                        modifier = Modifier.padding(horizontal = 32.dp),
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            color = ThemeDeepPurple,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    )
                }
                AsyncImage(
                    model = ImageRequest.Builder(context).data(R.drawable.doraemon2).build(),
                    imageLoader = imageLoader,
                    contentDescription = "Character Helper GIF",
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
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x20000000),
                spotColor = Color(0x40FF99CC)
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
            contentDescription = "Rotated Target Graphic",
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
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x20000000),
                spotColor = Color(0x40FF99CC)
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
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight.Bold,
                color = ThemeDeepPurple,
                textAlign = TextAlign.Center
            )
        )
        IconButton(
            onClick = { onSoundClick() },
            modifier = Modifier.size(18.dp)
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.level1_speaker),
                contentDescription = "Stream specific word audio",
                contentScale = ContentScale.Fit
            )
        }
    }
}