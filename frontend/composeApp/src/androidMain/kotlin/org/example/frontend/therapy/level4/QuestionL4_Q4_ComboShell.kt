package org.example.frontend.therapy.level4

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

// --- HANDWRITING CANVAS COMPRESSION ---
fun createBitmapFromPathsQ4(paths: List<Path>, size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 12f
        isAntiAlias = true
        strokeJoin = android.graphics.Paint.Join.ROUND
        strokeCap = android.graphics.Paint.Cap.ROUND
    }
    paths.forEach { canvas.drawPath(it.asAndroidPath(), paint) }
    return bitmap
}

@OptIn(ExperimentalLayoutApi::class, UnstableApi::class)
@Composable
fun QuestionL4_Q4_ComboShell(
    sessionItem: SessionQuestion4,
    uiSequenceNumber: Int,
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
        MiniQuestionTarget("Loading...", "Loading sentence...")
    }

    // Filter characters to determine dynamic FlowRow layout allocation
    val lettersOnly = currentPair.sentence.filter { it.isLetterOrDigit() }
    val drawingState = remember { mutableStateMapOf<Int, List<Path>>() }

    // --- AUDIO RECORDING STATES ---
    val audioRecorder = remember { AudioRecorderHelperQ4(context) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isRecording = true
                audioRecorder.startRecording()
            } else {
                Log.e("Audio", "Microphone permission denied")
            }
        }
    )

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

    // Pre-cache streaming instructions via ExoPlayer
    val instructionPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            sessionItem.audioUrl?.let { url ->
                setMediaItem(MediaItem.fromUri(url))
                prepare()
            }
        }
    }

    DisposableEffect(sessionItem.audioUrl) {
        onDispose {
            instructionPlayer.release()
        }
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

    // Clear internal drawing states and recordings dynamically whenever the index changes
    LaunchedEffect(currentIndex) {
        drawingState.clear()
        recordedFile = null
    }

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
                .width(370.dp)
                .height(570.dp)
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
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                // =========================
                // HEADER
                // =========================
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(62.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val normalizedStage = if (uiSequenceNumber >= 16) uiSequenceNumber - 15 else uiSequenceNumber
                    Text(
                        text = "Question $normalizedStage",
                        Modifier.width(245.dp).height(62.dp),
                        style = TextStyle(
                            fontSize = 34.sp,
                            color = Color(0xFFFF9A62), // Pastel orange accent
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                // =========================
                // INSTRUCTION ROW
                // =========================
                Row(
                    modifier = Modifier.fillMaxWidth().height(90.dp).padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sessionItem.instructionText,
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
                // MAIN COMBO INTERACTION AREA
                // =========================
                Box(
                    Modifier
                        .shadow(14.dp, shape = RoundedCornerShape(32.dp), spotColor = Color(0x40FFB347))
                        .width(350.dp).height(350.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFFFF8F3)
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentPair.sentence,
                                style = TextStyle(
                                    fontSize = 22.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFF8A4E),
                                    textAlign = TextAlign.Center,
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            if (isRecording) {
                                Text("🔴 Rec...", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Thematic Microphone Toggle Button
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(50))
                                    .background(color = Color(0xFFFFB347), shape = RoundedCornerShape(50))
                                    .clickable {
                                        if (!isRecording) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        } else {
                                            recordedFile = audioRecorder.stopRecording()
                                            isRecording = false
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = if (isRecording) R.drawable.pause else R.drawable.play
                                    ),
                                    contentDescription = "Microphone Toggle",
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            }
                        }

                        // Dynamically allocate DrawingBoxes mapped exactly to filtered string length
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)
                        ) {
                            lettersOnly.forEachIndexed { index, _ ->
                                key("${currentIndex}_$index") {
                                    DrawingBoxQ4(
                                        modifier = Modifier.size(45.dp),
                                        onPathsChanged = { newPaths -> drawingState[index] = newPaths }
                                    )
                                }
                            }
                        }

                        // Asynchronous Dual-Pipeline Verification Trigger
                        Box(
                            Modifier
                                .padding(bottom = 16.dp)
                                .width(165.dp).height(56.dp)
                                .shadow(10.dp, shape = RoundedCornerShape(26.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = if (isLoading) {
                                            listOf(Color.Gray, Color.LightGray)
                                        } else {
                                            listOf(Color(0xFFFFC94D), Color(0xFFFF9A62))
                                        }
                                    ),
                                    shape = RoundedCornerShape(26.dp)
                                )
                                .clickable(enabled = !isLoading) {
                                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                                    if (userId != null) {
                                        isLoading = true
                                        val boxSizePx = 200

                                        // 1. Guarantee a valid PNG byte array for every single expected character box
                                        val imageList = lettersOnly.indices.map { idx ->
                                            val paths = drawingState[idx]
                                            val bitmap = if (paths != null && paths.isNotEmpty()) {
                                                createBitmapFromPathsQ4(paths, boxSizePx)
                                            } else {
                                                Bitmap.createBitmap(boxSizePx, boxSizePx, Bitmap.Config.ARGB_8888).apply {
                                                    eraseColor(android.graphics.Color.WHITE)
                                                }
                                            }
                                            val stream = ByteArrayOutputStream()
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                            stream.toByteArray()
                                        }

                                        val isFinalMiniQuestion = (currentIndex == activePairsList.lastIndex)

                                        // --- RE-USE ESTABLISHED WRITING ENDPOINT ---
                                        fun triggerWritingValidation(onComplete: () -> Unit) {
                                            try {
                                                val client = OkHttpClient()
                                                val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

                                                multipartBuilder.addFormDataPart("user_id", userId)
                                                multipartBuilder.addFormDataPart("target_sentence", currentPair.sentence)
                                                multipartBuilder.addFormDataPart("target_word", currentPair.word)
                                                multipartBuilder.addFormDataPart("target_letter", currentPair.word.take(1))
                                                multipartBuilder.addFormDataPart("question_number", sessionItem.dbQuestionNumber.toString())
                                                multipartBuilder.addFormDataPart("is_final_mini", isFinalMiniQuestion.toString())

                                                imageList.forEachIndexed { index, bytes ->
                                                    multipartBuilder.addFormDataPart(
                                                        "images", "char_$index.png",
                                                        bytes.toRequestBody("image/png".toMediaTypeOrNull())
                                                    )
                                                }

                                                val baseUrl = if (ip.startsWith("http")) ip else "http://$ip"
                                                val request = Request.Builder()
                                                    .url("$baseUrl/verify_l4_q2_writing")
                                                    .post(multipartBuilder.build())
                                                    .build()

                                                client.newCall(request).enqueue(object : Callback {
                                                    override fun onFailure(call: Call, e: IOException) {
                                                        Handler(Looper.getMainLooper()).post { onComplete() }
                                                    }
                                                    override fun onResponse(call: Call, response: Response) {
                                                        response.body?.string() // Consume stream securely
                                                        Handler(Looper.getMainLooper()).post { onComplete() }
                                                    }
                                                })
                                            } catch (e: Exception) {
                                                Handler(Looper.getMainLooper()).post { onComplete() }
                                            }
                                        }

                                        // --- RE-USE ESTABLISHED VOICE ENDPOINT ---
                                        fun triggerVoiceValidation(audio: File, onComplete: () -> Unit) {
                                            try {
                                                val client = OkHttpClient()
                                                val requestBody = MultipartBody.Builder()
                                                    .setType(MultipartBody.FORM)
                                                    .addFormDataPart("target_sentence", currentPair.sentence)
                                                    .addFormDataPart("target_word", currentPair.word)
                                                    .addFormDataPart("target_letter", currentPair.word.take(1))
                                                    .addFormDataPart("user_id", userId)
                                                    .addFormDataPart("question_number", sessionItem.dbQuestionNumber.toString())
                                                    .addFormDataPart("is_final_mini", isFinalMiniQuestion.toString())
                                                    .addFormDataPart(
                                                        "audio",
                                                        audio.name,
                                                        audio.asRequestBody("audio/wav".toMediaTypeOrNull())
                                                    )
                                                    .build()

                                                val baseUrl = if (ip.startsWith("http")) ip else "http://$ip"
                                                val request = Request.Builder()
                                                    .url("$baseUrl/verify_l4_q1_voice")
                                                    .post(requestBody)
                                                    .build()

                                                client.newCall(request).enqueue(object : Callback {
                                                    override fun onFailure(call: Call, e: IOException) {
                                                        Handler(Looper.getMainLooper()).post { onComplete() }
                                                    }
                                                    override fun onResponse(call: Call, response: Response) {
                                                        response.body?.string()
                                                        Handler(Looper.getMainLooper()).post { onComplete() }
                                                    }
                                                })
                                            } catch (e: Exception) {
                                                Handler(Looper.getMainLooper()).post { onComplete() }
                                            }
                                        }

                                        // 2. Dispatch Dual-Validation Pipeline Asynchronously
                                        triggerWritingValidation {
                                            if (recordedFile != null) {
                                                triggerVoiceValidation(recordedFile!!) {
                                                    isLoading = false
                                                    if (currentIndex < activePairsList.lastIndex) {
                                                        currentIndex++
                                                    } else {
                                                        onNext()
                                                    }
                                                }
                                            } else {
                                                // Advance securely even if the user skipped the audio toggle
                                                isLoading = false
                                                if (currentIndex < activePairsList.lastIndex) {
                                                    currentIndex++
                                                } else {
                                                    onNext()
                                                }
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
                        text = sessionItem.instructionText,
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
                AsyncImage(
                    model = ImageRequest.Builder(context).data(R.drawable.doraemon).build(),
                    imageLoader = imageLoader,
                    contentDescription = "Character Overlay",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(327.dp).offset(y = (-120).dp).align(Alignment.BottomStart)
                )
            }
        }
    }
}

@Composable
fun DrawingBoxQ4(modifier: Modifier = Modifier, onPathsChanged: (List<Path>) -> Unit) {
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Box(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp))
            .border(width = 1.5.dp, color = Color(0x88FFD6EA), shape = RoundedCornerShape(8.dp))
            .background(color = Color.White).clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> currentPath = Path().apply { moveTo(offset.x, offset.y) } },
                    onDrag = { change, _ ->
                        change.consume()
                        currentPath?.lineTo(change.position.x, change.position.y)
                        val tempPath = Path()
                        currentPath?.let { tempPath.addPath(it) }
                        currentPath = tempPath
                    },
                    onDragEnd = {
                        currentPath?.let {
                            paths.add(it)
                            onPathsChanged(paths.toList())
                        }
                        currentPath = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // CRITICAL VISION AI ALIGNMENT: Drawing paths set to absolute Color.Black to protect backend classification accuracy
            paths.forEach { drawPath(path = it, color = Color.Black, style = Stroke(8f)) }
            currentPath?.let { drawPath(path = it, color = Color.Black, style = Stroke(8f)) }
        }
    }
}

// --- HARDENED AAC AUDIO ENCODER OPTIMIZED FOR WHISPERX ---
class AudioRecorderHelperQ4(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording() {
        audioFile = File(context.cacheDir, "temp_l4_combo_speech.wav")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            // Hardened standard MPEG_4 / AAC encoding guarantees clean WhisperX processing
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