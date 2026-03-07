package org.example.frontend.AssesmentTest.Level3

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt

data class CardItem(
    val id: Int,
    val word: String,
)

@Composable
fun Question12() {
    val ip = NetworkConfig.SERVER_IP
    val context = LocalContext.current
    val overlay_boolean = remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }
    val part_boolean = remember { mutableStateOf(false) }
    val play_boolean = remember { mutableStateOf(false) }
    val cards = remember {
        mutableStateListOf(
            CardItem(1, "deb"),
            CardItem(2, "dib"),
            CardItem(3, "dob"),
            CardItem(4, "bad"),
            CardItem(5, "bed"),
            CardItem(6, "bid"),
            CardItem(7, "bod"),
        )
    }

    // --- AUDIO & NETWORK STATES ---
    val audioRecorder = remember { AudioRecorderHelper(context) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var transcriptionText by remember { mutableStateOf("") }

    // Permission Launcher for Microphone
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                play_boolean.value = true
                audioRecorder.startRecording()
            } else {
                Log.e("Audio", "Microphone permission denied")
            }
        }
    )

    // GIPHY HANDLER
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

    // PRESSING SPEAKER FUNCTION
    fun Clicked_Speaker() {
        overlay_boolean.value = true
        speaker_boolean.value = true
    }
    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel3q12)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(3000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }

    // CARD EFFECT
    var autoDismissTop by remember { mutableStateOf(false) }
    LaunchedEffect(cards.size) {
        if (cards.isNotEmpty()) {
            autoDismissTop = false
            delay(2000)
            autoDismissTop = false
        }
    }

    // DESIGN
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(R.drawable.assessment_level1q3),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Original Screen
        Box(
            modifier = Modifier
                .width(299.dp)
                .height(550.dp)
                .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                .align(Alignment.Center)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Question Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question no 12",
                        style = TextStyle(
                            fontSize = 34.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xF527B51A),
                            textAlign = TextAlign.Center,
                        )
                    )
                }

                // Instruction Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(color = Color(0x00FFFFFF)),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Read the following\n words out loud",
                        style = TextStyle(
                            fontSize = 25.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF27B51A),
                            textAlign = TextAlign.Center,
                        )
                    )
                    // Speaker Button
                    Box(modifier = Modifier.offset(x = 10.dp)) {
                        IconButton(onClick = { Clicked_Speaker() }) {
                            Image(
                                modifier = Modifier.size(35.dp),
                                painter = painterResource(id = R.drawable.sound_button),
                                contentDescription = "Speaker",
                                contentScale = ContentScale.None
                            )
                        }
                    }
                }

                // CARD BOX
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    cards.forEachIndexed { index, card ->
                        val isTopCard = index == cards.lastIndex
                        SwipeCard(
                            modifier = Modifier
                                .graphicsLayer {
                                    val scale = if (isTopCard) 1f else 0.95f
                                    scaleX = scale
                                    scaleY = scale
                                    translationY = (cards.lastIndex - index) * 10f
                                },
                            onDismiss = {
                                if (isTopCard) {
                                    cards.remove(card)
                                    // if (cards.isEmpty()) onNextPage()
                                }
                            },
                            autoDismiss = isTopCard && autoDismissTop,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(55.dp),
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Text(
                                        text = card.word,
                                        style = TextStyle(
                                            fontSize = 50.sp,
                                            fontFamily = FontFamily(Font(R.font.windsol)),
                                            fontWeight = FontWeight(400),
                                            color = Color(0xF527B51A),
                                            textAlign = TextAlign.Center,
                                        )
                                    )
                                }

                                if (!play_boolean.value) {
                                    // PLAY (RECORD) Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(150.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            },
                                            modifier = Modifier.size(110.dp)
                                        ) {
                                            Image(
                                                modifier = Modifier.size(110.dp),
                                                painter = painterResource(id = R.drawable.play_btn),
                                                contentDescription = "Start Recording",
                                                contentScale = ContentScale.None
                                            )
                                        }
                                    }
                                } else {
                                    // PAUSE (STOP) Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(150.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        IconButton(
                                            onClick = {
                                                play_boolean.value = false
                                                recordedFile = audioRecorder.stopRecording()
                                            },
                                            modifier = Modifier.size(110.dp)
                                        ) {
                                            Image(
                                                modifier = Modifier.size(110.dp),
                                                painter = painterResource(id = R.drawable.pause_btn),
                                                contentDescription = "Stop Recording",
                                                contentScale = ContentScale.None
                                            )
                                        }
                                    }
                                }

                                // SUBMIT BUTTON
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(150.dp)
                                            .height(50.dp)
                                            .background(color = Color(0xF527B51A), shape = RoundedCornerShape(size = 35.dp))
                                            .clickable(enabled = !isProcessing && recordedFile != null) {
                                                isProcessing = true
                                                recordedFile?.let { file ->
                                                    uploadAudioForTranscription(file, ip) { result ->
                                                        isProcessing = false
                                                        transcriptionText = result ?: "Error"
                                                        Log.d("FlaskAPI", "WhisperX Result: $transcriptionText")
                                                    }
                                                }
                                            }
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isProcessing) "Sending..." else "Submit",
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color.White,
                                                textAlign = TextAlign.Center,
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // GREY OVERLAY HANDLED BY IF STATEMENT
        if (overlay_boolean.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0x4FFFFFFF))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(color = Color(0x4FFFFFFF))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.speech_bubble),
                            contentDescription = "",
                        )
                        Text(
                            text = "Read the following\n words out loud",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF27B51A),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(R.drawable.doraemon2).build(),
                        imageLoader = imageLoader,
                        contentDescription = "Doraemon GIF",
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
}

