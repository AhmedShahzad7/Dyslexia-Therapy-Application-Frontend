package org.example.frontend.AssesmentTest.Level4

import android.graphics.Bitmap
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
import okhttp3.RequestBody.Companion.toRequestBody
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import java.io.ByteArrayOutputStream

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

fun sendSentenceToFlask(userID: String, sentence: String, images: List<ByteArray>, onResult: (String) -> Unit) {
    val client = OkHttpClient()
    val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
    val ip= NetworkConfig.SERVER_IP
    multipartBuilder.addFormDataPart("user_id", userID)
    multipartBuilder.addFormDataPart("target_sentence", sentence)
    multipartBuilder.addFormDataPart("question_number", "17")

    images.forEachIndexed { index, bytes ->
        multipartBuilder.addFormDataPart(
            "images", "char_$index.png",
            bytes.toRequestBody("image/png".toMediaTypeOrNull())
        )
    }

    val request = Request.Builder()
        .url("http://"+ip+"/predict_handwriting_sentence")
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
fun Question17(onNextScreen: () -> Unit) {
    val context = LocalContext.current
    val overlay_boolean = remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

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

    val sentences = listOf(
        "was it on or no",
        "The big dog",
        "On mat sat a cat"
    )
    val currentindex = remember { mutableIntStateOf(0) }
    val currentSentenceString = sentences[currentindex.intValue]

    val lettersOnly = currentSentenceString.filter { it.isLetterOrDigit() }
    val drawingState = remember { mutableStateMapOf<Int, List<Path>>() }

    LaunchedEffect(currentindex.intValue) {
        drawingState.clear()
    }

    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.rewrite_sentence)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener { it.release() }
            delay(5000)
            overlay_boolean.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.assessment_level1q2),
            contentDescription = "Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .width(370.dp)
                .height(570.dp)
                .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(35.dp))
                .align(Alignment.Center)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp).height(62.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question no 17",
                        Modifier.width(245.dp).height(62.dp),
                        style = TextStyle(
                            fontSize = 34.sp,
                            color = Color(0xF527B51A),
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            textAlign = TextAlign.Center
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().height(100.dp).background(color = Color(0x00FFFFFF)).padding(start = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Rewrite the sentence shown \n below",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF27B51A),
                                textAlign = TextAlign.Center
                            )
                        )
                        IconButton(onClick = { overlay_boolean.value = true }) {
                            Image(
                                modifier = Modifier.size(35.dp),
                                painter = painterResource(id = R.drawable.sound_button),
                                contentDescription = "sound",
                                contentScale = ContentScale.None
                            )
                        }
                    }
                }

                Box(
                    Modifier
                        .shadow(25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                        .width(350.dp).height(350.dp)
                        .background(Color(0xE5FFFFFF), RoundedCornerShape(35.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentSentenceString,
                            style = TextStyle(
                                fontSize = 30.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xF527B51A),
                                textAlign = TextAlign.Center,
                            )
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top)
                        ) {
                            lettersOnly.forEachIndexed { index, _ ->
                                key("${currentindex.intValue}_$index") {
                                    DrawingBox(
                                        modifier = Modifier.size(50.dp),
                                        onPathsChanged = { newPaths -> drawingState[index] = newPaths }
                                    )
                                }
                            }
                        }

                        Box(
                            Modifier
                                .padding(bottom = 16.dp)
                                .width(150.dp).height(50.dp)
                                .background(color = Color(0xF527B51A), shape = RoundedCornerShape(35.dp))
                                .clickable(enabled = !isLoading) {
                                    val currentUser = FirebaseAuth.getInstance().currentUser
                                    if (currentUser != null) {
                                        isLoading = true
                                        val boxSizePx = 200
                                        val imageList = lettersOnly.indices.map { idx ->
                                            val bitmap = createBitmapFromPaths(drawingState[idx] ?: emptyList(), boxSizePx)
                                            val stream = ByteArrayOutputStream()
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                                            stream.toByteArray()
                                        }

                                        sendSentenceToFlask(currentUser.uid, currentSentenceString, imageList) { result ->
                                            isLoading = false
                                            if (currentindex.intValue < sentences.lastIndex) {
                                                currentindex.intValue++
                                            } else {
                                                onNextScreen()
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLoading) "Checking..." else "Submit",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.windsol))
                            )
                        }
                    }
                }
            }
        }

        if (overlay_boolean.value) {
            Box(modifier = Modifier.fillMaxSize().background(color = Color(0x4FFFFFFF))) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)) {
                    Image(painter = painterResource(R.drawable.speech_bubble), contentDescription = "")
                    Text(
                        text = "Rewrite the sentence shown \n below",
                        style = TextStyle(
                            fontSize = 25.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            color = Color(0xFF27B51A),
                            textAlign = TextAlign.Center,
                        )
                    )
                }

                AsyncImage(
                    model = ImageRequest.Builder(context).data(R.drawable.doraemon2).build(),
                    imageLoader = imageLoader,
                    contentDescription = "Doraemon",
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
            .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
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
            paths.forEach { drawPath(path = it, color = Color.Black, style = Stroke(8f)) }
            currentPath?.let { drawPath(path = it, color = Color.Black, style = Stroke(8f)) }
        }
    }
}