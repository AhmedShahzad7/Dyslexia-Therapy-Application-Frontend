package org.example.frontend.AssesmentTest.Level2


import WaterSoundPlayer
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import org.example.frontend.R
import coil.ImageLoader
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lettersViewModelSmall
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.example.frontend.AssesmentTest.Level4.DrawingBox
import java.io.ByteArrayOutputStream


@Composable
fun Question8(onNextScreen:()->Unit,
    viewModelSmall: lettersViewModelSmall=androidx.lifecycle.viewmodel.compose.viewModel (),
){
    val context = LocalContext.current
    val waterSound = remember { WaterSoundPlayer(context) }

    DisposableEffect(Unit) {
        onDispose {
            waterSound.release()
        }
    }


    val overlay_boolean= remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }

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

    fun Clicked_Speaker(){
        overlay_boolean.value = true
        speaker_boolean.value = true
    }
    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.trace_finger)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(5000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }
    val paths1 = remember { mutableStateListOf<Path>() }
    var currentPath1 by remember { mutableStateOf<Path?>(null) }

// States for Canvas 2
    val paths2 = remember { mutableStateListOf<Path>() }
    var currentPath2 by remember { mutableStateOf<Path?>(null) }

// States for Canvas 3
    val paths3 = remember { mutableStateListOf<Path>() }
    var currentPath3 by remember { mutableStateOf<Path?>(null) }

// States for Canvas 4
    val paths4 = remember { mutableStateListOf<Path>() }
    var currentPath4 by remember { mutableStateOf<Path?>(null) }
    val density = LocalDensity.current
    val targetPixels=64
    val boxSizeDp =64.dp
    val boxSizePx = with(density) { targetPixels.dp.toPx().toInt() }


    fun createBitmapFromPaths(paths: List<Path>, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 10f // Thicker lines show up better after resizing
            isAntiAlias = true
            strokeJoin = android.graphics.Paint.Join.ROUND
            strokeCap = android.graphics.Paint.Cap.ROUND
        }

        paths.forEach { composePath ->
            canvas.drawPath(composePath.asAndroidPath(), paint)
        }

        return bitmap
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
    val question_number="8"
//    fun sendImageToFlask(userid:String, byteArray: ByteArray, expectedLetter:String, boxIndex: String, onResult: (String) -> Unit) {
//        val client = OkHttpClient()
//        val requestBody = MultipartBody.Builder()
//            .setType(MultipartBody.FORM)
//            .addFormDataPart(
//                "file",
//                "image.png",
//                byteArray.toRequestBody("image/png".toMediaTypeOrNull())
//            )
//
//            .addFormDataPart("user_id", userid)
//            .addFormDataPart("question_number", question_number)
//            .addFormDataPart("expected_Letter", expectedLetter)
//            .addFormDataPart("Box_Index",boxIndex)
//            .build()
//
//        val request = Request.Builder()
//            .url("http://192.168.0.14:5000/predict_q8")
//            .post(requestBody)
//            .build()
//
//        // FOR NEXT PAGE FIX CHECK FUNCTION
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("FlaskAPI", "Error! ${e.message}", e)
//                Handler(Looper.getMainLooper()).post {
//                    onResult("Error: ${e.message}")
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val result = response.body?.string() ?: "No response"
//                Log.d("FlaskAPI", "Response: $result")
//                // FIX 2: Run success callback on Main Thread
//                Handler(Looper.getMainLooper()).post {
//                    onResult(result)
//                }
//            }
//        })
//    }
suspend fun sendImageToFlaskSuspend(
    userId: String,
    byteArray: ByteArray,
    expectedLetter: String,
    boxIndex: String
): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", "image.png", byteArray.toRequestBody("image/png".toMediaTypeOrNull()))
        .addFormDataPart("user_id", userId)
        .addFormDataPart("question_number", "8")
        .addFormDataPart("expected_Letter", expectedLetter)
        .addFormDataPart("Box_Index", boxIndex)
        .build()

    val request = Request.Builder()
        .url("http://192.168.0.14:5000/predict_q8")
        .post(requestBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        return@withContext response.body?.string() ?: "No response"
    }
}


    Box(
        modifier=Modifier.fillMaxSize(),
    )
    {
        Image(
            painter = painterResource(id = R.drawable.assessment_level1q3),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )


        Box(
            modifier=Modifier
                .width(299.dp)
                .height(497.dp)
                .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                .align(Alignment.Center)

        )
        {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier=Modifier
                        .fillMaxWidth().padding(top = 32.dp)
                        .height(62.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically

                )
                {
                    Text(
                        text = "Question no 8",
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
                    modifier=Modifier
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
                            text = "Trace your fingers\n on the Letters",
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
                            IconButton(
                                onClick = {
                                    Clicked_Speaker()
                                }
                            ) {
                                Image(
                                    modifier = Modifier
                                        .width(35.dp)
                                        .height(35.dp),
                                    painter = painterResource(id = R.drawable.sound_button),
                                    contentDescription = "selected checkmark",
                                    contentScale = ContentScale.None
                                )
                            }
                        }
                    }
                }

                Box(
                    Modifier
                        .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                        .width(250.dp)
                        .height(450.dp)
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
                        Column(
                            modifier = Modifier
                                .wrapContentSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp), // space between rows
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                        {


                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(boxSizeDp) // Use the variable
                                        .background(color = Color.White)
                                        .clipToBounds()
                                        .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    waterSound.start()
                                                    val newPath = Path().apply { moveTo(offset.x, offset.y) }
                                                    currentPath1 = newPath
                                                },
                                                onDrag = { change, _ ->
                                                    currentPath1?.lineTo(change.position.x, change.position.y)
                                                    // Trigger recomposition (hacky but works for Path updates)
                                                    currentPath1 = Path().apply {
                                                        currentPath1?.let { addPath(it) }
                                                    }
                                                },
                                                onDragEnd = {
                                                    waterSound.stop()
                                                    currentPath1?.let { paths1.add(it) }
                                                    currentPath1 = null
                                                },
                                                onDragCancel = {
                                                    waterSound.stop()
                                                }
                                            )
                                        }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        with(viewModelSmall) {
                                            drawDottedLettern()
                                        }
