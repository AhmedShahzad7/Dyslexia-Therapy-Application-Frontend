package org.example.frontend.AssesmentTest.Level1

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.frontend.R
import android.media.MediaPlayer
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
import androidx.compose.runtime.LaunchedEffect

// --- FIX: Made these private to avoid conflict with Question4 ---
private val GreenText1 = Color(0xFF27B51A)
private val TranslucentWhite1 = Color(0xC7FFFFFF)
private val ErrorRed1 = Color(0xFFFF0000)
// Purple is unique to Q5, but good practice to keep private if only used here
private val PurpleBorder = Color(0xFF9747FF)

@Composable
fun Question5() {
    // --- STATE VARIABLES ---
    var popupMessage by remember { mutableStateOf("Click the left foot of the character below ?") }

    // Game Logic State
    val isErrorState = remember { mutableStateOf(false) }
    val isSuccessState = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // --- LOGIC FUNCTION ---
    fun checkAnswer(isLeftFoot: Boolean) {
        if (isLeftFoot) {
            // Correct Answer: Turn Green, wait, then Navigate
            isSuccessState.value = true
            scope.launch {
                delay(1000) // Show green for 1 second
//                onNextScreen()
                isSuccessState.value = false
            }
        } else {
            // Incorrect Answer: Turn Red, wait, then Reset
            isErrorState.value = true
            scope.launch {
                delay(1000) // Show red for 1 second
                isErrorState.value = false
            }
        }
    }



    // Determine Border Color dynamically
    val currentBorderColor = when {
        isSuccessState.value -> GreenText1
        isErrorState.value -> ErrorRed1
        else -> Color.White
    }


    //GIPHY HANDLER
    val context = LocalContext.current
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

    //MEDIA HANDLER
    val overlay_boolean= remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }
    fun Clicked_Speaker(){
        overlay_boolean.value = true
        speaker_boolean.value = true
    }



    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel1q5)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(5000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Image
            Image(
                painter = painterResource(id = R.drawable.question5bkg),
                contentDescription = "Forest Background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Main Content Card
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
                    // --- Header ---
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

                    // --- Instructions Row ---
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


                        // Speaker Button
                        IconButton(
                            onClick = {
                                popupMessage = "Click the left foot of the character below ?"
                                overlay_boolean.value = true
                            },
                            modifier = Modifier.width(35.dp) .height(35.dp)
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
                            // The Box with the Image and Hitboxes
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
                                // Character Image
                                Image(
                                    painter = painterResource(id = R.drawable.mario),
                                    contentDescription = "Character",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(10.dp)
                                )

                                // --- HITBOXES ---

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
                }
            }

            // --- POPUP LOGIC ---
            if (overlay_boolean.value) {


                Box(
                    modifier=Modifier
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
        }
    }
}