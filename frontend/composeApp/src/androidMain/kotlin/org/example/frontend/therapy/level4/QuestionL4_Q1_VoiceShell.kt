package org.example.frontend.therapy.level4

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.media3.common.Player
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import java.io.File
import java.io.IOException

@OptIn(UnstableApi::class)
@Composable
fun QuestionL4_Q1_VoiceShell(
    sessionItem: SessionQuestion4,
    uiSequenceNumber: Int,
    cartoonResId: Int, // ---> INJECTED DYNAMIC GIF ID <---
    onNext: () -> Unit
) {
    val context = LocalContext.current
    val overlayBoolean = remember { mutableStateOf(false) }
    val ip = NetworkConfig.SERVER_IP

    var currentIndex by rememberSaveable { mutableStateOf(0) }

    // UI Interaction Lock prevents multiple overlapping audio network executions
    var isSpeakerDisabled by remember { mutableStateOf(false) }

    // Dynamically paired word and sentence extraction for the active progression step
    val activePairsList = sessionItem.miniQuestions
    val currentPair = if (currentIndex < activePairsList.size) {
        activePairsList[currentIndex]
    } else {
        MiniQuestionTarget("Loading...", "Loading sentence...")
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) { add(ImageDecoderDecoder.Factory()) }
                else { add(GifDecoder.Factory()) }
            }
            .build()
    }

    // --- STABILIZED VIEWMODEL AUDIO STREAMING BLOCK ---
    // 1. Mount the player without pre-fetching media streams
    val instructionPlayer = remember(sessionItem.audioUrl) {
        ExoPlayer.Builder(context).build().apply {
            sessionItem.audioUrl?.let { url ->
                setMediaItem(MediaItem.fromUri(url))
                // Notice prepare() is REMOVED here. Sockets remain closed.
            }
        }
    }

    DisposableEffect(sessionItem.audioUrl) {
        onDispose { instructionPlayer.release() }
    }

    // 2. Open sockets and prepare streams ONLY on explicit interaction
    fun clickedSpeaker() {
        val isPlayingOrBuffering = instructionPlayer.isPlaying ||
                instructionPlayer.playbackState == Player.STATE_BUFFERING

        if (isSpeakerDisabled || isPlayingOrBuffering) return

        isSpeakerDisabled = true
        overlayBoolean.value = true

        // If the player hasn't prepared the stream yet, prepare it now
        if (instructionPlayer.playbackState == Player.STATE_IDLE) {
            instructionPlayer.prepare()
        }

        instructionPlayer.seekTo(0)
        instructionPlayer.play()

        Handler(Looper.getMainLooper()).postDelayed({
            isSpeakerDisabled = false
        }, 500)
    }

    LaunchedEffect(overlayBoolean.value) {
        if (overlayBoolean.value) {
            delay(5000)
            overlayBoolean.value = false
        }
    }

    val isPlaying = remember { mutableStateOf(false) }
    val audioRecorder = remember { AudioRecorderHelperL4(context) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isPlaying.value = true
                audioRecorder.startRecording()
            } else {
                Log.e("Audio", "Microphone permission denied")
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ---> THEMATIC BACKGROUND: Mapped to the specified level4_q1 composition <---
        Image(
            painter = painterResource(id = R.drawable.level4_q1),
            contentDescription = "Thematic Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // ==========================================
        // UNIFORM GLASSMORPHIC CARD (LEVEL 4 THEME)
        // ==========================================
        Box(
            modifier = Modifier
                .width(330.dp)
                .height(540.dp)
                .shadow(
                    elevation = 25.dp,
                    shape = RoundedCornerShape(38.dp),
                    ambientColor = Color(0x30FFFFFF),
                    spotColor = Color(0x55FFB347)
                )
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
                .background(
                    color = Color(0x66FFFFFF),
                    shape = RoundedCornerShape(38.dp)
                )
                .blur(0.3.dp)
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // =========================
                // HEADER
                // =========================
                val normalizedStage = if (uiSequenceNumber >= 16) uiSequenceNumber - 15 else uiSequenceNumber
                Text(
                    text = "Question $normalizedStage",
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9A62),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 18.dp)
                )

                // =========================
                // INSTRUCTION ROW
                // =========================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sessionItem.instructionText,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF9A62),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.weight(1f).padding(vertical = 14.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Audio Button implementation mapping execution locks cleanly
                    IconButton(
                        onClick = { clickedSpeaker() },
                        enabled = !isSpeakerDisabled, // Disable natively if processing
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
                // MAIN VOICE CARD AREA
                // =========================
                Box(
                    modifier = Modifier
                        .width(270.dp)
                        .height(310.dp)
                        .shadow(
                            elevation = 14.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = Color(0x40FFB347)
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFFFF8F3)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentPair.sentence,
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF8A4E),
                                textAlign = TextAlign.Center
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(95.dp)
                                .shadow(elevation = 12.dp, shape = RoundedCornerShape(50))
                                .background(color = Color(0xFFFFB347), shape = RoundedCornerShape(50))
                                .clickable {
                                    if (!isPlaying.value) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        isPlaying.value = false
                                        recordedFile = audioRecorder.stopRecording()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (isPlaying.value) R.drawable.pause else R.drawable.play
                                ),
                                contentDescription = "Microphone Status Trigger",
                                modifier = Modifier.size(45.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(165.dp)
                                .height(56.dp)
                                .shadow(elevation = 10.dp, shape = RoundedCornerShape(26.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = if (isProcessing || recordedFile == null) {
                                            listOf(Color.Gray, Color.LightGray)
                                        } else {
                                            listOf(Color(0xFFFFC94D), Color(0xFFFF9A62))
                                        }
                                    ),
                                    shape = RoundedCornerShape(26.dp)
                                )
                                .clickable(enabled = !isProcessing && recordedFile != null) {
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId != null && recordedFile != null) {
                                        isProcessing = true
                                        val isFinalMiniQuestion = (currentIndex == activePairsList.lastIndex)

                                        uploadSentenceVoicePayload(
                                            audioFile = recordedFile!!,
                                            serverIp = ip,
                                            targetSentence = currentPair.sentence,
                                            targetWord = currentPair.word,
                                            targetLetter = currentPair.word.firstOrNull()?.toString() ?: "b",
                                            dbQuestionNumber = sessionItem.dbQuestionNumber.toString(),
                                            userId = userId,
                                            isFinalMini = isFinalMiniQuestion
                                        ) { result ->
                                            isProcessing = false
                                            Log.d("TherapyVoice", "Scored Payload: $result")

                                            if (currentIndex < activePairsList.lastIndex) {
                                                currentIndex++
                                                recordedFile = null
                                            } else {
                                                onNext()
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isProcessing) "Scoring..." else "Submit",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
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
                        painter = painterResource(R.drawable.level4_speechbubble),
                        contentDescription = "Speech Bubble"
                    )
                    Text(
                        text = sessionItem.instructionText,
                        modifier = Modifier.padding(horizontal = 30.dp),
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF9C4F2D),
                            textAlign = TextAlign.Center
                        )
                    )
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(cartoonResId) // Passes injected integer identifier cleanly
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Dynamic Helper Animation",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(327.dp).offset(y = (-120).dp).align(Alignment.BottomStart)
                )
            }
        }
    }
}

