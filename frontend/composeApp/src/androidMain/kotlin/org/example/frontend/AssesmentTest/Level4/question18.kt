package org.example.frontend.AssesmentTest.Level4

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import okhttp3.*
import org.example.frontend.R
import java.io.IOException

@Composable
fun Question18(onNextScreen: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- STATE VARIABLES ---
    val overlay_boolean = remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }

    // Track selected indices to know which words are highlighted
    val selectedIndices = remember { mutableStateListOf<Int>() }

    // --- FLASK CONFIGURATION ---
    val ip_address = "http://192.168.43.84:5000"
    val question_number = "18"

    // --- FLASK FUNCTION ---
    fun sendDataToFlask(userId: String, selectedWords: List<String>) {
        val client = OkHttpClient()

        // Manual JSON conversion: ["word1", "word2"]
        val jsonAnswers = selectedWords.joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId)
            .addFormDataPart("question_number", question_number)
            .addFormDataPart("answers_list", jsonAnswers)
            .build()

        val request = Request.Builder()
            .url("$ip_address/check_answers_q18")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FlaskAPI", "Error: ${e.message}")
                // Optional: Even on failure, you might want to move forward for better UX
                Handler(Looper.getMainLooper()).post { onNextScreen() }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                Log.d("FlaskAPI", "Response: $result")

                // Navigate to next screen on success
                Handler(Looper.getMainLooper()).post {
                    onNextScreen()
                }
            }
        })
    }

    // --- IMAGE LOADER ---
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

    // --- SPEAKER LOGIC ---
    fun Clicked_Speaker() {
        overlay_boolean.value = true
        speaker_boolean.value = true
    }

    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.bog)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                it.release()
            }
            delay(5000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }

    val CurrentSentenceString = "The dog and hog went to the bog In the bog they saw a frog on a log and fog covered the bog as they jog"
    val wordsList = CurrentSentenceString.split(Regex("\\s+")).filter { it.isNotBlank() }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.assessment_level1q3),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // Main Card
        Box(
            modifier = Modifier
                .width(370.dp)
                .height(670.dp)
                .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                .align(Alignment.Center)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth().padding(top = 32.dp)
                        .height(62.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question no 18",
                        Modifier.width(245.dp).height(62.dp),
                        style = TextStyle(
                            fontSize = 34.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xF527B51A),
                            textAlign = TextAlign.Center,
                        )
                    )
                }

                // Instruction Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(color = Color(0x00FFFFFF))
                        .padding(start = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Select the word “bog” in the \n sentence",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF27B51A),
                                textAlign = TextAlign.Center,
                            )
                        )
                        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                            IconButton(onClick = { Clicked_Speaker() }) {
                                Image(
                                    modifier = Modifier.width(35.dp).height(35.dp),
                                    painter = painterResource(id = R.drawable.sound_button),
                                    contentDescription = "sound",
                                    contentScale = ContentScale.None
                                )
                            }
                        }
                    }
                }

                // Word Grid
                Column(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        wordsList.forEachIndexed { index, word ->
                            key("word_$index") {
                                WordBox(
                                    word = word,
                                    isSelected = selectedIndices.contains(index),
                                    onClick = {
                                        if (selectedIndices.contains(index)) {
                                            selectedIndices.remove(index)
                                        } else {
                                            selectedIndices.add(index)
                                        }
                                    },
                                    modifier = Modifier.width(75.dp).height(50.dp)
                                )
                            }
                        }
                    }
                }

                // NEXT BUTTON
                Box(
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .background(Color(0xFF27B51A), RoundedCornerShape(15.dp))
                        .clickable {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                // Gather selected words
                                val selectedWords = selectedIndices.map { wordsList[it] }
                                sendDataToFlask(currentUser.uid, selectedWords)
                            } else {
                                // Fallback for testing without auth
                                onNextScreen()
                            }
                        }
                        .padding(horizontal = 40.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Next",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        // Popup Overlay
        if (overlay_boolean.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0x4FFFFFFF))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.speech_bubble),
                            contentDescription = "",
                        )
                        Text(
                            text = "Select the word “bog” in the \n sentence",
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
                        model = ImageRequest.Builder(context).data(R.drawable.doraemon).build(),
                        imageLoader = imageLoader,
                        contentDescription = "Doraemon GIF",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .width(327.dp)
                            .height(327.dp)
                            .offset(y = (-120).dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}

@Composable
fun WordBox(
    word: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isSelected) {
        if (word.equals("bog", ignoreCase = true)) Color(0xF527B51A) else Color.Red
    } else {
        Color.LightGray
    }

    Column(
        modifier = modifier
            .border(
                width = 2.dp,
                color = statusColor,
                shape = RoundedCornerShape(8.dp)
            )
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .clipToBounds()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = word,
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.windsol)),
                    color = if (isSelected) statusColor else Color.Black,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}