//

                                        paths1.forEach { path ->
                                            drawPath(path = path, color = Color.Black, style = Stroke(8f))
                                        }
                                        currentPath1?.let {
                                            drawPath(path = it, color = Color.Black, style = Stroke(8f))
                                        }
                                    }

                                }
                                Box(
                                    modifier = Modifier
                                        .size(boxSizeDp) // Use the variable
                                        .background(color = Color.White)
                                        .clipToBounds()
                                        .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                waterSound.start()
                                                    val newPath = Path().apply { moveTo(offset.x, offset.y) }
                                                    currentPath2 = newPath
                                                },
                                                onDrag = { change, _ ->
                                                    currentPath2?.lineTo(change.position.x, change.position.y)
                                                    // Trigger recomposition (hacky but works for Path updates)
                                                    currentPath2 = Path().apply {
                                                        currentPath2?.let { addPath(it) }
                                                    }
                                                },
                                                onDragEnd = {
                                                waterSound.stop()
                                                    currentPath2?.let { paths2.add(it) }
                                                    currentPath2 = null
                                                },
                                                onDragCancel = {
                                                waterSound.stop()
                                                }
                                            )
                                        }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        with(viewModelSmall) {
                                            drawDottedLetteru()
                                        }
//

                                        paths2.forEach { path ->
                                            drawPath(path = path, color = Color.Black, style = Stroke(8f))
                                        }
                                        currentPath2?.let {
                                            drawPath(path = it, color = Color.Black, style = Stroke(8f))
                                        }
                                    }

                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(boxSizeDp) // Use the variable
                                        .background(color = Color.White)
                                        .clipToBounds()
                                        .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    waterSound.start()
                                                    val newPath = Path().apply { moveTo(offset.x, offset.y) }
                                                    currentPath3 = newPath
                                                },
                                                onDrag = { change, _ ->
                                                    currentPath3?.lineTo(change.position.x, change.position.y)
                                                    // Trigger recomposition (hacky but works for Path updates)
                                                    currentPath3 = Path().apply {
                                                        currentPath3?.let { addPath(it) }
                                                    }
                                                },
                                                onDragEnd = {
                                                    waterSound.stop()
                                                    currentPath3?.let { paths3.add(it) }
                                                    currentPath3 = null
                                                },
                                                onDragCancel = {
                                                    waterSound.stop()
                                                }
                                            )
                                        }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        with(viewModelSmall) {
                                            drawDottedLetters()
                                        }
