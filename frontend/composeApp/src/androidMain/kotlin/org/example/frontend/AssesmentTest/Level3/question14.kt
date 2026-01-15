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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt




@Composable
fun Question14(){
    val context = LocalContext.current
    val overlay_boolean= remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }
    val part_boolean= remember { mutableStateOf(false) }
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
    //CANVA HANDLE
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    val density = LocalDensity.current
    val targetPixels = 250
    val boxSizeDp =250.dp
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

    fun sendImageToFlask(byteArray: ByteArray, onResult: (String) -> Unit) {
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.png",
                byteArray.toRequestBody("image/png".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("http://192.168.10.108:5000/predict")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FlaskAPI", "Error! ${e.message}", e)
                onResult("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string() ?: "No response"
                Log.d("FlaskAPI", "Response: $result")
                onResult(result)
            }
        })
    }


    //PRESSING SPEAKER FUNCTION
    fun Clicked_Speaker(){
        overlay_boolean.value = true
        speaker_boolean.value = true
    }
    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel3q14)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(3000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }
    //PRESSED OPTION FUNCTION
    fun Clicked_Option(){
        part_boolean.value = true
    }

    Box(
        modifier=Modifier.fillMaxSize(),
    ){
        Image(
            painter=painterResource(R.drawable.assessment_level1q4),
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
                        text = "Question no 14",
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
                        .background(color = Color(0x00FFFFFF))
                        .padding(start = 5.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Circle all “was”",
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
                        modifier = Modifier.offset(y = -5.dp,x=30.dp)
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


                // CIRCLES LIST TABLE
                Column(
                    modifier=Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ){
                    //ROW 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 50.dp,
                            alignment = Alignment.CenterHorizontally
                        )
                    ){
                        OptionCircle("was",
                            onOptionClick = {
                                Clicked_Option()
                            })
                        OptionCircle("saw",
                            onOptionClick = {
                                Clicked_Option()
                            })
                        OptionCircle("was",
                            onOptionClick = {
                                Clicked_Option()
                            })
                    }
                    //ROW 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 50.dp,
                            alignment = Alignment.CenterHorizontally
                        )
                    ){
                        OptionCircle("raw",
                            onOptionClick = {
                                Clicked_Option()
                            })
                        OptionCircle("was",
                            onOptionClick = {
                                Clicked_Option()
                            })
                        OptionCircle("why",
                            onOptionClick = {
                                Clicked_Option()
                            })
                    }
                    //ROW 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 50.dp,
                            alignment = Alignment.CenterHorizontally
                        )
                    ){
                        OptionCircle("saw",
                            onOptionClick = {
                                Clicked_Option()
                            })
                        OptionCircle("way",
                            onOptionClick = {
                                Clicked_Option()
                            })
                        OptionCircle("saw",
                            onOptionClick = {
                                Clicked_Option()
                            })
                    }

                    //ROW 4
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            space = 50.dp,
                            alignment = Alignment.CenterHorizontally
                        )
                    ){
                        OptionCircle("win",
                            onOptionClick = {
                                Clicked_Option()
                            })
                        OptionCircle("was",
                            onOptionClick = {
                                Clicked_Option()
                            })
                        OptionCircle("war",
                            onOptionClick = {
                                Clicked_Option()
                            })
                    }























                }



















            }//END OF MIDDLE BOX
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Position at Bottom Right of the Blur Box
                    .padding(end = 10.dp, bottom = 10.dp) // Add spacing from the edges
                    .background(Color(0xFF27B51A), RoundedCornerShape(15.dp)) // Green bg
                    .clickable {
//                        // 1. Convert drawing to bitmap
//                        val bitmap = createBitmapFromPaths(paths, boxSizePx, boxSizePx)
//                        val byteArray = bitmapToByteArray(bitmap)
//
//                        // 2. Send to Flask API
//                        sendImageToFlask(byteArray) { result ->
//                            Log.d("API_RESULT", result)
//                        }

                        // 3. Navigate to next screen
//                        onNextScreen()
                    }
                    .padding(horizontal = 20.dp, vertical = 5.dp) // Padding inside the button
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
                            text = "Circle all “was”",
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

//REPEATING CIRCLE FUNCTION INSTEAD OF MULTIPLE BOXES ADDING FUNCTION TO PASS CIRCLE TEXT
@Composable
private fun OptionCircle(
    text: String,
    onOptionClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
            .width(50.dp)
            .height(50.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
            .clickable { onOptionClick() }
            .padding(top = 5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.height(20.dp),
            text = text,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight(400),
                textAlign = TextAlign.Center,
                color = Color(0xFF000278),
            )
        )

    }
}