// --- NETWORK HELPER MATCHING FULL SENTENCES & PASSING TARGET WORD ---
fun uploadSentenceVoicePayload(
    audioFile: File,
    serverIp: String,
    targetSentence: String,
    targetWord: String,
    targetLetter: String,
    dbQuestionNumber: String,
    userId: String,
    isFinalMini: Boolean,
    onResult: (String?) -> Unit
) {
    try {
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("target_sentence", targetSentence)
            .addFormDataPart("target_word", targetWord)
            .addFormDataPart("target_letter", targetLetter)
            .addFormDataPart("user_id", userId)
            .addFormDataPart("question_number", dbQuestionNumber)
            .addFormDataPart("is_final_mini", isFinalMini.toString())
            .addFormDataPart(
                "audio",
                audioFile.name,
                audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
            )
            .build()

        val baseUrl = if (serverIp.startsWith("http")) serverIp else "http://$serverIp"

        val request = Request.Builder()
            .url("$baseUrl/verify_l4_q1_voice")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post { onResult("Network Error: ${e.message}") }
            }
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                Handler(Looper.getMainLooper()).post {
                    if (response.isSuccessful) onResult(responseData)
                    else onResult("Server error: ${response.code}")
                }
            }
        })
    } catch (e: Exception) {
        Handler(Looper.getMainLooper()).post { onResult("App Error: ${e.message}") }
    }
}

class AudioRecorderHelperL4(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording() {
        audioFile = File(context.cacheDir, "temp_l4_sentence_speech.wav")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }
    }

    fun stopRecording(): File? {
        recorder?.apply {
            try { stop() } catch (e: Exception) { e.printStackTrace() }
            release()
        }
        recorder = null
        return audioFile
    }
}