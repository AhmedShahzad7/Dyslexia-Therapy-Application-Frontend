package org.example.frontend.therapy.level4

import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import okhttp3.*
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import java.io.IOException

@OptIn(ExperimentalLayoutApi::class, UnstableApi::class)
@Composable
fun QuestionL4_Q3_GridShell(
    sessionItem: SessionQuestion4,
    uiSequenceNumber: Int,
    cartoonResId: Int, // ---> INJECTED DYNAMIC GIF ID <---
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val overlayBoolean = remember { mutableStateOf(false) }
    val ip = NetworkConfig.SERVER_IP

    var currentIndex by rememberSaveable { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    // Dynamically pull the active mini-question pairing for this specific stage
    val activePairsList = sessionItem.miniQuestions
    val currentPair = if (currentIndex < activePairsList.size) {
        activePairsList[currentIndex]
    } else {
        MiniQuestionTarget("Loading...", "Loading grid...")
    }

    // Derived instruction text updates seamlessly when currentIndex changes
    val dynamicInstruction by remember(currentIndex) {
        derivedStateOf { "Find and click the word \"${currentPair.word}\" in the grid" }
    }

    // ---> CRITICAL FIX: Aggressively strip out trailing full stops and punctuation from spawned tokens <---
    val wordsList = remember(currentPair.sentence) {
        currentPair.sentence
            .split(" ")
            .filter { it.isNotBlank() }
            .map { rawToken ->
                rawToken.replace(Regex("[^\\w]"), "")
            }
            .filter { it.isNotEmpty() }
    }

    val selectedIndices = remember { mutableStateListOf<Int>() }

    // Clear active grid selections dynamically whenever the internal step increments
    LaunchedEffect(currentIndex) {
        selectedIndices.clear()
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }
            .build()
    }

    // --- SHARED VIEWMODEL AUDIO STREAMING ---
    val instructionPlayer = remember { ExoPlayer.Builder(context).build() }

    LaunchedEffect(sessionItem.audioUrl) {
        sessionItem.audioUrl?.let { url ->
            instructionPlayer.apply {
                stop()
                clearMediaItems()
                setMediaItem(MediaItem.fromUri(url))
                prepare()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { instructionPlayer.release() }
    }

    fun clickedSpeaker() {
        overlayBoolean.value = true
        instructionPlayer.seekTo(0)
        instructionPlayer.play()
    }

    LaunchedEffect(overlayBoolean.value) {
        if (overlayBoolean.value) {
            delay(5000)
            overlayBoolean.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ---> THEMATIC BACKGROUND: Mapped to the specified level4_q3 composition <---
        Image(
            painter = painterResource(id = R.drawable.level4_q3),
            contentDescription = "Thematic Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // ==========================================
        // UNIFORM GLASSMORPHIC CARD (LEVEL 4 THEME)
        // ==========================================
        Box(
            modifier = Modifier
                .width(370.dp)
                .height(670.dp)
                .shadow(
                    elevation = 25.dp,
                    shape = RoundedCornerShape(38.dp),
                    ambientColor = Color(0x30FFFFFF),
                    spotColor = Color(0x55FFB347) // Soft orange ambient glow
                )
                // OUTER GLASS GLOW
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x88FFFFFF),
                            Color(0x55FFF7F2),
                            Color(0x33FFFFFF)
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
                            Color(0x55FFB347),
                            Color(0x44FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                // TRANSLUCENT GLASS EFFECT (40% milky opacity base)
                .background(
                    color = Color(0x66FFFFFF),
                    shape = RoundedCornerShape(38.dp)
                )
                .blur(0.3.dp)
                .align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize().padding(bottom = 20.dp)
            ) {
                // =========================
                // HEADER
                // =========================
                val normalizedStage = if (uiSequenceNumber >= 16) uiSequenceNumber - 15 else uiSequenceNumber
                Text(
                    text = "Question $normalizedStage",
                    modifier = Modifier.padding(top = 20.dp),
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9A62), // Pastel orange accent
                        textAlign = TextAlign.Center
                    )
                )

                // =========================
                // INSTRUCTION ROW
                // =========================
                Row(
                    modifier = Modifier.fillMaxWidth().height(90.dp).padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dynamicInstruction,
                        modifier = Modifier.weight(1f),
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF9A62),
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Stripped outer wrapper boxes to mount the pre-colored SVG cleanly
                    IconButton(
                        onClick = { clickedSpeaker() },
                        modifier = Modifier.size(50.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.level4_speaker),
                            contentDescription = "Speaker",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // =========================
                // DYNAMIC WORD GRID AREA
                // =========================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FlowRow(
                        maxItemsInEachRow = 4,
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        wordsList.forEachIndexed { index, word ->
                            key("${currentIndex}_$index") {
                                WordBox(
                                    word = word,
                                    targetWord = currentPair.word,
                                    isSelected = selectedIndices.contains(index),
                                    onClick = {
                                        if (selectedIndices.contains(index)) selectedIndices.remove(index)
                                        else selectedIndices.add(index)
                                    },
                                    modifier = Modifier.width(72.dp).height(50.dp)
                                )
                            }
                        }
                    }
                }

                // =========================
                // SUBMIT BUTTON
                // =========================
                // Allows unhindered payload dispatch to protect clinical validity
                Box(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .width(165.dp)
                        .height(56.dp)
                        .shadow(10.dp, shape = RoundedCornerShape(26.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (isLoading || selectedIndices.isEmpty()) {
                                    listOf(Color.Gray, Color.LightGray)
                                } else {
                                    listOf(Color(0xFFFFC94D), Color(0xFFFF9A62))
                                }
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .clickable(enabled = !isLoading && selectedIndices.isNotEmpty()) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                isLoading = true
                                val selectedWords = selectedIndices.map { wordsList[it] }
                                val isFinalMiniQuestion = (currentIndex == activePairsList.lastIndex)

                                uploadDynamicGridPayload(
                                    answers = selectedWords,
                                    serverIp = ip,
                                    targetSentence = wordsList.joinToString(" "),
                                    targetWord = currentPair.word,
                                    targetLetter = currentPair.word.take(1),
                                    dbQuestionNumber = sessionItem.dbQuestionNumber.toString(),
                                    uId = userId,
                                    isFinalMini = isFinalMiniQuestion
                                ) { result ->
                                    isLoading = false
                                    Log.d("TherapyGrid", "Scored Payload: $result")

                                    if (currentIndex < activePairsList.lastIndex) {
                                        currentIndex++
                                    } else {
                                        onNext()
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoading) "Scoring..." else "Submit",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ==================================================
        // UNIFORM CHARACTER OVERLAY (OVERFLOW FIXED)
        // ==================================================
        if (overlayBoolean.value) {
            Box(modifier = Modifier.fillMaxSize().background(color = Color(0x4FFFFFFF))) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)) {
                    Image(painter = painterResource(R.drawable.level4_speechbubble), contentDescription = "Speech Bubble")
                    Text(
                        text = dynamicInstruction,
                        // Guaranteed overflow protection via explicit padding and boundary scaling
                        modifier = Modifier.padding(horizontal = 30.dp),
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9C4F2D), // Deep high-contrast warm orange-brown
                            textAlign = TextAlign.Center
                        )
                    )
                }

                // ---> SWAPPED HARDCODED IMAGE REFERENCE FOR DYNAMIC HELPER STATE <---
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(cartoonResId) // Connects directly to integer resource state
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Dynamic Companion Overlay Helper",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(327.dp).offset(y = (-120).dp).align(Alignment.BottomStart)
                )
            }
        }
    }
}

