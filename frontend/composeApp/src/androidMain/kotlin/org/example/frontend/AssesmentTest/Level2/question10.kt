package org.example.frontend.AssesmentTest.Level2


import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
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
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import org.example.frontend.R
import coil.ImageLoader
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.example.frontend.AssesmentTest.Level4.wordboxes


@Composable
fun Question10(onNextScreen:()->Unit){
    val context = LocalContext.current
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
            val mediaPlayer = MediaPlayer.create(context, R.raw.reverse_letter)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(5000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }

    val RversedSentenceString="a f o ᗺ  d e p h c v Ↄ Ↄ"
    val wordsListreversed = RversedSentenceString.split(Regex("\\s+")).filter { it.isNotBlank() }

    Box(
        modifier=Modifier.fillMaxSize(),
    )
    {
        Image(
            painter = painterResource(id = R.drawable.question5bkg),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )


        Box(
            modifier=Modifier
                .width(299.dp)
                .height(550.dp)
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
                        text = "Question no 10",
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
                            text = "Click Box of each\n reversed letter",
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        maxItemsInEachRow = 4 // Words are wider, so fewer per row
                    ) {
                        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        wordsListreversed.forEachIndexed { index, word ->
                            key("word_$index") {
                                // Updated to call your wordboxes function
                                wordboxesReversed(
                                    word = word,
                                    userid = currentUser,
                                    modifier = Modifier
                                        .width(75.dp) // Adjusted width to fit better
                                        .height(50.dp)
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp) // optional padding
                            .height(50.dp) // enough space for the button
                    ) {
                        Button(
                            onClick = { onNextScreen() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .height(50.dp)
                                .width(120.dp),
                            shape = RoundedCornerShape(25.dp), // rounded button
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xF527B51A)) // green
                        ) {
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
                            text = "Click Box of each\n reversed letter",
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


//@Composable
//fun wordboxesReversed(word: String, modifier: Modifier = Modifier){
//    val isSelected = remember { mutableStateOf(false) }
//
//    Column(
//        modifier = modifier
//            .border(
//                width = 2.dp,
//                color = if(isSelected.value) Color(0xF527B51A) else Color.LightGray,
//                shape = RoundedCornerShape(8.dp)
//            )
//
//            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
//            .clickable { isSelected.value = !isSelected.value }
//            .clipToBounds()
//    ) {
//        Box(modifier = Modifier.fillMaxSize()) {
//
//            // 1. THE WORD GUIDE (Background)
//            Text(
//                text = word,
//                modifier = Modifier.align(Alignment.Center),
//                style = TextStyle(
//                    fontSize = 25.sp,
//                    fontFamily = FontFamily(Font(R.font.windsol)),
//                    color = if(isSelected.value) Color(0xF527B51A) else Color.Black,
//                    textAlign = TextAlign.Center
//                )
//            )
//        }
//    }
//}

@Composable
fun wordboxesReversed(word: String, userid: String, modifier: Modifier = Modifier) {
    // Track the border color: Gray (default), Green (correct), Red (wrong)
    var borderColor by remember { mutableStateOf(Color.LightGray) }
    var isProcessing by remember { mutableStateOf(false) }

    val question_number="10"
    val ip_address="http://192.168.0.14:5000"
    fun sendLetterToFlask(userid:String,letter: String,onResult: (String) -> Unit) {
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userid)
            .addFormDataPart("question_number", question_number)
            .addFormDataPart("reversed_selected", letter)
            .build()

        val request = Request.Builder()
            .url(ip_address+"/predict_q10")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FlaskAPI", "Error! ${e.message}", e)
                Handler(Looper.getMainLooper()).post {
                    onResult("Error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string() ?: "No response"
                Log.d("FlaskAPI", "Response: $result")
                Handler(Looper.getMainLooper()).post {
                    onResult(result)
                }
            }
        })
    }

    Column(
        modifier = modifier
            .border(
                width = 3.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            .clickable(enabled = !isProcessing) { // Prevent double clicks
                isProcessing = true
                sendLetterToFlask(userid, word) { response ->
                    // Logic to parse the response
                    borderColor = if (response.trim()=="correct") {
                        Color(0xFF27B51A) // Green

                    } else {
                        Color.Red
                    }
                    isProcessing = false
                }
            }
            .clipToBounds()
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = word,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.windsol)),
                    color = Color.Black
                )
            )
        }
    }
}