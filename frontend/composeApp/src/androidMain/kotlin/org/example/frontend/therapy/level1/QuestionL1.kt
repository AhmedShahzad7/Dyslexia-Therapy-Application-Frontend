package org.example.frontend.therapy.level1

import WaterSoundPlayer
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import org.json.JSONObject
import java.io.ByteArrayOutputStream

@Composable
fun QuestionL1(onNextScreen: () -> Unit) {
    // ==========================================================
    // ⬇️ CHANGE THIS NUMBER FOR OTHER QUESTION SCREENS ⬇️
    // ==========================================================
    val CURRENT_QUESTION_NUMBER = 1
    // ==========================================================

    val context = LocalContext.current
    val waterSound = remember { WaterSoundPlayer(context) }
    val ip = NetworkConfig.SERVER_IP
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Dynamic State Variables
    var isLoading by remember { mutableStateOf(true) }
    var questionText by remember { mutableStateOf("") }
    var targetWord by remember { mutableStateOf("") }

    // We initialize the UI number with our hardcoded variable, but it can update from backend if needed
    var questionNumber by remember { mutableStateOf(CURRENT_QUESTION_NUMBER) }

    // Dynamic Audio State Variables
    var dynamicAudioUrl by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    val overlay_boolean = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            waterSound.release()
        }
    }

    // 1. Fetch the question from Flask when the screen loads
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            val client = OkHttpClient()
            val request = Request.Builder()
                // Safely passing the UID and dynamic question number
                .url("http://$ip/get_personalized_question?user_id=$uid&question_number=$CURRENT_QUESTION_NUMBER")
                .get()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        questionText = "Error loading question."
                        isLoading = false
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        try {
                            val json = JSONObject(responseData)
                            Handler(Looper.getMainLooper()).post {
                                targetWord = json.optString("target_word", "Up")
                                questionText = json.optString("instruction_text", "Draw the arrow")
                                questionNumber = json.optInt("question_number", CURRENT_QUESTION_NUMBER)
                                dynamicAudioUrl = if (json.isNull("audio_url")) null else json.getString("audio_url")
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            Log.e("FlaskAPI", "JSON Parsing error", e)
                        }
                    }
                }
            })
        } ?: run {
            // If user is null, stop loading to prevent infinite spinner
            isLoading = false
            questionText = "User not logged in."
        }
    }

    // Audio Streaming Logic via Typecast URL
    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value && !dynamicAudioUrl.isNullOrEmpty()) {
            isAudioPlaying = true
            try {
                val mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(dynamicAudioUrl)

                    setOnPreparedListener { mp -> mp.start() }
                    setOnCompletionListener { mp ->
                        mp.release()
                        isAudioPlaying = false
                        overlay_boolean.value = false
                    }
                    setOnErrorListener { mp, _, _ ->
                        mp.release()
                        isAudioPlaying = false
                        overlay_boolean.value = false
                        true
                    }
                }
                mediaPlayer.prepareAsync()
            } catch (e: Exception) {
                isAudioPlaying = false
                overlay_boolean.value = false
            }
        } else if (overlay_boolean.value) {
            kotlinx.coroutines.delay(3000)
            overlay_boolean.value = false
        }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) { add(ImageDecoderDecoder.Factory()) }
                else { add(GifDecoder.Factory()) }
            }.build()
    }

    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    val density = LocalDensity.current
    val targetPixels = 250
    val boxSizeDp = 250.dp
    val boxSizePx = with(density) { targetPixels.dp.toPx().toInt() }

    fun createBitmapFromPaths(paths: List<Path>, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
            strokeJoin = android.graphics.Paint.Join.ROUND
            strokeCap = android.graphics.Paint.Cap.ROUND
        }
        paths.forEach { composePath -> canvas.drawPath(composePath.asAndroidPath(), paint) }
        return bitmap
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    // 2. Updated function with qNum parameter
    fun sendImageToFlask(userid: String, byteArray: ByteArray, target: String, qNum: Int, onResult: (String) -> Unit) {
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.png", byteArray.toRequestBody("image/png".toMediaTypeOrNull()))
            .addFormDataPart("user_id", userid)
            .addFormDataPart("target_word", target)
            .addFormDataPart("question_number", qNum.toString()) // Dynamic POST variable
            .build()

        val request = Request.Builder()
            .url("http://$ip/predict_direction") // Corrected to POST to predict_direction
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post { onResult("Error: ${e.message}") }
            }
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string() ?: "No response"
                Handler(Looper.getMainLooper()).post { onResult(result) }
            }
        })
    }

    // UI RENDERING
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.assessmenttestquestion1),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF27B51A)
            )
        } else {
            Box(
                modifier = Modifier
                    .width(299.dp)
                    .height(497.dp)
                    .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                    .align(Alignment.Center)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(62.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question no $questionNumber",
                            style = TextStyle(
                                fontSize = 34.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                color = Color(0xF527B51A),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp).padding(start = 20.dp)
                    ) {
                        Text(
                            text = questionText,
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                color = Color(0xFF27B51A),
                                textAlign = TextAlign.Center,
                            )
                        )
                        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                            IconButton(onClick = { overlay_boolean.value = true }, enabled = !isAudioPlaying) {
                                Image(
                                    modifier = Modifier.size(35.dp),
                                    painter = painterResource(id = R.drawable.sound_button),
                                    contentDescription = "Speaker",
                                    contentScale = ContentScale.None
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(boxSizeDp)
                            .background(color = Color.White)
                            .clipToBounds()
                            .border(width = 4.dp, color = Color(0xFF27B51A))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        waterSound.start()
                                        currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                    },
                                    onDrag = { change, _ ->
                                        currentPath?.lineTo(change.position.x, change.position.y)
                                        currentPath = Path().apply { currentPath?.let { addPath(it) } }
                                    },
                                    onDragEnd = {
                                        waterSound.stop()
                                        currentPath?.let { paths.add(it) }
                                        currentPath = null
                                    },
                                    onDragCancel = { waterSound.stop() }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            paths.forEach { path -> drawPath(path = path, color = Color.Black, style = Stroke(8f)) }
                            currentPath?.let { drawPath(path = it, color = Color.Black, style = Stroke(8f)) }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 20.dp)
                        .background(Color(0xFF27B51A), RoundedCornerShape(15.dp))
                        .clickable {
                            val bitmap = createBitmapFromPaths(paths, boxSizePx, boxSizePx)
                            val bytes = bitmapToByteArray(bitmap)

                            // 3. Safe call to unwrapped user ID
                            currentUser?.uid?.let { uid ->
                                sendImageToFlask(uid, bytes, targetWord, questionNumber) { result ->
                                    onNextScreen()
                                }
                            }
                        }
                        .padding(horizontal = 40.dp, vertical = 15.dp)
                ) {
                    Text(
                        text = "Next",
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            if (overlay_boolean.value) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color(0x4FFFFFFF))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)
                    ) {
                        Image(painter = painterResource(R.drawable.speech_bubble), contentDescription = "")
                        Text(
                            text = questionText,
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                color = Color(0xFF27B51A),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(R.drawable.doraemon).build(),
                        imageLoader = imageLoader,
                        contentDescription = "Doraemon GIF",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(327.dp).offset(y = (-120).dp).align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}