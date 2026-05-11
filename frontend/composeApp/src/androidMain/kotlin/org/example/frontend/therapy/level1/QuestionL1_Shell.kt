package org.example.frontend.therapy.level1

import WaterSoundPlayer
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
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
import java.io.ByteArrayOutputStream

@Composable
fun QuestionL1_Shell(
    sessionItem: SessionQuestion, // DIRECT INJECTION: Holds the mined mini-questions payload received from the Router
    uiSequenceNumber: Int,        // Dynamic visual sequence numbering derived from array progression (1, 2, 3...)
    onNext: () -> Unit            // Triggers the Router to advance the array pointer and mount the next screen
) {
    val context = LocalContext.current
    val waterSound = remember { WaterSoundPlayer(context) }
    val ip = NetworkConfig.SERVER_IP
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Dynamic Audio State Variables
    var isAudioPlaying by remember { mutableStateOf(false) }
    val overlayBoolean = remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { waterSound.release() }
    }

    // Audio Streaming Logic via pre-cached static URL
    LaunchedEffect(overlayBoolean.value) {
        if (overlayBoolean.value && sessionItem.audioUrl != null) {
            isAudioPlaying = true
            try {
                val mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(sessionItem.audioUrl)
                    setOnPreparedListener { mp -> mp.start() }
                    setOnCompletionListener { mp ->
                        mp.release()
                        isAudioPlaying = false
                        overlayBoolean.value = false
                    }
                    setOnErrorListener { mp, _, _ ->
                        mp.release()
                        isAudioPlaying = false
                        overlayBoolean.value = false
                        true
                    }
                }
                mediaPlayer.prepareAsync()
            } catch (e: Exception) {
                isAudioPlaying = false
                overlayBoolean.value = false
            }
        } else if (overlayBoolean.value) {
            kotlinx.coroutines.delay(3000)
            overlayBoolean.value = false
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

    // CRITICAL BACKEND ALIGNMENT: Creates a pure black stroke on a white bitmap to guarantee high-contrast thresholding for AI models
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

    // THE DYNAMIC DATA FLOW: POSTs the completed drawing to evaluate accuracy and triggers the final mastery update for the specific DB slot
    fun sendDrawingToFlask(userid: String, byteArray: ByteArray, target: String, qNum: Int, onResult: (String) -> Unit) {
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.png", byteArray.toRequestBody("image/png".toMediaTypeOrNull()))
            .addFormDataPart("user_id", userid)
            .addFormDataPart("target_word", target)
            .addFormDataPart("question_number", qNum.toString()) // THE SECURE LINK: Permanently anchors the evaluation payload to the exact absolute database slot received from the server
            .build()

        val request = Request.Builder()
            .url("http://$ip/predict_therapy_direction")
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
        // ---> THEME BACKGROUND: Pastel Easter & Felt Crafts Flat-Lay <---
        Image(
            painter = painterResource(R.drawable.level1_q1),
            contentDescription = "Thematic Pastel Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // ==========================
        // GLASSMORPHIC PASTEL THEME
        // ==========================
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
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
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
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            Color(0xAAFFFFFF),
                            Color(0x55FFB6D9),
                            Color(0x44FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                // TRANSLUCENT GLASS EFFECT
                .background(
                    color = Color(0x33FFFFFF),
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
                        color = Color(0xFFFF8FC4), // soft pastel pink
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
                    // ---> REFINED UI: Outer bounding boxes removed entirely. Renders text directly on the glass card <---
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

                    // ==========================
                    // AUDIO BUTTON
                    // ==========================
                    // ---> REFINED UI: Stripped outer background box overlays to let the pre-colored SVG asset render cleanly <---
                    IconButton(
                        onClick = { overlayBoolean.value = true },
                        enabled = !isAudioPlaying,
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
                // DRAWING AREA
                // ==========================
                Box(
                    modifier = Modifier
                        .size(255.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = Color(0x55FFB6D9)
                        )
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xAAFFFFFF),
                                    Color(0xEEFFF7FB)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0x88FFD6EA),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    waterSound.start()
                                    currentPath = Path().apply {
                                        moveTo(offset.x, offset.y)
                                    }
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(
                                        change.position.x,
                                        change.position.y
                                    )
                                    currentPath = Path().apply {
                                        currentPath?.let { addPath(it) }
                                    }
                                },
                                onDragEnd = {
                                    waterSound.stop()
                                    currentPath?.let { paths.add(it) }
                                    currentPath = null
                                },
                                onDragCancel = {
                                    waterSound.stop()
                                }
                            )
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // ---> CRITICAL VISION AI ALIGNMENT: Drawing paths set to absolute Color.Black to protect backend thresholding accuracy <---
                        paths.forEach { path ->
                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(width = 8f)
                            )
                        }

                        currentPath?.let {
                            drawPath(
                                path = it,
                                color = Color.Black,
                                style = Stroke(width = 8f)
                            )
                        }
                    }
                }

                // ==========================
                // NEXT BUTTON
                // ==========================
                Box(
                    modifier = Modifier
                        .padding(bottom = 22.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFFA7D1),
                                    Color(0xFFFF84BF)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xAAFFFFFF),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable(enabled = !isSubmitting) {
                            isSubmitting = true

                            val bitmap = createBitmapFromPaths(
                                paths,
                                boxSizePx,
                                boxSizePx
                            )

                            val bytes = bitmapToByteArray(bitmap)

                            currentUser?.uid?.let { uid ->
                                sendDrawingToFlask(
                                    userid = uid,
                                    byteArray = bytes,
                                    target = sessionItem.targetWord,
                                    qNum = sessionItem.dbQuestionNumber
                                ) { result ->
                                    isSubmitting = false
                                    onNext()
                                }
                            } ?: run {
                                isSubmitting = false
                                onNext()
                            }
                        }
                        .padding(horizontal = 45.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
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
        }

        // ---> UNTOUCHED OVERLAY: Preserved your exact original character logic with guaranteed text containment <---
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
                    Image(painter = painterResource(R.drawable.level1_speechbubble), contentDescription = "")
                    Text(
                        text = sessionItem.instructionText,
                        modifier = Modifier.padding(horizontal = 28.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            color = Color(0xFF7A3E66),
                            fontWeight = FontWeight.Bold,
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