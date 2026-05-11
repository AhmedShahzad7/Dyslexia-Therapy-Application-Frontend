package org.example.frontend.therapy.level3

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import okio.IOException
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import org.json.JSONObject

// ─────────────────────────────────────────────────────────────────────────────
// TOP-LEVEL HELPER — posts to /check_answers_therapy so the backend:
//   • On WRONG : stores word in Level_Schema and increments Threshold_Count
//   • On RIGHT : deletes the Level_Schema document (question mastered)
// ─────────────────────────────────────────────────────────────────────────────
fun submitTherapyAnswer(
    userID: String,
    qNum: Int,
    targetWord: String,
    isCorrect: Boolean,
    onResult: () -> Unit
) {
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("user_id", userID)
        .addFormDataPart("question_number", qNum.toString())
        .addFormDataPart("target_word", targetWord)
        .addFormDataPart("is_correct", isCorrect.toString())
        .build()

    val request = Request.Builder()
        .url("http://${NetworkConfig.SERVER_IP}/check_answers_therapy")
        .post(requestBody)
        .build()

    OkHttpClient().newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Handler(Looper.getMainLooper()).post { onResult() }
        }
        override fun onResponse(call: Call, response: Response) {
            Handler(Looper.getMainLooper()).post { onResult() }
        }
    })
}

// ─────────────────────────────────────────────────────────────────────────────
// QUESTION L11  —  "Circle the option that matches with the word"
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuestionL11(onNextScreen: () -> Unit) {
    val CURRENT_QUESTION_NUMBER = 11
    val ip = NetworkConfig.SERVER_IP
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var questionText by remember { mutableStateOf("Circle the option that\n matches with the word") }
    var dynamicAudioUrl by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    var targetWords by remember { mutableStateOf<List<String>>(emptyList()) }
    var optionsLists by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var currentPartIndex by remember { mutableStateOf(0) }

    val overlay_boolean = remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Default data shown when network call fails
    fun setupDefaults() {
        if (targetWords.isEmpty()) {
            targetWords = listOf("pen")
            optionsLists = listOf(listOf("ten", "qen", "pen"))
        }
    }

    // ── Fetch personalised question from backend ──────────────────────────────
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://$ip/get_personalized_question?user_id=$uid&question_number=$CURRENT_QUESTION_NUMBER")
                .get().build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post { setupDefaults(); isLoading = false }
                }
                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    Handler(Looper.getMainLooper()).post {
                        if (responseData != null) {
                            try {
                                val json = JSONObject(responseData)
                                dynamicAudioUrl = if (json.isNull("audio_url")) null else json.getString("audio_url")
                                val dataArray = json.optJSONArray("data")
                                if (dataArray != null && dataArray.length() > 0) {
                                    val newTargets = mutableListOf<String>()
                                    val newOptionsLists = mutableListOf<List<String>>()
                                    for (i in 0 until dataArray.length()) {
                                        val item = dataArray.getJSONObject(i)
                                        newTargets.add(item.optString("target", "word"))
                                        val optsJson = item.optJSONArray("options")
                                        val optsList = mutableListOf<String>()
                                        if (optsJson != null) {
                                            for (j in 0 until optsJson.length()) optsList.add(optsJson.getString(j))
                                        }
                                        newOptionsLists.add(optsList)
                                    }
                                    targetWords = newTargets
                                    optionsLists = newOptionsLists
                                } else { setupDefaults() }
                            } catch (e: Exception) { setupDefaults() }
                        } else { setupDefaults() }
                        isLoading = false
                    }
                }
            })
        } ?: run { setupDefaults(); isLoading = false }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context).components {
            if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
        }.build()
    }

    // ── Audio overlay ─────────────────────────────────────────────────────────
    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value && !dynamicAudioUrl.isNullOrEmpty()) {
            isAudioPlaying = true
            try {
                val mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA).build()
                    )
                    setDataSource(dynamicAudioUrl)
                    setOnPreparedListener { mp -> mp.start() }
                    setOnCompletionListener { mp ->
                        mp.release(); isAudioPlaying = false; overlay_boolean.value = false
                    }
                    setOnErrorListener { mp, _, _ ->
                        mp.release(); isAudioPlaying = false; overlay_boolean.value = false; true
                    }
                }
                mediaPlayer.prepareAsync()
            } catch (e: Exception) { isAudioPlaying = false; overlay_boolean.value = false }
        } else if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel3q11)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }
            delay(3000)
            overlay_boolean.value = false
        }
    }

    // ── Option tap handler ────────────────────────────────────────────────────
    fun onOptionClicked(selectedOption: String) {
        val currentTarget = targetWords[currentPartIndex]
        val isCorrect = (selectedOption == currentTarget)

        val advance = {
            if (currentPartIndex < targetWords.size - 1) currentPartIndex++
            else onNextScreen()
        }

        currentUser?.uid?.let { userId ->
            // Check threshold and correctness before moving forward
            submitTherapyAnswer(userId, CURRENT_QUESTION_NUMBER, currentTarget, isCorrect) {
                advance()
            }
        } ?: advance()
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.therapy_level3),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFF8335D)
            )
        } else {
            // Card
            Box(
                modifier = Modifier
                    .width(299.dp).height(550.dp)
                    .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(35.dp))
                    .align(Alignment.Center)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Question number
                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question no $CURRENT_QUESTION_NUMBER",
                            style = TextStyle(
                                fontSize = 34.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFF8335D),
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    // Instruction + speaker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth().height(100.dp)
                            .background(color = Color.Transparent)
                            .padding(start = 5.dp)
                    ) {
                        Text(
                            text = questionText,
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFF8335D),
                                textAlign = TextAlign.Center
                            )
                        )
                        Box(modifier = Modifier.offset(y = 0.dp)) {
                            IconButton(
                                onClick = { overlay_boolean.value = true },
                                enabled = !isAudioPlaying
                            ) {
                                Image(
                                    modifier = Modifier.size(35.dp),
                                    painter = painterResource(id = R.drawable.sound_button1),
                                    contentDescription = "Speaker",
                                    contentScale = ContentScale.None
                                )
                            }
                        }
                    }

                    // Target word + option circles
                    if (targetWords.isNotEmpty() && currentPartIndex < targetWords.size) {
                        Row(
                            modifier = Modifier.height(55.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = targetWords[currentPartIndex],
                                style = TextStyle(
                                    fontSize = 50.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFF8335D),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 7.dp, top = 58.dp, end = 7.dp, bottom = 58.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            optionsLists[currentPartIndex].forEach { optionText ->
                                OptionCircle(text = optionText) { onOptionClicked(optionText) }
                            }
                        }
                    }
                }
            }

            // Doraemon overlay
            if (overlay_boolean.value) {
                Box(modifier = Modifier.fillMaxSize().background(color = Color(0x4FFFFFFF))) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)
                    ) {
                        Image(painter = painterResource(R.drawable.speech_bubble3), contentDescription = "")
                        Text(
                            text = questionText,
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFFF8335D),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(R.drawable.doraemon).build(),
                        imageLoader = imageLoader,
                        contentDescription = "Doraemon",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .size(327.dp).offset(y = (-120).dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionCircle(text: String, onOptionClick: () -> Unit) {
    Column(
        modifier = Modifier
            .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
            .size(75.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(75.dp))
            .clickable { onOptionClick() }
            .padding(top = 5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.height(20.dp),
            text = text,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight(400),
                textAlign = TextAlign.Center,
                color = Color(0xFF000278)
            )
        )
    }
}