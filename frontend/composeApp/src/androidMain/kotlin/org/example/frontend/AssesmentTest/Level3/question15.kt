package org.example.frontend.AssesmentTest.Level3


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import org.example.frontend.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import coil.decode.GifDecoder
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.draw.shadow
import java.io.ByteArrayOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch //


import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.alpha
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


data class CardItemBox(
    val id: Int,
    val word: String,
    val length:Int,
)

//CANVA HANDLER
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


fun sendBatchImagesToFlask(
    userID: String,
    word: String,
    images: List<ByteArray>,
    onResult: (String) -> Unit
) {
    val client = OkHttpClient()
    val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

    // Add Text Data
    multipartBuilder.addFormDataPart("user_id", userID)
    multipartBuilder.addFormDataPart("target_word", word)
    multipartBuilder.addFormDataPart("question_number", "15")

    // Loop to Add All Images
    images.forEachIndexed { index, byteArray ->
        multipartBuilder.addFormDataPart(
            "images", // The key Python will look for (can use getlist)
            "letter_$index.png", // Filename
            byteArray.toRequestBody("image/png".toMediaTypeOrNull())
        )
    }

    val request = Request.Builder()
        .url("http://192.168.1.9:5000/predict_handwriting_batch")
        .post(multipartBuilder.build())
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


@Composable
fun Question15(){
    val context = LocalContext.current
    val overlay_boolean= remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }
    val part_boolean= remember { mutableStateOf(false) }
    val play_boolean=remember{mutableStateOf(false)}
    val cards = remember {
        mutableStateListOf(
            CardItemBox(1, "big",3),
            CardItemBox(2, "on",2),
            CardItemBox(3, "friend",6),
            CardItemBox(4, "frog",4),
            CardItemBox(5, "dad",3),

        )
    }


    //GIPHY HANDLER
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


    //PRESSING SPEAKER FUNCTION
    fun Clicked_Speaker(){
        overlay_boolean.value = true
        speaker_boolean.value = true
    }
    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel3q15)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(4000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }
    //PRESSED OPTION FUNCTION
    fun Clicked_Option(){
        part_boolean.value = true
    }

    //CARD EFFECT
    var autoDismissTop by remember { mutableStateOf(false) }
    LaunchedEffect(cards.size) {
        if (cards.isNotEmpty()) {
            autoDismissTop = false
            autoDismissTop = false
        }
    }


    //CARD PLAY BUTTON   //REMINDER TO ADD ID PARAMETER FOR BACKEND
    fun Clicked_Play(){
        if(!play_boolean.value){
            play_boolean.value = true
        }else{
            play_boolean.value = false
        }

    }



    //DESIGN
    Box(
        modifier=Modifier.fillMaxSize(),
    ){
        Image(
            painter=painterResource(R.drawable.assessment_level1),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )


        //Original Screen
        Box(
            modifier=Modifier
                .width(299.dp)
                .height(550.dp)
                .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                .align(Alignment.Center)
        ){
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                //Question Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Text(
                        text = "Question no 15",
                        style = TextStyle(
                            fontSize = 34.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xF527B51A),
                            textAlign = TextAlign.Center,
                        )
                    )
                }
                //Intruction Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(color = Color(0x00FFFFFF)),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Write the word\n below in the boxes\n given",
                        style = TextStyle(
                            fontSize = 25.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF27B51A),
                            textAlign = TextAlign.Center,
                        )
                    )
                    //Speaker Button
                    Box(
                        modifier = Modifier.offset(x = 10.dp)
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


//CARD BOX

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    cards.forEachIndexed { index, card ->
                        val isTopCard = index == cards.lastIndex
                        var triggerAutoSwipe by remember { mutableStateOf(false) }
                        var isLoading by remember { mutableStateOf(false) }
                        SwipeCard_Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    // Slight scale for cards underneath
                                    val scale = if (isTopCard) 1f else 0.95f
                                    scaleX = scale
                                    scaleY = scale

                                    // Push lower cards slightly down
                                    translationY = (cards.lastIndex - index) * 10f
                                },
                            enabled = false,
                            autoDismiss = triggerAutoSwipe,
                            onDismiss = {
                                if (isTopCard) {

                                    cards.remove(card)
                                    if (cards.isEmpty()) {
                                        // onNextPage()
                                    }
                                }
                            },

                        ) {

                            //CONTENT
                            val drawingState = remember { mutableStateMapOf<Int, List<Path>>() }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(18.dp)
                            ){

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(55.dp),
                                    horizontalArrangement = Arrangement.Center,
                                ){
                                    Text(
                                        text = card.word,
                                        style = TextStyle(
                                            fontSize = 50.sp,
                                            fontFamily = FontFamily(Font(R.font.windsol)),
                                            fontWeight = FontWeight(400),
                                            color = Color(0xF527B51A),
                                            textAlign = TextAlign.Center,
                                        )
                                    )
                                }
