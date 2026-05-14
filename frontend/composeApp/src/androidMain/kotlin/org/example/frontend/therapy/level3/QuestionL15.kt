package org.example.frontend.therapy.level3

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
import androidx.compose.ui.draw.clipToBounds
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
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import org.json.JSONObject
import java.io.ByteArrayOutputStream

// Data Models
data class CardItemBox(val id: Int, val word: String, val length: Int)

// Bitmap Compression Handlers
fun createBitmapFromPaths(paths: List<Path>, width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)
    val paint = android.graphics.Paint().apply {
        color       = android.graphics.Color.BLACK
        style       = android.graphics.Paint.Style.STROKE
        strokeWidth = 10f
        isAntiAlias = true
        strokeJoin  = android.graphics.Paint.Join.ROUND
        strokeCap   = android.graphics.Paint.Cap.ROUND
    }
    paths.forEach { canvas.drawPath(it.asAndroidPath(), paint) }
    return bitmap
}

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun sendBatchImagesToTherapy(
    userID: String,
    word: String,
    images: List<ByteArray>,
    onResult: (String) -> Unit
) {
    val ip      = NetworkConfig.SERVER_IP
    val client  = OkHttpClient()
    val builder = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("user_id", userID)
        .addFormDataPart("target_word", word)
        .addFormDataPart("question_number", "15")

    images.forEachIndexed { i, bytes ->
        builder.addFormDataPart(
            "images", "letter_$i.png",
            bytes.toRequestBody("image/png".toMediaTypeOrNull())
        )
    }

    val request = Request.Builder()
        .url("http://$ip/predict_handwriting_batch_therapy")
        .post(builder.build())
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            val errorMsg = "Error: ${e.message}"
            Handler(Looper.getMainLooper()).post { onResult(errorMsg) }
        }
        override fun onResponse(call: Call, response: Response) {
            // ---> CRITICAL FIX: Read the stream on the background thread first <---
            val responseData = response.body?.string() ?: "No response"

            // THEN post the safe string back to the UI thread
            Handler(Looper.getMainLooper()).post {
                onResult(responseData)
            }
        }
    })
}
@Composable
fun QuestionL15(onNextScreen: () -> Unit) {
    val CURRENT_QUESTION_NUMBER = 15
    val ip         = NetworkConfig.SERVER_IP
    val context    = LocalContext.current
    val waterSound = remember { WaterSoundPlayer(context) }

    var isLoading       by remember { mutableStateOf(true) }
    // Clean inline UI defaults optimized for auto-wrap scaling layouts
    var questionText    by remember { mutableStateOf("Write the word below in the box given") }
    var dynamicAudioUrl by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying  by remember { mutableStateOf(false) }
    var isAudioOverlay  by remember { mutableStateOf(false) }

    // ---> 1. INDEPENDENT LOCAL STATE FOR CARTOON GIF <---
    var cartoonResId by remember { mutableStateOf(R.drawable.mickey1) }

    var wordList     by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }

    // Explicit state monitor preventing navigation race condition drops
    var isFinished   by remember { mutableStateOf(false) }

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

    DisposableEffect(Unit) { onDispose { waterSound.release() } }

    // Safe Navigation Monitor
    LaunchedEffect(currentIndex, wordList.size) {
        if (!isLoading && wordList.isNotEmpty() && currentIndex >= wordList.size) {
            if (!isFinished) {
                isFinished = true
                onNextScreen()
            }
        }
    }

    // Dynamic Network Resource Handlers
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            val client  = OkHttpClient()
            val request = Request.Builder()
                .url("http://$ip/get_personalized_question?user_id=$uid&question_number=$CURRENT_QUESTION_NUMBER")
                .get().build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post {
                        wordList  = listOf("goat", "frog", "page", "plug")
                        cartoonResId = R.drawable.mickey1
                        isLoading = false
                    }
                }
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    Handler(Looper.getMainLooper()).post {
                        try {
                            val json = JSONObject(body ?: "")
                            if (json.optBoolean("mastered", false)) {
                                isLoading = false
                                if (!isFinished) {
                                    isFinished = true
                                    onNextScreen()
                                }
                                return@post
                            }

                            // ---> 3. PARSE AND MAP SELECTION LOCALLY <---
                            val helperStr = json.optString("cartoon_selection", "mickey")
                            cartoonResId = mapCartoonStringToDrawable(helperStr)

                            questionText    = json.optString("instruction_text", questionText)
                            dynamicAudioUrl = if (json.isNull("audio_url")) null
                            else json.getString("audio_url")
                            val arr = json.optJSONArray("data")
                            wordList = if (arr != null && arr.length() > 0)
                                (0 until arr.length()).map { arr.getString(it) }
                            else
                                listOf("goat", "frog", "page", "plug")
                        } catch (e: Exception) {
                            wordList = listOf("goat", "frog", "page", "plug")
                        }
                        isLoading = false
                    }
                }
            })
        } ?: run {
            wordList  = listOf("goat", "frog", "page", "plug")
            cartoonResId = R.drawable.mickey1
            isLoading = false
        }
    }

    // Audio Trigger Arrays
    LaunchedEffect(isAudioOverlay) {
        if (!isAudioOverlay) return@LaunchedEffect
        if (!dynamicAudioUrl.isNullOrEmpty()) {
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
                    setOnCompletionListener {
                        it.release(); isAudioPlaying = false; isAudioOverlay = false
                    }
                    setOnErrorListener { it, _, _ ->
                        it.release(); isAudioPlaying = false; isAudioOverlay = false; true
                    }
                }
                mp.prepareAsync()
            } catch (e: Exception) { isAudioPlaying = false; isAudioOverlay = false }
        } else {
            val mp = MediaPlayer.create(context, R.raw.doraemon_alevel3q15)
            mp.start()
            mp.setOnCompletionListener { it.release() }
            delay(4000)
            isAudioOverlay = false
        }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context).components {
            if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
        }.build()
    }

    // Container Interface Framework
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter            = painterResource(R.drawable.therapy_level3),
            contentDescription = "",
            contentScale       = ContentScale.FillBounds,
            modifier           = Modifier.fillMaxSize()
        )

        // 1. Loading and Terminating Scopes
        if (isLoading || isFinished) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color    = Color(0xFFF8335D)
            )
        }
        // 2. Active Validation View scopes
        else if (wordList.isNotEmpty() && currentIndex < wordList.size) {

            Box(
                modifier = Modifier
                    .width(310.dp).height(570.dp) // Adjusted width for spatial balance
                    .background(Color(0xC7FFFFFF), RoundedCornerShape(35.dp))
                    .align(Alignment.Center)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title Banner
                    Row(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Question no $CURRENT_QUESTION_NUMBER",
                            style = TextStyle(
                                fontSize   = 34.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                    }

                    // Prompt & Speaker Display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(90.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, // Pushes items to structural boundaries safely
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = questionText,
                            modifier = Modifier.weight(1f).padding(end = 8.dp),
                            style = TextStyle(
                                fontSize   = 20.sp, // Optimized from 23.sp to prevent box clipping
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight.SemiBold,
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )

                        // Visible Action-Button Wrapper Fix
                        IconButton(
                            onClick = { isAudioOverlay = true },
                            enabled = !isAudioPlaying,
                            modifier = Modifier.size(48.dp) // Absolute responsive action scaling constraints
                        ) {
                            Image(
                                modifier           = Modifier.fillMaxSize(),
                                painter            = painterResource(id = R.drawable.sound_button1),
                                contentDescription = "Speaker Toggle",
                                contentScale       = ContentScale.Fit
                            )
                        }
                    }

                    // Safe UI Wrapper Block
                    key(currentIndex) {
                        val safeIndex   = currentIndex.coerceIn(0, wordList.size - 1)
                        val word        = wordList[safeIndex]
                        var isChecking  by remember { mutableStateOf(false) }

                        val letterPaths = remember {
                            word.indices.map {
                                mutableStateListOf<Path>() as MutableList<Path>
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {

                            Text(
                                text  = word,
                                style = TextStyle(
                                    fontSize   = 46.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color      = Color(0xFFF8335D),
                                    textAlign  = TextAlign.Center
                                )
                            )

                            val rowSize = 5
                            val numRows = (word.length + rowSize - 1) / rowSize
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (row in 0 until numRows) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        val start = row * rowSize
                                        val end   = minOf(start + rowSize, word.length)
                                        for (idx in start until end) {
                                            key(idx) {
                                                LetterBox15(
                                                    char       = word[idx],
                                                    pathList   = letterPaths[idx],
                                                    waterSound = waterSound
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Trigger Submissions Natively
                            // Trigger Submissions Natively
                            val scope = rememberCoroutineScope()
                            Box(
                                modifier = Modifier
                                    .width(140.dp).height(46.dp)
                                    .background(Color(0xFFF8335D), RoundedCornerShape(30.dp))
                                    .clickable(enabled = !isChecking) {
                                        isChecking = true
                                        val boxPx = 150
                                        val imageList = letterPaths.map { paths ->
                                            bitmapToByteArray(
                                                createBitmapFromPaths(
                                                    paths.toList(), boxPx, boxPx
                                                )
                                            )
                                        }
                                        currentUser?.uid?.let { uid ->
                                            sendBatchImagesToTherapy(uid, word, imageList) { _ ->
                                                scope.launch {
                                                    // ---> CRITICAL FIX: Loop through the remaining words <---
                                                    if (currentIndex < wordList.size - 1) {
                                                        currentIndex++
                                                        isChecking = false // Unlock the button for the next word
                                                    } else {
                                                        // Only navigate away when all words are done
                                                        if (!isFinished) {
                                                            isFinished = true
                                                            onNextScreen()
                                                        }
                                                    }
                                                }
                                            }
                                        } ?: run {
                                            scope.launch {
                                                if (!isFinished) {
                                                    isFinished = true
                                                    onNextScreen()
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isChecking) "Checking..." else "Submit",
                                    style = TextStyle(
                                        fontSize = 22.sp,
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

            // Overlay Companion Guidance Frame Layout
            if (isAudioOverlay) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x4FFFFFFF))) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)
                    ) {
                        Image(painterResource(R.drawable.speech_bubble3), contentDescription = "Speech Bubble")

                        // Optimized bounding padding prevents external overflow
                        Text(
                            text  = questionText,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            style = TextStyle(
                                fontSize   = 16.sp, // Downscaled down safely from 23.sp to anchor cleanly inside bubble borders
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight.Bold,
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                    }

                    // ---> 4. USE LOCAL STATE IN ASYNCIMAGE <---
                    AsyncImage(
                        model              = ImageRequest.Builder(context)
                            .data(cartoonResId) // Passes active local dynamic state identifier cleanly
                            .build(),
                        imageLoader        = imageLoader,
                        contentDescription = "Dynamic Guidance Character Helper Animation",
                        contentScale       = ContentScale.FillBounds,
                        modifier           = Modifier
                            .size(327.dp).offset(y = (-120).dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }
        // 3. Fallback Completion State Framework
        else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFF8335D))
            }
        }
    }
}

@Composable
fun LetterBox15(
    char: Char,
    pathList: MutableList<Path>,
    waterSound: WaterSoundPlayer
) {
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Box(
        modifier = Modifier
            .size(45.dp)
            .background(Color.White)
            .clipToBounds()
            .border(3.dp, Color(0xFFF8335D), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        waterSound.start()
                        currentPath = Path().apply { moveTo(offset.x, offset.y) }
                    },
                    onDrag = { change, _ ->
                        currentPath = Path().apply {
                            currentPath?.let { addPath(it) }
                            lineTo(change.position.x, change.position.y)
                        }
                    },
                    onDragEnd = {
                        waterSound.stop()
                        currentPath?.let { pathList.add(it) }
                        currentPath = null
                    },
                    onDragCancel = {
                        waterSound.stop()
                        currentPath = null
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = char.toString(),
            style = TextStyle(
                fontSize   = 22.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                color      = Color(0x22F8335D)
            )
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            pathList.forEach { p -> drawPath(p, Color.Black, style = Stroke(8f)) }
            currentPath?.let  { drawPath(it, Color.Black, style = Stroke(8f)) }
        }
    }
}