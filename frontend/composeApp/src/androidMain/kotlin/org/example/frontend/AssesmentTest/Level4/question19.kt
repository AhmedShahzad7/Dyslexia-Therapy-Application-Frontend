package org.example.frontend.AssesmentTest.Level4

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.key
// --- AUDIO RECORDER HELPER ---

// --- AUDIO RECORDER HELPER ---
class AudioRecorderHelperQ19(private val context: Context) {
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
            // UPDATED: Changed to THREE_GPP and AMR_NB for better AI transcription compatibility
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }
    }

    fun stopRecording(): File? {
        try {
            recorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder?.release()
            recorder = null
        }
        return audioFile
    }
}

// --- NETWORK HELPER FOR AUDIO (PHONETIC FUZZY MATCHING) ---
fun uploadAudioForTranscription(
    audioFile: File,
    serverIp: String,
    targetWord: String,
    userId: String,
    onResult: (String?) -> Unit
) {
    try {
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("target_word", targetWord)
            .addFormDataPart(
                "audio",
                audioFile.name,
                audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
            )
            .addFormDataPart("user_id", userId)
            .addFormDataPart("question_number", "19")
            .build()

        val baseUrl = if (serverIp.startsWith("http")) serverIp else "http://$serverIp"

        val request = Request.Builder()
            .url("$baseUrl/transcribe_and_score1")
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
    } catch (e: Exception) {
        e.printStackTrace()
        Handler(Looper.getMainLooper()).post { onResult("App Error: ${e.message}") }
    }
}