@Composable
fun WordBox(
    word: String,
    targetWord: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Colors mapped to the global glossy theme
    val statusBorderColor = if (isSelected) {
        if (word.equals(targetWord, ignoreCase = true)) Color(0xFF33CC66) else Color(0xFFFF3333)
    } else {
        Color(0x88FFD6EA)
    }

    val statusBgColor = if (isSelected) {
        if (word.equals(targetWord, ignoreCase = true)) Color(0xAAFFFFFF) else Color(0x88FFCCCC)
    } else {
        Color(0x66FFFFFF)
    }

    Column(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x40FFB347))
            .background(color = statusBgColor, shape = RoundedCornerShape(16.dp))
            .border(width = 2.dp, color = statusBorderColor, shape = RoundedCornerShape(16.dp))
            .clipToBounds()
            .clickable { onClick() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = word,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A3E66), // Uniform high-contrast overlay text color
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
    }
}

fun uploadDynamicGridPayload(
    answers: List<String>,
    serverIp: String,
    targetSentence: String,
    targetWord: String,
    targetLetter: String,
    dbQuestionNumber: String,
    uId: String,
    isFinalMini: Boolean,
    onResult: (String?) -> Unit
) {
    try {
        val client = OkHttpClient()
        val jsonAnswers = answers.joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", uId)
            .addFormDataPart("target_sentence", targetSentence)
            .addFormDataPart("target_word", targetWord)
            .addFormDataPart("target_letter", targetLetter)
            .addFormDataPart("question_number", dbQuestionNumber)
            .addFormDataPart("is_final_mini", isFinalMini.toString())
            .addFormDataPart("answers_list", jsonAnswers)
            .build()

        val baseUrl = if (serverIp.startsWith("http")) serverIp else "http://$serverIp"
        val request = Request.Builder()
            .url("$baseUrl/verify_l4_q3_grid")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post { onResult("Error: ${e.message}") }
            }
            override fun onResponse(call: Call, response: Response) {
                val resData = response.body?.string()
                Handler(Looper.getMainLooper()).post {
                    if (response.isSuccessful) onResult(resData)
                    else onResult("Server error: ${response.code}")
                }
            }
        })
    } catch (e: Exception) {
        Handler(Looper.getMainLooper()).post { onResult("App Error: ${e.message}") }
    }
}