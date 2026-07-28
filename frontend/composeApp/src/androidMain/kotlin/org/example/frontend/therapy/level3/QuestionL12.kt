package org.example.frontend.therapy.level3

import android.Manifest
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt

data class CardItem(val id: Int, val word: String)

// ─────────────────────────────────────────────────────────────────────────────
// QUESTION L12  —  "Read the following words out loud"
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuestionL12(onNextScreen: () -> Unit) {
    val CURRENT_QUESTION_NUMBER = 12
    val ip = NetworkConfig.SERVER_IP
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var questionText by remember { mutableStateOf("Read the following\n words out loud") }
    var dynamicAudioUrl by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    // ---> 1. INDEPENDENT LOCAL STATE FOR CARTOON GIF <---
    var cartoonResId by remember { mutableStateOf(R.drawable.mickey1) }

    val overlay_boolean = remember { mutableStateOf(false) }
    val play_boolean = remember { mutableStateOf(false) }
    val cards = remember { mutableStateListOf<CardItem>() }
    val currentUser = FirebaseAuth.getInstance().currentUser

    // ---> 2. LOCAL RESOURCE MAPPER <---
    fun mapCartoonStringToDrawable(cartoon: String): Int {
        return when (cartoon.lowercase().trim()) {
            "mickey" -> R.drawable.mickey1
            "pooh" -> R.drawable.pooh1
            "tom" -> R.drawable.tom1
            "duffy" -> R.drawable.duffy2
            else -> R.drawable.mickey1
        }
    }

    fun setupDefaults() {
        if (cards.isEmpty()) {
            cards.addAll(listOf(
                CardItem(1, "bid"),
                CardItem(2, "dib"),
                CardItem(3, "deb")
            ))
            cartoonResId = R.drawable.mickey1
        }
    }

    // ── Fetch personalised words from backend ─────────────────────────────────
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://$ip/get_personalized_question?user_id=$uid&question_number=$CURRENT_QUESTION_NUMBER")
                .get().build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post { setupDefaults(); isLoading = false }
                }
                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    Handler(Looper.getMainLooper()).post {
                        if (responseData != null) {
                            try {
                                val json = JSONObject(responseData)

                                // ---> 3. PARSE AND MAP SELECTION LOCALLY <---
                                val helperStr = json.optString("cartoon_selection", "mickey")
                                cartoonResId = mapCartoonStringToDrawable(helperStr)

                                dynamicAudioUrl = if (json.isNull("audio_url")) null else json.getString("audio_url")
                                val dataArray = json.optJSONArray("data")
                                if (dataArray != null && dataArray.length() > 0) {
                                    for (i in 0 until dataArray.length())
                                        cards.add(CardItem(i + 1, dataArray.getString(i)))
                                } else { setupDefaults() }
                            } catch (e: Exception) { setupDefaults() }
                        } else { setupDefaults() }
                        isLoading = false
                    }
                }
            })
        } ?: run { setupDefaults(); isLoading = false }
    }

    val audioRecorder = remember { AudioRecorderHelper(context) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) { play_boolean.value = true; audioRecorder.startRecording() }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context).components {
            if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
        }.build()
    }

    // ── Audio overlay ─────────────────────────────────────────────────────────
    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value && !dynamicAudioUrl.isNullOrEmpty()) {
            isAudioPlaying = true
            try {
                val mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA).build()
                    )
                    setDataSource(dynamicAudioUrl)
                    setOnPreparedListener { mp -> mp.start() }
                    setOnCompletionListener { mp ->
                        mp.release(); isAudioPlaying = false; overlay_boolean.value = false
                    }
                    setOnErrorListener { mp, _, _ ->
                        mp.release(); isAudioPlaying = false; overlay_boolean.value = false; true
                    }
                }
                mediaPlayer.prepareAsync()
            } catch (e: Exception) { isAudioPlaying = false; overlay_boolean.value = false }
        } else if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel3q12)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }
            delay(3000)
            overlay_boolean.value = false
        }
    }

    var autoDismissTop by remember { mutableStateOf(false) }
    LaunchedEffect(cards.size) { if (cards.isNotEmpty()) autoDismissTop = false }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.therapy_level3),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFF8335D)
            )
        } else {
            Box(
                modifier = Modifier
                    .width(299.dp).height(550.dp)
                    .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(35.dp))
                    .align(Alignment.Center)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question no $CURRENT_QUESTION_NUMBER",
                            style = TextStyle(
                                fontSize = 34.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFF8335D),
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth().height(100.dp)
                            .background(color = Color.Transparent),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = questionText,
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFF8335D),
                                textAlign = TextAlign.Center
                            )
                        )
                        Box(modifier = Modifier.offset(x = 10.dp)) {
                            IconButton(
                                onClick = { overlay_boolean.value = true },
                                enabled = !isAudioPlaying
                            ) {
                                Image(
                                    modifier = Modifier.size(35.dp),
                                    painter = painterResource(id = R.drawable.sound_button1),
                                    contentDescription = "Speaker",
                                    contentScale = ContentScale.None
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        cards.forEachIndexed { index, card ->
                            val isTopCard = index == cards.lastIndex
                            SwipeCard(
                                modifier = Modifier.graphicsLayer {
                                    val scale = if (isTopCard) 1f else 0.95f
                                    scaleX = scale; scaleY = scale
                                    translationY = (cards.lastIndex - index) * 10f
                                },
                                onDismiss = {
                                    if (isTopCard) {
                                        cards.remove(card)
                                        if (cards.isEmpty()) onNextScreen()
                                    }
                                },
                                autoDismiss = isTopCard && autoDismissTop,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().height(55.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = card.word,
                                            style = TextStyle(
                                                fontSize = 50.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFFF8335D),
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }

                                    if (!play_boolean.value) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(150.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            IconButton(
                                                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                                modifier = Modifier.size(110.dp)
                                            ) {
                                                Image(
                                                    modifier = Modifier.size(110.dp),
                                                    painter = painterResource(id = R.drawable.play_btn1),
                                                    contentDescription = "Start",
                                                    contentScale = ContentScale.None
                                                )
                                            }
                                        }
                                    } else {
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
                                                    painter = painterResource(id = R.drawable.pause_btn1),
                                                    contentDescription = "Stop",
                                                    contentScale = ContentScale.None
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(150.dp).height(50.dp)
                                                .background(
                                                    color = Color(0xFFF8335D),
                                                    shape = RoundedCornerShape(35.dp)
                                                )
                                                .clickable(enabled = !isProcessing && recordedFile != null) {
                                                    currentUser?.uid?.let { userId ->
                                                        isProcessing = true
                                                        recordedFile?.let { audioFile ->
                                                            uploadAudioForTherapy(
                                                                audioFile = audioFile,
                                                                serverIp = ip,
                                                                targetWord = card.word,
                                                                userId = userId,
                                                                questionNumber = CURRENT_QUESTION_NUMBER
                                                            ) { _ ->
                                                                isProcessing = false
                                                                autoDismissTop = true
                                                            }
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
                                                    textAlign = TextAlign.Center
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

            if (overlay_boolean.value) {
                Box(modifier = Modifier.fillMaxSize().background(color = Color(0x4FFFFFFF))) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)
                    ) {
                        Image(painter = painterResource(R.drawable.speech_bubble3), contentDescription = "")
                        Text(
                            text = questionText,
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFF8335D),
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    // ---> 4. USE LOCAL STATE IN ASYNCIMAGE <---
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(cartoonResId) // Passes the local state variable directly
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = "Dynamic Guidance Character Helper",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .size(327.dp).offset(y = (-120).dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Uploads audio to /transcribe_and_score_therapy  (THERAPY endpoint)
// ─────────────────────────────────────────────────────────────────────────────
fun uploadAudioForTherapy(
    audioFile: File,
    serverIp: String,
    targetWord: String,
    userId: String,
    questionNumber: Int,
    onResult: (String?) -> Unit
) {
    try {
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("target_word", targetWord)
            .addFormDataPart("audio", audioFile.name, audioFile.asRequestBody("audio/wav".toMediaTypeOrNull()))
            .addFormDataPart("user_id", userId)
            .addFormDataPart("question_number", questionNumber.toString())
            .build()

        val baseUrl = if (serverIp.startsWith("http")) serverIp else "http://$serverIp"
        val request = Request.Builder()
            .url("$baseUrl/transcribe_and_score_therapy")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post { onResult("Error") }
            }
            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                Handler(Looper.getMainLooper()).post { onResult(res) }
            }
        })
    } catch (e: Exception) {
        Handler(Looper.getMainLooper()).post { onResult("App Error") }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SwipeCard — unchanged from original
// ─────────────────────────────────────────────────────────────────────────────
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
                                launch { offsetX.animateTo(offsetX.value * 3, tween(300)) }
                                launch { alpha.animateTo(0f, tween(300)) }
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
                .width(259.dp).height(294.5.dp)
                .background(color = Color(0xE5FFFFFF), shape = RoundedCornerShape(35.dp))
                .padding(10.dp),
            content = content
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// AudioRecorderHelper — unchanged from original
// ─────────────────────────────────────────────────────────────────────────────
class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording() {
        audioFile = File(context.cacheDir, "temp_speech.wav")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
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
        recorder?.apply { try { stop() } catch (e: Exception) {}; release() }
        recorder = null
        return audioFile
    }
}