// --- HANDWRITING HELPER FUNCTIONS ---
fun createBitmapFromPathsQ19(paths: List<Path>, size: Int): Bitmap {
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

fun sendSentenceToFlaskQ19(userID: String, sentence: String, images: List<ByteArray>, onResult: (String) -> Unit) {
    val client = OkHttpClient()
    val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
    val ip = NetworkConfig.SERVER_IP
    multipartBuilder.addFormDataPart("user_id", userID)
    multipartBuilder.addFormDataPart("target_sentence", sentence)
    multipartBuilder.addFormDataPart("question_number", "19")

    images.forEachIndexed { index, bytes ->
        multipartBuilder.addFormDataPart(
            "images", "char_$index.png",
            bytes.toRequestBody("image/png".toMediaTypeOrNull())
        )
    }

    val request = Request.Builder()
        .url("http://" + ip + "/predict_handwriting_sentence")
        .post(multipartBuilder.build())
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: java.io.IOException) {
            Handler(Looper.getMainLooper()).post { onResult("Error: ${e.message}") }
        }
        override fun onResponse(call: Call, response: Response) {
            val result = response.body?.string() ?: "No response"
            Handler(Looper.getMainLooper()).post { onResult(result) }
        }
    })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Question19(onNextScreen: () -> Unit) {
    val context = LocalContext.current
    val overlay_boolean = remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // --- AUDIO STATES ---
    val audioRecorder = remember { AudioRecorderHelperQ19(context) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isRecording by remember { mutableStateOf(false) }

    // Permission Launcher for Microphone
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

    fun Clicked_Speaker() {
        overlay_boolean.value = true
        speaker_boolean.value = true
    }

    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.read_write)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(5000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }

    val questionsentences = listOf(
        "He sat on a mat",
        "The car is far",
        "The bug dug in"
    )
    val currentindexquestion = remember { mutableStateOf(0) }

    val currentQuestionSentenceString = questionsentences[currentindexquestion.value]
    val lettersOnlyQuestion = currentQuestionSentenceString.filter { it.isLetter() }
    val isplayingquestion = remember { mutableStateOf(false) }

    // Backend drawing state
    val drawingState = remember { mutableStateMapOf<Int, List<Path>>() }
    LaunchedEffect(currentindexquestion.value) {
        drawingState.clear()
        recordedFile = null // Clear audio file reference on new question
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    )
    {
        Image(
            painter = painterResource(id = R.drawable.question4bkg),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .width(370.dp)
                .height(570.dp)
                .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                .align(Alignment.Center)

        )
        {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth().padding(top = 32.dp)
                        .height(62.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically

                )
                {
                    Text(
                        text = "Question no 19",
                        Modifier
                            .width(245.dp)
                            .height(62.dp),
                        style = TextStyle(
                            fontSize = 34.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xF527B51A),
                            textAlign = TextAlign.Center,
                        )
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(color = Color(0x00FFFFFF))
                        .padding(start = 20.dp)

                )
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {
                        Text(
                            text = "Read and write the sentence \n shown below",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF27B51A),
                                textAlign = TextAlign.Center,
                            )
                        )

                        Box(
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Row {
                                IconButton(onClick = { Clicked_Speaker() }) {
                                    Image(
                                        modifier = Modifier.size(35.dp),
                                        painter = painterResource(id = R.drawable.sound_button),
                                        contentDescription = "speaker"
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    Modifier
                        .shadow(
                            elevation = 25.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000)
                        )
                        .width(350.dp)
                        .height(550.dp)
                        .background(color = Color(0xE5FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                        .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                )
                {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        )
                        {
                            Text(
                                text = questionsentences[currentindexquestion.value],
                                style = TextStyle(
                                    fontSize = 28.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xF527B51A),
                                    textAlign = TextAlign.Center,
                                ),
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            // Recording visual indicator
                            if (isRecording) {
                                Text("🔴 Recording...", color = Color.Red, fontSize = 12.sp)
                            }
                            // --- MICROPHONE BUTTON (Custom Image Style) ---
                            Image(
                                painter = painterResource(
                                    id = if (isRecording) R.drawable.pause else R.drawable.play
                                ),
                                contentDescription = "microphone",
                                contentScale = ContentScale.Inside,
                                modifier = Modifier
                                    .size(45.dp)
                                    .background(color = Color(0xF527B51A), shape = RoundedCornerShape(50))
                                    .clickable {
                                        if (!isRecording) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        } else {
                                            recordedFile = audioRecorder.stopRecording()
                                            isRecording = false
                                        }
                                    }
                                    .padding(8.dp),
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
                            )
                        }


                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                            maxItemsInEachRow = 4
                        ) {
                            lettersOnlyQuestion.forEachIndexed { index, char ->
                                key("${currentindexquestion.value}_$index") {
                                    DrawingBoxquestion19(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .size(50.dp),
                                        onPathsChanged = { newPaths ->
                                            drawingState[index] = newPaths
                                        }
                                    )
                                }
                            }
                        }

                        Box(
                            Modifier
                                .padding(bottom = 16.dp)
                                .width(150.dp)
                                .height(50.dp)
                                .background(color = Color(0xF527B51A), shape = RoundedCornerShape(size = 35.dp))
                                .clickable(enabled = !isLoading) {
                                    if (!isplayingquestion.value) {
                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                        if (currentUser != null) {
                                            isLoading = true
                                            val boxSizePx = 200
                                            val imageList = lettersOnlyQuestion.indices.map { idx ->
                                                val bitmap = createBitmapFromPathsQ19(
                                                    drawingState[idx] ?: emptyList(),
                                                    boxSizePx
                                                )
                                                val stream = ByteArrayOutputStream()
                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                                stream.toByteArray()
                                            }

                                            // 1. Send Handwriting Prediction
                                            sendSentenceToFlaskQ19(
                                                currentUser.uid,
                                                currentQuestionSentenceString,
                                                imageList
                                            ) { hResult ->
                                                Log.d("FlaskAPI", "Handwriting Result: $hResult")
                                            }

                                            // 2. Send Audio Transcription independently
                                            recordedFile?.let { audio ->
                                                uploadAudioForTranscription(
                                                    audio,
                                                    NetworkConfig.SERVER_IP,
                                                    currentQuestionSentenceString,
                                                    currentUser.uid
                                                ) { aResult ->
                                                    Log.d("FlaskAPI", "Audio Result: $aResult")
                                                    isLoading = false
                                                    if (currentindexquestion.value < questionsentences.lastIndex) {
                                                        currentindexquestion.value++
                                                    } else {
                                                        onNextScreen()
                                                    }
                                                }
                                            } ?: run {
                                                isLoading = false
                                                if (currentindexquestion.value < questionsentences.lastIndex) {
                                                    currentindexquestion.value++
                                                } else {
                                                    onNextScreen()
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        )
                        {
                            Text(
                                text = if (isLoading) "Checking..." else "Submit",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFFFFFFF),
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- OVERLAY ---
        if (overlay_boolean.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0x4FFFFFFF))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = -120.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.speech_bubble),
                        contentDescription = "",
                    )
                    Text(
                        text = "Read and write the sentence \n shown below",
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
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.doraemon2)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Doraemon GIF",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .width(327.dp)
                        .height(327.dp)
                        .offset(y = -120.dp)
                        .align(Alignment.BottomStart)
                )
            }
        }
    }
}

@Composable
fun DrawingBoxquestion19(
    modifier: Modifier = Modifier,
    onPathsChanged: (List<Path>) -> Unit = {}
) {
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    val boxSizeDp = 64.dp

    Column(
        modifier = modifier
            .width(boxSizeDp)
            .wrapContentHeight()
            .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(boxSizeDp)
                .background(color = Color.White)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newPath = Path().apply { moveTo(offset.x, offset.y) }
                            currentPath = newPath
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                            currentPath = Path().apply {
                                currentPath?.let { addPath(it) }
                            }
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
                paths.forEach { path ->
                    drawPath(path = path, color = Color.Black, style = Stroke(8f))
                }
                currentPath?.let {
                    drawPath(path = it, color = Color.Black, style = Stroke(8f))
                }
            }
        }
    }
}