package org.example.frontend.AssesmentTest.Level1

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.frontend.R
import android.media.MediaPlayer
import okhttp3.*
import java.io.IOException
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build.VERSION.SDK_INT
import coil.decode.GifDecoder
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

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
    var popupMessage by remember { mutableStateOf("Match the arrow to the correct word") }

    // GIPHY HANDLER
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

    // --- FLASK HANDLER ---
    val question_number = "4"
    // Use 10.0.2.2 for Emulator, use specific IP for real device
    val ip_address = "http://192.168.0.14:5000"

    fun sendDataToFlask(userid: String, selection: String, onResult: (String) -> Unit) {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userid)
            .addFormDataPart("question_number", question_number)
            .addFormDataPart("arrow_selected", selection)
            .build()

        val request = Request.Builder()
            .url(ip_address + "/predict_q4")
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

    // MEDIA HANDLER
    val overlay_boolean = remember { mutableStateOf(false) }

    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel1q4)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }
            delay(5000)
            overlay_boolean.value = false
        }
    }

    // --- GAME LOGIC STATE ---
    val selectedArrow = remember { mutableStateOf<Direction?>(null) }
    val selectedWord = remember { mutableStateOf<Direction?>(null) }

    // Lists to track locked states
    val solvedMatches = remember { mutableStateListOf<Direction>() }     // Stores Correctly matched directions (Green)
    val wrongArrows = remember { mutableStateListOf<Direction>() }       // Stores Arrows matched incorrectly (Red)
    val wrongWords = remember { mutableStateListOf<Direction>() }        // Stores Words matched incorrectly (Red)

    val scope = rememberCoroutineScope()

    // --- MATCHING LOGIC FUNCTION ---
    fun checkMatch() {
        val arrow = selectedArrow.value
        val word = selectedWord.value

        // Only run if BOTH are selected
        if (arrow != null && word != null) {

            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid ?: "TEST_USER_123"

            // 1. CHECK IF MATCH IS CORRECT
            if (arrow == word) {
                // --- SUCCESS ---
                solvedMatches.add(arrow) // Lock as Green

                // Send "Correct Match"
                sendDataToFlask(userId, "Correct Match: ${arrow.name}") {
                    Log.d("FlaskAPI", "Success sent: $it")
                }

                Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()

            } else {
                // --- ERROR (PERMANENT RED) ---
                // Add to 'Wrong' lists to lock them in RED permanently
                wrongArrows.add(arrow)
                wrongWords.add(word)

                // Send Error String to Backend (Logged as True in DB)
                val errorString = "Error: Arrow(${arrow.name}) matched with Word(${word.name})"
                sendDataToFlask(userId, errorString) { }

                Toast.makeText(context, "Incorrect!", Toast.LENGTH_SHORT).show()
            }

            // Clear selections immediately (No timer/delay, just lock and move on)
            selectedArrow.value = null
            selectedWord.value = null
        }
    }

    // --- COLOR LOGIC (UPDATED FOR PERMANENT RED) ---
    fun getBorderColor(itemType: String, direction: Direction): Color {
        // PRIORITY 1: Is it already solved correctly? (Green)
        if (solvedMatches.contains(direction)) return GreenText

        // PRIORITY 2: Was it matched incorrectly? (Red)
        // Check specific lists because incorrect matches might be mismatched (e.g. Up arrow + Down word)
        if (itemType == "arrow" && wrongArrows.contains(direction)) return ErrorRed
        if (itemType == "word" && wrongWords.contains(direction)) return ErrorRed

        // PRIORITY 3: Is it currently selected? (Blue)
        if (itemType == "arrow" && selectedArrow.value == direction) return SelectedBlue
        if (itemType == "word" && selectedWord.value == direction) return SelectedBlue

        // PRIORITY 4: Default
        return Color.Transparent
    }

    // Helper to check if an item is locked (Green or Red) so it can't be clicked again
    fun isItemLocked(itemType: String, direction: Direction): Boolean {
        if (solvedMatches.contains(direction)) return true
        if (itemType == "arrow" && wrongArrows.contains(direction)) return true
        if (itemType == "word" && wrongWords.contains(direction)) return true
        return false
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
                                popupMessage = "Match the arrow to the correct word"
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
                        // --- ROW 1: ARROWS ---
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            ArrowItem(iconId = R.drawable.up, borderColor = getBorderColor("arrow", Direction.UP),
                                onClick = { if (!isItemLocked("arrow", Direction.UP)) { selectedArrow.value = Direction.UP; checkMatch() } })

                            ArrowItem(iconId = R.drawable.down, borderColor = getBorderColor("arrow", Direction.DOWN),
                                onClick = { if (!isItemLocked("arrow", Direction.DOWN)) { selectedArrow.value = Direction.DOWN; checkMatch() } })

                            ArrowItem(iconId = R.drawable.right_am, borderColor = getBorderColor("arrow", Direction.RIGHT),
                                onClick = { if (!isItemLocked("arrow", Direction.RIGHT)) { selectedArrow.value = Direction.RIGHT; checkMatch() } })

                            ArrowItem(iconId = R.drawable.left, borderColor = getBorderColor("arrow", Direction.LEFT),
                                onClick = { if (!isItemLocked("arrow", Direction.LEFT)) { selectedArrow.value = Direction.LEFT; checkMatch() } })
                        }

                        // --- ROW 2: WORDS ---
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            OptionCircle(text = "Left", borderColor = getBorderColor("word", Direction.LEFT),
                                onOptionClick = { if (!isItemLocked("word", Direction.LEFT)) { selectedWord.value = Direction.LEFT; checkMatch() } },
                                onSoundClick = { MediaPlayer.create(context, R.raw.left_voice).start() })

                            OptionCircle(text = "Right", borderColor = getBorderColor("word", Direction.RIGHT),
                                onOptionClick = { if (!isItemLocked("word", Direction.RIGHT)) { selectedWord.value = Direction.RIGHT; checkMatch() } },
                                onSoundClick = { MediaPlayer.create(context, R.raw.right_voice).start() })

                            OptionCircle(text = "Up", borderColor = getBorderColor("word", Direction.UP),
                                onOptionClick = { if (!isItemLocked("word", Direction.UP)) { selectedWord.value = Direction.UP; checkMatch() } },
                                onSoundClick = { MediaPlayer.create(context, R.raw.up_voice).start() })

                            OptionCircle(text = "Down", borderColor = getBorderColor("word", Direction.DOWN),
                                onOptionClick = { if (!isItemLocked("word", Direction.DOWN)) { selectedWord.value = Direction.DOWN; checkMatch() } },
                                onSoundClick = { MediaPlayer.create(context, R.raw.down_voice).start() })
                        }
                    }

                    // --- NEXT BUTTON ---
                    // Calculate total finished pairs (Correct + Incorrect)
                    val totalFinished = solvedMatches.size + wrongArrows.size

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 20.dp, top = 50.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 10.dp, bottom = 10.dp)
                                .background(
                                    // Active only if all 4 arrows have been matched (Right or Wrong)
                                    color = if (totalFinished >= 4) Color(0xFF27B51A) else Color.Gray,
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .clickable {
                                    if (totalFinished >= 4) {
                                        scope.launch {
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            val userId = currentUser?.uid ?: "TEST_USER_123"
                                            sendDataToFlask(userId, "Completed") { onNextScreen() }
                                        }
                                    } else {
                                        Toast.makeText(context, "Finish matching all items first!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 5.dp)
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
            // Popup logic...
            if (overlay_boolean.value) {
                Box(modifier = Modifier.offset(x = 0.dp, y = 0.dp)
                    .width(430.dp)
                    .height(932.dp)
                    .background(color = Color(0x4FFFFFFF))
                    .fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()
                        .background(color = Color(0x4FFFFFFF))) {
                        Box(contentAlignment = Alignment.Center,
                            modifier = Modifier.align(Alignment.CenterEnd).
                            offset(y = -120.dp)) {
                            Image(painter = painterResource(R.drawable.speech_bubble),
                                contentDescription = "")
                            Text(text = "Match the arrow\n to the correct word",
                                style = TextStyle(
                                    fontSize = 25.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFF27B51A),
                                    textAlign = TextAlign.Center))
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(R.drawable.doraemon).build(),
                            imageLoader = imageLoader,
                            contentDescription = "Doraemon GIF",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.width(327.dp)
                                .height(327.dp)
                                .offset(y = -120.dp)
                                .align(Alignment.BottomStart))
                    }
                }
            }
        }
    }
}

// Helpers...
@Composable
private fun ArrowItem(iconId: Int, borderColor: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
            .width(50.dp).height(50.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
            .border(width = 4.dp, color = borderColor, shape = RoundedCornerShape(size = 75.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = iconId),
            contentDescription = "arrow",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun OptionCircle(text: String, borderColor: Color, onOptionClick: () -> Unit, onSoundClick: () -> Unit) {
    Column(
        modifier = Modifier
            .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
            .width(50.dp).height(50.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
            .border(width = 4.dp, color = borderColor, shape = RoundedCornerShape(size = 75.dp))
            .clickable { onOptionClick() }
            .padding(top = 5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(modifier = Modifier
            .height(20.dp),
            text = text,
            style = TextStyle(fontSize = 13.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight(400),
                color = BlueText,
                textAlign = TextAlign.Center))
        IconButton(onClick = { onSoundClick() },
            modifier = Modifier
                .width(16.dp)
                .height(15.dp)) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.soundbutton_am),
                contentDescription = "sound", contentScale = ContentScale.None)
        }
    }
}