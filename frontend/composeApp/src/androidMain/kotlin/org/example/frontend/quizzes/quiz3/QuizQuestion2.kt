package org.example.frontend.quizzes.quiz3

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

// ─────────────────────────────────────────────────────────────────────────────
// QUIZ QUESTION 2  —  "Read the following words out loud"  (Read Aloud)
//
// FIX: Two changes vs the original
//   1. Fetches with question_number=2  (backend routes to read-aloud branch)
//   2. After the speech upload, also calls /submit_quiz_answer so every
//      card attempt is counted toward the quiz 75% pass flag.
//      question_number=2 is stored in Firestore.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuizQuestion2(onNextScreen: () -> Unit) {

    val FETCH_Q_NUM  = "2"   // backend routes to read-aloud word list
    val SUBMIT_Q_NUM = 2     // stored in Firestore for the 75% calculation

    val ip      = NetworkConfig.SERVER_IP
    val context = LocalContext.current

    var isLoading       by remember { mutableStateOf(true) }
    var questionText    by remember { mutableStateOf("Read the following\n words out loud") }
    var dynamicAudioUrl by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying  by remember { mutableStateOf(false) }
    var isProcessing    by remember { mutableStateOf(false) }
    var recordedFile    by remember { mutableStateOf<File?>(null) }
    var autoDismissTop  by remember { mutableStateOf(false) }

    val overlayBoolean = remember { mutableStateOf(false) }
    val playBoolean    = remember { mutableStateOf(false) }
    val cards          = remember { mutableStateListOf<CardItem>() }
    val currentUser    = FirebaseAuth.getInstance().currentUser

    fun setupDefaults() {
        if (cards.isEmpty()) {
            cards.addAll(listOf(
                CardItem(1, "bid"),
                CardItem(2, "dib"),
                CardItem(3, "deb")
            ))
        }
    }

    // ── Fetch word list ───────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            val client  = OkHttpClient()
            val request = Request.Builder()
                .url("http://$ip/get_personalized_question_quiz3?user_id=$uid&question_number=$FETCH_Q_NUM")
                .get().build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post { setupDefaults(); isLoading = false }
                }
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    Handler(Looper.getMainLooper()).post {
                        if (body != null) {
                            try {
                                val json = JSONObject(body)
                                dynamicAudioUrl = if (json.isNull("audio_url")) null
                                else json.getString("audio_url")
                                // Backend returns "data": ["word1","word2",...]
                                val dataArray = json.optJSONArray("data")
                                if (dataArray != null && dataArray.length() > 0) {
                                    for (i in 0 until dataArray.length())
                                        cards.add(CardItem(i + 1, dataArray.getString(i)))
                                } else setupDefaults()
                            } catch (e: Exception) { setupDefaults() }
                        } else setupDefaults()
                        isLoading = false
                    }
                }
            })
        } ?: run { setupDefaults(); isLoading = false }
    }

    val audioRecorder = remember { Quiz2AudioRecorder(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) { playBoolean.value = true; audioRecorder.startRecording() }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context).components {
            if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
        }.build()
    }

    LaunchedEffect(cards.size) { if (cards.isNotEmpty()) autoDismissTop = false }

    // ── Doraemon / audio overlay ──────────────────────────────────────────────
    LaunchedEffect(overlayBoolean.value) {
        if (overlayBoolean.value && !dynamicAudioUrl.isNullOrEmpty()) {
            isAudioPlaying = true
            try {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA).build()
                    )
                    setDataSource(dynamicAudioUrl)
                    setOnPreparedListener  { it.start() }
                    setOnCompletionListener { it.release(); isAudioPlaying = false; overlayBoolean.value = false }
                    setOnErrorListener      { it, _, _ -> it.release(); isAudioPlaying = false; overlayBoolean.value = false; true }
                }
                mp.prepareAsync()
            } catch (e: Exception) { isAudioPlaying = false; overlayBoolean.value = false }
        } else if (overlayBoolean.value) {
            val mp = MediaPlayer.create(context, R.raw.doraemon_alevel3q12)
            mp.start()
            mp.setOnCompletionListener { it.release() }
            delay(3000)
            overlayBoolean.value = false
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter            = painterResource(R.drawable.therapy_level3),
            contentDescription = "",
            contentScale       = ContentScale.FillBounds,
            modifier           = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color    = Color(0xFFF8335D)
            )
        } else {
            Box(
                modifier = Modifier
                    .width(299.dp).height(550.dp)
                    .background(Color(0xC7FFFFFF), RoundedCornerShape(35.dp))
                    .align(Alignment.Center)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().height(100.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Quiz 3 Question 2",
                            style = TextStyle(
                                fontSize   = 34.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                    }

                    Row(
                        modifier              = Modifier
                            .fillMaxWidth().height(100.dp)
                            .background(Color.Transparent),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text  = questionText,
                            style = TextStyle(
                                fontSize   = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                        Box(modifier = Modifier.offset(x = 10.dp)) {
                            IconButton(
                                onClick  = { overlayBoolean.value = true },
                                enabled  = !isAudioPlaying
                            ) {
                                Image(
                                    modifier           = Modifier.size(35.dp),
                                    painter            = painterResource(R.drawable.sound_button1),
                                    contentDescription = "Speaker",
                                    contentScale       = ContentScale.None
                                )
                            }
                        }
                    }

                    // Swipeable word cards
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        cards.forEachIndexed { index, card ->
                            val isTopCard = index == cards.lastIndex
                            Quiz2SwipeCard(
                                modifier = Modifier.graphicsLayer {
                                    val scale = if (isTopCard) 1f else 0.95f
                                    scaleX = scale; scaleY = scale
                                    translationY = (cards.lastIndex - index) * 10f
                                },
                                onDismiss   = {
                                    if (isTopCard) {
                                        cards.remove(card)
                                        if (cards.isEmpty()) onNextScreen()
                                    }
                                },
                                autoDismiss = isTopCard && autoDismissTop
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        modifier              = Modifier.fillMaxWidth().height(55.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text  = card.word,
                                            style = TextStyle(
                                                fontSize   = 50.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color      = Color(0xFFF8335D),
                                                textAlign  = TextAlign.Center
                                            )
                                        )
                                    }

                                    Row(
                                        modifier              = Modifier.fillMaxWidth().height(150.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (!playBoolean.value) {
                                            IconButton(
                                                onClick  = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                                modifier = Modifier.size(110.dp)
                                            ) {
                                                Image(
                                                    modifier           = Modifier.size(110.dp),
                                                    painter            = painterResource(R.drawable.play_btn1),
                                                    contentDescription = "Start",
                                                    contentScale       = ContentScale.None
                                                )
                                            }
                                        } else {
                                            IconButton(
                                                onClick  = {
                                                    playBoolean.value = false
                                                    recordedFile = audioRecorder.stopRecording()
                                                },
                                                modifier = Modifier.size(110.dp)
                                            ) {
                                                Image(
                                                    modifier           = Modifier.size(110.dp),
                                                    painter            = painterResource(R.drawable.pause_btn1),
                                                    contentDescription = "Stop",
                                                    contentScale       = ContentScale.None
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier              = Modifier.fillMaxWidth().padding(top = 10.dp),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(150.dp).height(50.dp)
                                                .background(Color(0xFFF8335D), RoundedCornerShape(35.dp))
                                                .clickable(enabled = !isProcessing && recordedFile != null) {
                                                    currentUser?.uid?.let { userId ->
                                                        isProcessing = true
                                                        val audioFile = recordedFile ?: return@clickable

                                                        // Step 1: upload audio for transcription + therapy scoring
                                                        quiz2UploadAudio(
                                                            audioFile      = audioFile,
                                                            serverIp       = ip,
                                                            targetWord     = card.word,
                                                            userId         = userId,
                                                            questionNumber = SUBMIT_Q_NUM
                                                        ) { isCorrect ->
                                                            // Step 2: also post to /submit_quiz_answer
                                                            // so this card counts toward the 75% pass flag
                                                            submitTherapyAnswer(
                                                                uid     = userId,
                                                                qNum    = SUBMIT_Q_NUM,
                                                                target  = card.word,
                                                                correct = isCorrect
                                                            ) {
                                                                isProcessing   = false
                                                                autoDismissTop = true
                                                            }
                                                        }
                                                    }
                                                }
                                                .padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text  = if (isProcessing) "Sending..." else "Submit",
                                                style = TextStyle(
                                                    fontSize   = 24.sp,
                                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                                    fontWeight = FontWeight(400),
                                                    color      = Color.White,
                                                    textAlign  = TextAlign.Center
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

            // Doraemon overlay
            if (overlayBoolean.value) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x4FFFFFFF))) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)
                    ) {
                        Image(painterResource(R.drawable.speech_bubble3), contentDescription = "")
                        Text(
                            text  = questionText,
                            style = TextStyle(
                                fontSize   = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                    }
                    AsyncImage(
                        model              = ImageRequest.Builder(context).data(R.drawable.doraemon2).build(),
                        imageLoader        = imageLoader,
                        contentDescription = "Doraemon",
                        contentScale       = ContentScale.FillBounds,
                        modifier           = Modifier
                            .size(327.dp).offset(y = (-120).dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Upload audio to /transcribe_and_score_therapy, then report back isCorrect
// so the caller can separately post to /submit_quiz_answer.
// ─────────────────────────────────────────────────────────────────────────────
private fun quiz2UploadAudio(
    audioFile: File,
    serverIp: String,
    targetWord: String,
    userId: String,
    questionNumber: Int,
    onResult: (isCorrect: Boolean) -> Unit
) {
    try {
        val client  = OkHttpClient()
        val baseUrl = if (serverIp.startsWith("http")) serverIp else "http://$serverIp"
        val body    = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("target_word",     targetWord)
            .addFormDataPart("user_id",         userId)
            .addFormDataPart("question_number", questionNumber.toString())
            .addFormDataPart("audio", audioFile.name, audioFile.asRequestBody("audio/wav".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url("$baseUrl/transcribe_and_score_therapy")
            .post(body).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // On failure treat as incorrect so attempt still counts
                Handler(Looper.getMainLooper()).post { onResult(false) }
            }
            override fun onResponse(call: Call, response: Response) {
                val respBody = response.body?.string() ?: ""
                // Backend returns {"is_correct": true/false, ...}
                val isCorrect = try {
                    org.json.JSONObject(respBody).optBoolean("is_correct", false)
                } catch (e: Exception) { false }
                Handler(Looper.getMainLooper()).post { onResult(isCorrect) }
            }
        })
    } catch (e: Exception) {
        Handler(Looper.getMainLooper()).post { onResult(false) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Swipe card (identical behaviour to therapy SwipeCard)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun Quiz2SwipeCard(
    modifier: Modifier = Modifier,
    swipeThreshold: Float = 150f,
    enabled: Boolean = true,
    onDismiss: () -> Unit,
    autoDismiss: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val scope   = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val alpha   = remember { Animatable(1f) }

    LaunchedEffect(autoDismiss) {
        if (autoDismiss) {
            offsetX.animateTo(800f, tween(300))
            alpha.animateTo(0f, tween(300))
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
                    onDrag    = { _, d -> scope.launch { offsetX.snapTo(offsetX.value + d.x); offsetY.snapTo(offsetY.value + d.y) } },
                    onDragEnd = {
                        scope.launch {
                            if (kotlin.math.abs(offsetX.value) > swipeThreshold) {
                                launch { offsetX.animateTo(offsetX.value * 3, tween(300)) }
                                launch { alpha.animateTo(0f, tween(300)) }
                                delay(300); onDismiss()
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
                .shadow(25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                .width(259.dp).height(294.5.dp)
                .background(Color(0xE5FFFFFF), RoundedCornerShape(35.dp))
                .padding(10.dp),
            content = content
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Audio recorder for Q2 (separate class to avoid clash if Q12 is in same module)
// ─────────────────────────────────────────────────────────────────────────────
class Quiz2AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording() {
        audioFile = File(context.cacheDir, "quiz3_q2_speech.wav")
        recorder  = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
                ).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare(); start()
            }
    }

    fun stopRecording(): File? {
        recorder?.apply { try { stop() } catch (e: Exception) {}; release() }
        recorder = null
        return audioFile
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CardItem data class (only needed here if not already declared in Q1 file)
// ─────────────────────────────────────────────────────────────────────────────
data class CardItem(val id: Int, val word: String)