// SWIPE CARD COMPONENT
@Composable
fun SwipeCard(
    modifier: Modifier = Modifier,
    swipeThreshold: Float = 150f,
    enabled: Boolean = true,
    onDismiss: () -> Unit,
    autoDismiss: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(autoDismiss) {
        if (autoDismiss) {
            offsetX.animateTo(targetValue = 800f, animationSpec = tween(300))
            alpha.animateTo(targetValue = 0f, animationSpec = tween(300))
            delay(300)
            onDismiss()
        }
    }
    Row(
        modifier = modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDrag = { _, dragAmount ->
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (kotlin.math.abs(offsetX.value) > swipeThreshold) {
                                launch { offsetX.animateTo(targetValue = offsetX.value * 3, animationSpec = tween(300)) }
                                launch { alpha.animateTo(targetValue = 0f, animationSpec = tween(300)) }
                                delay(300)
                                onDismiss()
                            } else {
                                offsetX.animateTo(0f, tween(300))
                                offsetY.animateTo(0f, tween(300))
                            }
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .alpha(alpha.value)
                .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .width(259.dp)
                .height(294.5.dp)
                .background(color = Color(0xE5FFFFFF), shape = RoundedCornerShape(35.dp))
                .padding(10.dp),
            content = content
        )
    }
}

// --- NETWORK HELPER FOR AUDIO ---
fun uploadAudioForTranscription(audioFile: File, serverIp: String, onResult: (String?) -> Unit) {
    val client = OkHttpClient()
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "audio",
            audioFile.name,
            audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
        )
        .build()

    val request = Request.Builder()
        .url("http://$serverIp/transcribe")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        private fun runOnMainThread(action: () -> Unit) {
            Handler(Looper.getMainLooper()).post(action)
        }
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            runOnMainThread { onResult("Network Error: ${e.localizedMessage}") }
        }
        override fun onResponse(call: Call, response: Response) {
            val responseData = response.body?.string()
            runOnMainThread {
                if (response.isSuccessful) onResult(responseData)
                else onResult("Server error: ${response.code}\n$responseData")
            }
        }
    })
}

// --- AUDIO RECORDER HELPER ---
class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording() {
        audioFile = File(context.cacheDir, "temp_speech.wav")
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