package org.example.frontend.AssesmentTest.Level1

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.frontend.R
import android.media.MediaPlayer
import okhttp3.*
import java.io.IOException
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build.VERSION.SDK_INT
import coil.decode.GifDecoder
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Define Colors
private val GreenText1 = Color(0xFF27B51A)
private val TranslucentWhite1 = Color(0xC7FFFFFF)
private val ErrorRed1 = Color(0xFFFF0000)

@Composable
fun Question5(onNextScreen: () -> Unit) {
    // --- STATE VARIABLES ---
    var popupMessage by remember { mutableStateOf("Click the left foot of the character below ?") }

    // Game Logic State
    val isErrorState = remember { mutableStateOf(false) }
    val isSuccessState = remember { mutableStateOf(false) }

    // **FIXED: Scope is required for the Next button**
    val scope = rememberCoroutineScope()

    // --- FLASK HANDLER ---
    val question_number = "5"
    // Use 10.0.2.2 for Android Emulator, or your specific IP
    val ip_address = "http://192.168.0.17:5000"

    fun sendDataToFlask(userid: String, selection: String, onResult: (String) -> Unit) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userid)
            .addFormDataPart("question_number", question_number)
            .addFormDataPart("arrow_selected", selection)
            .build()

        val request = Request.Builder()
            .url(ip_address + "/predict_q5")
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

    val context = LocalContext.current

    // --- LOGIC FUNCTION (NO TIMERS FOR BORDERS) ---
    fun checkAnswer(isLeftFoot: Boolean) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: "TEST_USER_123"

        if (isLeftFoot) {
            // --- CORRECT ANSWER ---
            // Turn Green and stay Green (No Delay)
            isSuccessState.value = true
            isErrorState.value = false

            sendDataToFlask(userId, "Completed") { res ->
                Log.d("FlaskAPI", "Success Sent: $res")
            }
            Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()

        } else {
            // --- INCORRECT ANSWER ---
            // Turn Red and stay Red (No Delay)
            isErrorState.value = true
            isSuccessState.value = false

            sendDataToFlask(userId, "Right") { res ->
                Log.d("FlaskAPI", "Error Sent: $res")
            }
            Toast.makeText(context, "Wrong Foot!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- COLOR LOGIC ---
    val currentBorderColor = when {
        isSuccessState.value -> GreenText1  // Green if Correct
        isErrorState.value -> ErrorRed1     // Red if Wrong
        else -> Color.White
    }

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

    val overlay_boolean = remember { mutableStateOf(false) }

    // This handles the Sound Overlay (Doraemon)
    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel1q5)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            // This delay is ONLY for the sound popup to disappear automatically
            delay(5000)
            overlay_boolean.value = false
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.question5bkg),
                contentDescription = "Forest Background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(299.dp)
                        .height(497.dp)
                        .background(color = TranslucentWhite1, shape = RoundedCornerShape(size = 35.dp))
                        .padding(top = 20.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
                ) {
                    Text(
                        modifier = Modifier.height(50.dp),
                        text = "Question no 5",
                        style = TextStyle(
                            fontSize = 34.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = GreenText1,
                            textAlign = TextAlign.Center
                        ),
                    )

                    Row(
                        modifier = Modifier.width(299.dp).height(100.dp)
                            .padding(start = 10.dp, end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                    ) {
                        Text(
                            modifier = Modifier.width(234.dp).height(56.dp),
                            text = "Click the left foot of\nthe character below ?",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = GreenText1,
                                textAlign = TextAlign.Center
                            ),
                        )

                        IconButton(
                            onClick = {
                                popupMessage = "Click the left foot of the character below ?"
                                overlay_boolean.value = true
                            },
                            modifier = Modifier.width(35.dp).height(35.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = GreenText1, shape = RoundedCornerShape(size = 75.dp)),
                                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.sound_button),
                                    contentDescription = "sound",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.width(299.dp).height(220.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(230.dp)
                                    .border(
                                        width = 3.dp,
                                        color = currentBorderColor,
                                        shape = RoundedCornerShape(2.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.mario),
                                    contentDescription = "Character",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp)
                                )

                                // 1. Left Foot (Correct)
                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(50.dp)
                                        .align(Alignment.BottomStart)
                                        .offset(x = 35.dp, y = (-10).dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { checkAnswer(true) }
                                )

                                // 2. Right Foot (Incorrect)
                                Box(
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(50.dp)
                                        .align(Alignment.BottomEnd)
                                        .offset(x = (-35).dp, y = (-10).dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { checkAnswer(false) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 20.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF27B51A), RoundedCornerShape(20.dp))
                                .clickable {
                                    // Using scope here to launch coroutine for Flask call + Navigation
                                    scope.launch {
                                        val currentUser = FirebaseAuth.getInstance().currentUser
                                        val userId = currentUser?.uid ?: "TEST_USER_123"
                                        sendDataToFlask(userId, "Completed") { result ->
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
                }
            }

            if (overlay_boolean.value) {
                Box(
                    modifier = Modifier
                        .offset(x = 0.dp, y = 0.dp)
                        .width(430.dp)
                        .height(932.dp)
                        .background(color = Color(0x4FFFFFFF))
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Color(0x4FFFFFFF))
                    ) {
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
                                text = "Click the left foot\n of  the character below",
                                style = TextStyle(
                                    fontSize = 25.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFF27B51A),
                                    textAlign = TextAlign.Center,
                                )
                            )
                        }
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
        }
    }
}