//

                                        paths3.forEach { path ->
                                            drawPath(path = path, color = Color.Black, style = Stroke(8f))
                                        }
                                        currentPath3?.let {
                                            drawPath(path = it, color = Color.Black, style = Stroke(8f))
                                        }
                                    }

                                }
                                Box(
                                    modifier = Modifier
                                        .size(boxSizeDp) // Use the variable
                                        .background(color = Color.White)
                                        .clipToBounds()
                                        .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
                                        .pointerInput(Unit) {
                                            detectDragGestures(
                                                onDragStart = { offset ->
                                                    waterSound.start()
                                                    val newPath = Path().apply { moveTo(offset.x, offset.y) }
                                                    currentPath4 = newPath
                                                },
                                                onDrag = { change, _ ->
                                                    currentPath4?.lineTo(change.position.x, change.position.y)
                                                    // Trigger recomposition (hacky but works for Path updates)
                                                    currentPath4 = Path().apply {
                                                        currentPath4?.let { addPath(it) }
                                                    }
                                                },
                                                onDragEnd = {
                                                    waterSound.stop()
                                                    currentPath4?.let { paths4.add(it) }
                                                    currentPath4 = null
                                                },
                                                onDragCancel = {
                                                    waterSound.stop()
                                                }
                                            )
                                        }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        with(viewModelSmall) {
                                            drawDottedLetterz()
                                        }


                                        paths4.forEach { path ->
                                            drawPath(path = path, color = Color.Black, style = Stroke(8f))
                                        }
                                        currentPath4?.let {
                                            drawPath(path = it, color = Color.Black, style = Stroke(8f))
                                        }
                                    }

                                }
                            }

                        }



                        Box(
                            Modifier
                                .padding(bottom = 16.dp)
                                .width(150.dp)
                                .height(50.dp)
                                .background(color = Color(0xF527B51A), shape = RoundedCornerShape(size = 35.dp))
                                .clickable {
                                    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                    CoroutineScope(Dispatchers.Main).launch {
                                        //For n
                                        val bitmap1 = createBitmapFromPaths(paths1, boxSizePx, boxSizePx)
                                        val bytes1 = bitmapToByteArray(bitmap1)
                                        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                        val result1 = sendImageToFlaskSuspend(currentUser, bytes1, "n", "1")
                                        println("Box 1 | Expected: n | Prediction: $result1")

                                        //For u
                                        val bitmap2 = createBitmapFromPaths(paths2, boxSizePx, boxSizePx)
                                        val bytes2 = bitmapToByteArray(bitmap2)
                                        val result2 = sendImageToFlaskSuspend(currentUser, bytes2, "u", "2")
                                        println("Box 2 | Expected: u | Prediction: $result2")
                                        //For s
                                        val bitmap3 = createBitmapFromPaths(paths3, boxSizePx, boxSizePx)
                                        val bytes3 = bitmapToByteArray(bitmap3)
                                        val result3 = sendImageToFlaskSuspend(currentUser, bytes3, "s", "3")
                                        println("Box 3 | Expected: s | Prediction: $result3")
                                        //For z
                                        val bitmap4 = createBitmapFromPaths(paths4, boxSizePx, boxSizePx)
                                        val bytes4 = bitmapToByteArray(bitmap4)
                                        val result4 = sendImageToFlaskSuspend(currentUser, bytes4, "Z", "4")
                                        println("Box 4 | Expected: Z | Prediction: $result4")

                                    }
                                    onNextScreen()
                                },
                            contentAlignment = Alignment.Center



                        )
                        {
                            Text(
                                text = "Next",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFFFFFFF),

                                    ),


                                )
                        }
                    }
                }
            }

        } //Ending Original Screen

        //Character reading question
        if(overlay_boolean.value) {

            Box(
                modifier = Modifier
                    .offset(x = 0.dp, y = 0.dp)
                    .width(430.dp)
                    .height(932.dp)
                    .background(color = Color(0x4FFFFFFF))
                    .fillMaxSize()

            ) {
                //Speech Bubble Location

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color(0x4FFFFFFF))

                ) {
                    // --- SPEECH BUBBLE (Center Right) ---

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
                            text = "Trace your fingers\n on the Letters",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF27B51A),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                    // --- DORAEMON (Bottom Left) ---

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(R.drawable.doraemon)
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
}