//
//                                WordGrid(word = card.word)
                                val chunks = card.word.chunked(3)
                                var globalIndexCounter = 0 // Helps us track index 0, 1, 2, 3... across rows

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    chunks.forEach { chunk ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            chunk.forEach { char ->
                                                val myIndex = globalIndexCounter++

                                                LetterBox(
                                                    char = char,
                                                    paths = drawingState[myIndex] ?: emptyList(),
                                                    onPathUpdate = { newPath ->
                                                        val currentList = drawingState[myIndex] ?: emptyList()
                                                        drawingState[myIndex] = currentList + newPath
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                //SUBMIT BUTTON

                                Row(){
                                    Box(
                                        modifier=Modifier
                                            .width(150.dp)
                                            .height(50.dp)
                                            .background(color = Color(0xF527B51A), shape = RoundedCornerShape(size = 35.dp))
                                            .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                                            .clickable(enabled = !isLoading){
                                                // A. Check if they drew something for every letter
                                                if (drawingState.size < card.word.length) {
                                                    // Optional: Show Toast "Please fill all boxes"
                                                    Log.d("Submission", "Incomplete drawings")
                                                    return@clickable
                                                }
                                                isLoading = true

                                                // B. Prepare data for submission
                                                // We need the density to calculate pixel size for bitmap
                                                val boxSizePx = 150 // 50dp * 3 (approx) or calculated via density

                                                // Convert Map to a List of ByteArrays (Images) in order
                                                val imageList = card.word.indices.map { index ->
                                                    val paths = drawingState[index] ?: emptyList()
                                                    val bitmap = createBitmapFromPaths(paths, boxSizePx, boxSizePx)
                                                    bitmapToByteArray(bitmap)
                                                }
                                                val currentUser = FirebaseAuth.getInstance().currentUser
                                                if (currentUser != null) {
                                                    val userId = currentUser.uid

                                                    // C. Send to Flask
                                                    sendBatchImagesToFlask(
                                                        userID = userId, // Replace with actual ID
                                                        word = card.word,
                                                        images = imageList
                                                    ) { result ->
                                                        Log.d("FlaskAPI", "Result: $result")
                                                        isLoading = false
                                                        // Handle Success (e.g., swipe card away)
                                                        triggerAutoSwipe = true
                                                    }
                                                }else{
                                                    isLoading = false
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ){
                                        Text(
                                            text =if (isLoading) "Checking..." else "Submit",
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFFFFFFFF),
                                                textAlign = TextAlign.Center,
                                            )
                                        )


                                    }


                                }




                            }



                        }
                    }
                }












            }//END OF MIDDLE BOX
