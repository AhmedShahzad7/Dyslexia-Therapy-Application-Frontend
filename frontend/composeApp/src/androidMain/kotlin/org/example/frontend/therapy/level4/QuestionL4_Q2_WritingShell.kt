package org.example.frontend.therapy.level4

import android.graphics.Bitmap
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
import okhttp3.RequestBody.Companion.toRequestBody
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import java.io.ByteArrayOutputStream
import java.io.IOException

// --- HELPER FUNCTIONS ---
fun createBitmapFromPaths(paths: List<Path>, size: Int): Bitmap {
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
fun QuestionL4_Q2_WritingShell(
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

    val activePairsList = sessionItem.miniQuestions
    val currentPair = if (currentIndex < activePairsList.size) {
        activePairsList[currentIndex]
    } else {
        MiniQuestionTarget("Loading...", "Loading sentence...")
    }

    val lettersOnly = currentPair.sentence.filter { it.isLetterOrDigit() }
    val drawingState = remember { mutableStateMapOf<Int, List<Path>>() }

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

    LaunchedEffect(currentIndex) {
        drawingState.clear()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ---> THEMATIC BACKGROUND: Mapped to the specified level4_q2 composition <---
        Image(
            painter = painterResource(id = R.drawable.level4_q2),
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
                // MAIN WRITING CARD AREA
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
                        Text(
                            text = currentPair.sentence,
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF8A4E),
                                textAlign = TextAlign.Center,
                            )
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
                        ) {
                            lettersOnly.forEachIndexed { index, _ ->
                                key("${currentIndex}_$index") {
                                    DrawingBox(
                                        modifier = Modifier.size(50.dp),
                                        onPathsChanged = { newPaths -> drawingState[index] = newPaths }
                                    )
                                }
                            }
                        }

                        // Submit Button Trigger
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

                                        // Guarantee a valid PNG byte payload for every single expected character box
                                        val imageList = lettersOnly.indices.map { idx ->
                                            val paths = drawingState[idx]
                                            val bitmap = if (paths != null && paths.isNotEmpty()) {
                                                createBitmapFromPaths(paths, boxSizePx)
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

                                        fun uploadDynamicWritingPayload(
                                            images: List<ByteArray>,
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
                                                val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

                                                multipartBuilder.addFormDataPart("user_id", uId)
                                                multipartBuilder.addFormDataPart("target_sentence", targetSentence)
                                                multipartBuilder.addFormDataPart("target_word", targetWord)
                                                multipartBuilder.addFormDataPart("target_letter", targetLetter)
                                                multipartBuilder.addFormDataPart("question_number", dbQuestionNumber)
                                                multipartBuilder.addFormDataPart("is_final_mini", isFinalMini.toString())

                                                images.forEachIndexed { index, bytes ->
                                                    multipartBuilder.addFormDataPart(
                                                        "images", "char_$index.png",
                                                        bytes.toRequestBody("image/png".toMediaTypeOrNull())
                                                    )
                                                }

                                                val baseUrl = if (serverIp.startsWith("http")) serverIp else "http://$serverIp"
                                                val request = Request.Builder()
                                                    .url("$baseUrl/verify_l4_q2_writing")
                                                    .post(multipartBuilder.build())
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

                                        uploadDynamicWritingPayload(
                                            images = imageList,
                                            serverIp = ip,
                                            targetSentence = currentPair.sentence,
                                            targetWord = currentPair.word,
                                            targetLetter = currentPair.word.firstOrNull()?.toString() ?: "b",
                                            dbQuestionNumber = sessionItem.dbQuestionNumber.toString(),
                                            uId = userId,
                                            isFinalMini = isFinalMiniQuestion
                                        ) { result ->
                                            isLoading = false
                                            Log.d("TherapyWriting", "Scored Payload: $result")

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
                            textAlign = TextAlign.Center,
                        )
                    )
                }

                // ---> SWAPPED STATIC ASSET REFERENCE FOR RESOLVED INTEGER TARGET <---
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(cartoonResId) // Passes injected integer state payload directly
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Dynamic Companion Guidance Overlay",
                    modifier = Modifier.size(327.dp).offset(y = (-120).dp).align(Alignment.BottomStart)
                )
            }
        }
    }
}

@Composable
fun DrawingBox(modifier: Modifier = Modifier, onPathsChanged: (List<Path>) -> Unit) {
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