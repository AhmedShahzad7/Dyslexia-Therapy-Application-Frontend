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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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

// Define Colors
val GreenText = Color(0xFF27B51A)
val BlueText = Color(0xFF000278)
val TranslucentWhite = Color(0xC7FFFFFF)
val ErrorRed = Color(0xFFFF0000)
val SelectedBlue = Color(0xFF0099FF)

enum class Direction { UP, DOWN, LEFT, RIGHT }

@Composable
fun Question4(onNextScreen: () -> Unit) {

    // --- STATE VARIABLES ---
    // Variable to change what the popup says
    var popupMessage by remember { mutableStateOf("Match the arrow to the correct word") }

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
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel1q4)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(5000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }


    // Game Logic State
    val selectedArrow = remember { mutableStateOf<Direction?>(null) }
    val selectedWord = remember { mutableStateOf<Direction?>(null) }
    val solvedMatches = remember { mutableStateListOf<Direction>() }
    val isErrorState = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // --- LOGIC FUNCTION ---
    fun checkMatch() {
        val arrow = selectedArrow.value
        val word = selectedWord.value

        if (arrow != null && word != null) {
            if (arrow == word) {
                solvedMatches.add(arrow)
                selectedArrow.value = null
                selectedWord.value = null
            } else {
                isErrorState.value = true
                scope.launch {
                    delay(1000)
                    selectedArrow.value = null
                    selectedWord.value = null
                    isErrorState.value = false
                }
            }
        }
    }

    fun getBorderColor(itemType: String, direction: Direction): Color {
        if (solvedMatches.contains(direction)) return GreenText
        if (isErrorState.value) {
            if (itemType == "arrow" && selectedArrow.value == direction) return ErrorRed
            if (itemType == "word" && selectedWord.value == direction) return ErrorRed
        }
        if (itemType == "arrow" && selectedArrow.value == direction) return SelectedBlue
        if (itemType == "word" && selectedWord.value == direction) return SelectedBlue
        return Color.Transparent
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.question4bkg),
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
                        .background(color = TranslucentWhite, shape = RoundedCornerShape(size = 35.dp))
                        .padding(top = 20.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top)
                ) {
                    // Header
                    Text(
                        modifier = Modifier.width(245.dp).height(62.dp),
                        text = "Question no 4",
                        style = TextStyle(
                            fontSize = 34.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400), color = GreenText,
                            textAlign = TextAlign.Center
                        ),
                    )

                    // Instructions
                    Row(
                        modifier = Modifier.width(299.dp).height(100.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                    ) {
                        Text(
                            modifier = Modifier.width(234.dp).height(56.dp),
                            text = "Match the arrow to\nthe correct word",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400), color = GreenText,
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
                                    .background(color = GreenText, shape = RoundedCornerShape(size = 75.dp)),
                                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.soundbutton_am),
                                    contentDescription = "sound",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Column(
                        modifier = Modifier.width(299.dp),
                        verticalArrangement = Arrangement.spacedBy(40.dp)
                    ) {
                        // Row 1: Arrows
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ArrowItem(
                                iconId = R.drawable.up,
                                borderColor = getBorderColor("arrow", Direction.UP),
                                onClick = {
                                    if (!solvedMatches.contains(Direction.UP) && !isErrorState.value) {
                                        selectedArrow.value = Direction.UP; checkMatch()
                                    }
                                })
                            ArrowItem(
                                iconId = R.drawable.down,
                                borderColor = getBorderColor("arrow", Direction.DOWN),
                                onClick = {
                                    if (!solvedMatches.contains(Direction.DOWN) && !isErrorState.value) {
                                        selectedArrow.value = Direction.DOWN; checkMatch()
                                    }
                                })
                            ArrowItem(
                                iconId = R.drawable.right_am,
                                borderColor = getBorderColor("arrow", Direction.RIGHT),
                                onClick = {
                                    if (!solvedMatches.contains(Direction.RIGHT) && !isErrorState.value) {
                                        selectedArrow.value = Direction.RIGHT; checkMatch()
                                    }
                                })
                            ArrowItem(
                                iconId = R.drawable.left,
                                borderColor = getBorderColor("arrow", Direction.LEFT),
                                onClick = {
                                    if (!solvedMatches.contains(Direction.LEFT) && !isErrorState.value) {
                                        selectedArrow.value = Direction.LEFT; checkMatch()
                                    }
                                })
                        }

                        // Row 2: Words
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            OptionCircle(
                                text = "Left",
                                borderColor = getBorderColor("word", Direction.LEFT),
                                onOptionClick = {
                                    if (!solvedMatches.contains(Direction.LEFT) && !isErrorState.value) {
                                        selectedWord.value = Direction.LEFT
                                        val mediaPlayer = MediaPlayer.create(context, R.raw.left_voice)
                                        mediaPlayer.start()
                                        mediaPlayer.setOnCompletionListener {
                                            it.release()}


                                        checkMatch()
                                    }
                                },
                                onSoundClick = {
                                    popupMessage = "Left"
                                    val mediaPlayer = MediaPlayer.create(context, R.raw.left_voice)
                                    mediaPlayer.start()
                                    mediaPlayer.setOnCompletionListener {
                                        it.release()}


                                }
                            )
                            OptionCircle(
                                text = "Right",
                                borderColor = getBorderColor("word", Direction.RIGHT),
                                onOptionClick = {
                                    if (!solvedMatches.contains(Direction.RIGHT) && !isErrorState.value) {
                                        selectedWord.value = Direction.RIGHT
                                        checkMatch()
                                        val mediaPlayer = MediaPlayer.create(context, R.raw.right_voice)
                                        mediaPlayer.start()
                                        mediaPlayer.setOnCompletionListener {
                                            it.release()}


                                    }
                                },
                                onSoundClick = {
                                    popupMessage = "Right"
                                    val mediaPlayer = MediaPlayer.create(context, R.raw.right_voice)
                                    mediaPlayer.start()
                                    mediaPlayer.setOnCompletionListener {
                                        it.release()}



                                }
                            )
                            OptionCircle(
                                text = "Up",
                                borderColor = getBorderColor("word", Direction.UP),
                                onOptionClick = {
                                    if (!solvedMatches.contains(Direction.UP) && !isErrorState.value) {
                                        selectedWord.value = Direction.UP
                                        val mediaPlayer = MediaPlayer.create(context, R.raw.up_voice)
                                        mediaPlayer.start()
                                        mediaPlayer.setOnCompletionListener {
                                            it.release()}


                                        checkMatch()
                                    }
                                },
                                onSoundClick = {
                                    popupMessage = "Up"
                                    val mediaPlayer = MediaPlayer.create(context, R.raw.up_voice)
                                    mediaPlayer.start()
                                    mediaPlayer.setOnCompletionListener {
                                        it.release()}

                                }
                            )
                            OptionCircle(
                                text = "Down",
                                borderColor = getBorderColor("word", Direction.DOWN),
                                onOptionClick = {
                                    if (!solvedMatches.contains(Direction.DOWN) && !isErrorState.value) {
                                        selectedWord.value = Direction.DOWN
                                        val mediaPlayer = MediaPlayer.create(context, R.raw.down_voice)
                                        mediaPlayer.start()
                                        mediaPlayer.setOnCompletionListener {
                                            it.release()
                                        }

                                        checkMatch()
                                    }
                                },
                                onSoundClick = {
                                    popupMessage = "Down"

                                    val mediaPlayer = MediaPlayer.create(context, R.raw.down_voice)
                                    mediaPlayer.start()
                                    mediaPlayer.setOnCompletionListener {
                                        it.release()
                                    }

                                }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 20.dp, top = 50.dp), // Right padding inside the card
                        horizontalArrangement = Arrangement.End // <--- This forces the button to the right
                    ){
                    Box(
                        modifier = Modifier
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
                        onNextScreen()
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
                        }
                }
            }


            // --- POPUP LOGIC ---
            if (overlay_boolean.value) {
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
                                text = "Match the arrow\n to the correct word",
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
}

@Composable
private fun ArrowItem(iconId: Int, borderColor: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
            .width(50.dp)
            .height(50.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
            .border(width = 4.dp, color = borderColor, shape = RoundedCornerShape(size = 75.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconId),
            contentDescription = "arrow",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun OptionCircle(
    text: String,
    borderColor: Color,
    onOptionClick: () -> Unit,
    onSoundClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
            .width(50.dp)
            .height(50.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
            .border(width = 4.dp, color = borderColor, shape = RoundedCornerShape(size = 75.dp))
            .clickable { onOptionClick() }
            .padding(top = 5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.height(20.dp),
            text = text,
            style = TextStyle(
                fontSize = 13.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight(400),
                color = BlueText,
                textAlign = TextAlign.Center,
            )
        )

        IconButton(
            onClick = { onSoundClick() },
            modifier = Modifier.width(16.dp).height(15.dp),

            ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.soundbutton_am),
                contentDescription = "sound",
                contentScale = ContentScale.None
            )
        }
    }
}