//            Box(
//                modifier = Modifier
//                    .align(Alignment.BottomEnd) // Position at Bottom Right of the Blur Box
//                    .padding(end = 10.dp, bottom = 10.dp) // Add spacing from the edges
//                    .background(Color(0xFF27B51A), RoundedCornerShape(15.dp)) // Green bg
//                    .clickable {
////                        // 1. Convert drawing to bitmap
////                        val bitmap = createBitmapFromPaths(paths, boxSizePx, boxSizePx)
////                        val byteArray = bitmapToByteArray(bitmap)
////
////                        // 2. Send to Flask API
////                        sendImageToFlask(byteArray) { result ->
////                            Log.d("API_RESULT", result)
////                        }
//
//                        // 3. Navigate to next screen
////                        onNextScreen()
//                    }
//                    .padding(horizontal = 20.dp, vertical = 5.dp) // Padding inside the button
//            ) {
//                Text(
//                    text = "Next",
//                    style = TextStyle(
//                        fontSize = 26.sp,
//                        fontFamily = FontFamily(Font(R.font.windsol)),
//                        fontWeight = FontWeight.Bold,
//                        color = Color.White
//                    )
//                )
//            }
        } //END OF ORIGINAL SCREEN


        //GREY OVERLAY HANDLED BY IF STATEMENT
        if(overlay_boolean.value){
            Box(
                modifier=Modifier
                    .offset(x = 0.dp, y = 0.dp)
                    .width(430.dp)
                    .height(932.dp)
                    .background(color = Color(0x4FFFFFFF))
                    .fillMaxSize()

            ){
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
                            text = "Write the word\n below in the boxes\n given",
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


    } //END OF PAGE BOX
}




//SSWIPE CARD COMPONENT

@Composable
fun SwipeCard_Box(
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
    var autoDismissTop by remember { mutableStateOf(false) }
    LaunchedEffect(autoDismiss) {
        if (autoDismiss) {
            offsetX.animateTo(
                targetValue = 800f, // slide right
                animationSpec = tween(300)
            )
            alpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(300)
            )
            delay(300)
            onDismiss()
        }
    }
    Row(
        modifier = modifier
            .offset {
                IntOffset(
                    offsetX.value.roundToInt(),
                    offsetY.value.roundToInt()
                )
            }
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
                                // Dismiss
                                launch {
                                    offsetX.animateTo(
                                        targetValue = offsetX.value * 3,
                                        animationSpec = tween(300)
                                    )
                                }
                                launch {
                                    alpha.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(300)
                                    )
                                }
                                delay(300)
                                onDismiss()
                            } else {
                                // Snap back
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
                .shadow(
                    elevation = 25.dp,
                    spotColor = Color(0x40000000),
                    ambientColor = Color(0x40000000)
                )
                .width(259.dp)
                .height(294.55463.dp)
                .background(
                    color = Color(0xE5FFFFFF),
                    shape = RoundedCornerShape(35.dp)
                )
                .padding(10.dp),
            content = content
        )
    }
}




//@Composable
//fun WordGrid(word: String) {
//    // 1. Break the word into chunks of 3 characters
//    // "friend" becomes ["fri", "end"]
//    // "big" becomes ["big"]
//    val chunks = word.chunked(3)
//    var globalIndexCounter = 0
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(18.dp) // Space between rows
//    ) {
//        chunks.forEach { chunk ->
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp), // Space between letters
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                chunk.forEach { char ->
//                    val myIndex = globalIndexCounter++ // Capture current index
//                    LetterBox(
//                        char = char,
//                        // Pass existing paths or empty list if none
//                        paths = drawingState[myIndex] ?: emptyList(),
//                        // When user draws, update the map
//                        onPathUpdate = { newPath ->
//                            val currentList = drawingState[myIndex] ?: emptyList()
//                            drawingState[myIndex] = currentList + newPath
//                        }
//                    )
//                }
//            }
//        }
//    }
//}

// A reusable styled box for a single letter
@Composable
fun LetterBox(char: Char,
    paths: List<Path>,
    onPathUpdate: (Path) -> Unit
    ) {
    var currentPath by remember { mutableStateOf<Path?>(null) }
    val density = LocalDensity.current
    val boxSizeDp = 50.dp

    Box(
        modifier = Modifier
            .size(boxSizeDp) // Use the variable
            .background(color = Color.White)
            .clipToBounds()

            .border(width = 3.dp, color = Color(0xFF27B51A),
                shape = RoundedCornerShape(16.dp))
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
                        currentPath?.let { onPathUpdate(it) }
                        currentPath = null
                    },
                    onDragCancel = {
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




