package org.example.frontend.quizzes.quiz1

import WaterSoundPlayer
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.delay
import org.example.frontend.R
import org.example.frontend.quizzes.quiz1.components.LiquidProgressBar
import java.io.ByteArrayOutputStream

@Composable
fun QuizQuestionL1_Shell(
    questionData: QuizQuestion,
    currentProgress: Float,
    questionNumber: Int,
    onAnswerSubmitted: (ByteArray?) -> Unit
) {
    val context = LocalContext.current
    val waterSound = remember { WaterSoundPlayer(context) }
    val darkSpaceNavy = Color(0xFF0A192F)

    var isAudioPlaying by remember { mutableStateOf(false) }
    val overlayBoolean = remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }


    DisposableEffect(Unit) {
        onDispose { waterSound.release() }
    }

    LaunchedEffect(overlayBoolean.value) {
        if (overlayBoolean.value && questionData.audioUrl != null) {
            isAudioPlaying = true
            try {
                val mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(questionData.audioUrl)
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
            delay(3000)
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

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.quiz1_q1),
            contentDescription = "Cosmic Testing Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .align(Alignment.TopCenter)
        ) {
            LiquidProgressBar(
                progress = currentProgress,
                liquidColor = Color(0xFF00E5FF),
                backgroundColor = darkSpaceNavy.copy(alpha = 0.6f)
            )
        }

        Box(
            modifier = Modifier
                .width(330.dp)
                .height(535.dp)
                .shadow(
                    elevation = 25.dp,
                    shape = RoundedCornerShape(38.dp),
                    ambientColor = Color(0x3300E5FF),
                    spotColor = Color(0x4400E5FF)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEEFFFFFF),
                            Color(0xCCFFFFFF),
                            Color(0xAAFFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xAAFFFFFF),
                            Color(0x6600E5FF),
                            Color(0xAAFFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Question $questionNumber",
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Bold,
                        color = darkSpaceNavy,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 20.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = questionData.instructionText,
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.SemiBold,
                            color = darkSpaceNavy,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.weight(1f).padding(vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = { overlayBoolean.value = true },
                        enabled = !isAudioPlaying,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.quiz1_speaker),
                            contentDescription = "Speaker Trigger"
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(255.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF7FCFF),
                                    Color(0xFFE3F2FD)
                                )
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0x3300E5FF),
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

                Box(
                    modifier = Modifier
                        .padding(bottom = 22.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color(0x8800E5FF)
                        )
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0066CC),
                                    Color(0xFF002266)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0x88FFFFFF),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable(enabled = !isSubmitting && paths.isNotEmpty()) {
                            isSubmitting = true

                            val bitmap = createBitmapFromPaths(paths, boxSizePx, boxSizePx)
                            val stream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                            val payloadBytes = stream.toByteArray()

                            onAnswerSubmitted(payloadBytes)
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
                            text = "Submit",
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
                    Image(painter = painterResource(R.drawable.quiz1_speechbubble), contentDescription = "")
                    Text(
                        text = questionData.instructionText,
                        modifier = Modifier.padding(horizontal = 28.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            color = darkSpaceNavy,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    )
                }
                AsyncImage(
                    model = ImageRequest.Builder(context).data(R.drawable.doraemon).build(),
                    imageLoader = imageLoader,
                    contentDescription = "Doraemon Helper Animation",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(327.dp).offset(y = (-120).dp).align(Alignment.BottomStart)
                )
            }
        }
    }
}