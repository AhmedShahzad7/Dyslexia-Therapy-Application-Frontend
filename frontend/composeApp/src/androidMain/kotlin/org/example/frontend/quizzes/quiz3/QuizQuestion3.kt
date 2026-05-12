package org.example.frontend.quizzes.quiz3

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
// QUIZ QUESTION 3  —  "Circle the words that rhyme the same"  (like Q13)
// Pink theme · windsol font · Doraemon overlay · sound button
//
// FIX: replaced undefined quizSubmitAnswer() with submitTherapyAnswer()
//      which is defined in QuizQuestion1.kt (same package).
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuizQuestion3(onNextScreen: () -> Unit) {

    val CURRENT_QUESTION_NUMBER = 3   // fetch & submit both use "3"
    val ip      = NetworkConfig.SERVER_IP
    val context = LocalContext.current

    var isLoading       by remember { mutableStateOf(true) }
    var questionText    by remember { mutableStateOf("Circle the words that\n rhyme the same") }
    var dynamicAudioUrl by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying  by remember { mutableStateOf(false) }

    val overlayBoolean  = remember { mutableStateOf(false) }
    val selectedIndices = remember { mutableStateOf(setOf<Int>()) }
    var gridWords       by remember { mutableStateOf<List<String>>(emptyList()) }
    var targetWord      by remember { mutableStateOf("") }

    val currentUser = FirebaseAuth.getInstance().currentUser

    fun setupDefaults() {
        targetWord = "lap"
        gridWords  = listOf(
            "lap", "cap", "bun", "map",
            "tub", "bat", "nut", "nap",
            "man", "bag", "tap", "bed"
        )
    }

    // ── Fetch from backend ────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            val client  = OkHttpClient()
            val request = Request.Builder()
                .url("http://$ip/get_personalized_question_quiz3?user_id=$uid&question_number=$CURRENT_QUESTION_NUMBER")
                .get().build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Handler(Looper.getMainLooper()).post { setupDefaults(); isLoading = false }
                }
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    Handler(Looper.getMainLooper()).post {
                        if (body != null) {
                            try {
                                val json = JSONObject(body)
                                dynamicAudioUrl = if (json.isNull("audio_url")) null
                                else json.getString("audio_url")
                                targetWord = json.optString("target_word", "")
                                val dataArray = json.optJSONArray("data")
                                if (dataArray != null && dataArray.length() >= 12) {
                                    val newGrid = mutableListOf<String>()
                                    for (i in 0 until 12) newGrid.add(dataArray.getString(i))
                                    gridWords = newGrid
                                } else setupDefaults()
                            } catch (e: Exception) { setupDefaults() }
                        } else setupDefaults()
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
    LaunchedEffect(overlayBoolean.value) {
        if (overlayBoolean.value && !dynamicAudioUrl.isNullOrEmpty()) {
            isAudioPlaying = true
            try {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA).build()
                    )
                    setDataSource(dynamicAudioUrl)
                    setOnPreparedListener  { it.start() }
                    setOnCompletionListener { it.release(); isAudioPlaying = false; overlayBoolean.value = false }
                    setOnErrorListener      { it, _, _ -> it.release(); isAudioPlaying = false; overlayBoolean.value = false; true }
                }
                mp.prepareAsync()
            } catch (e: Exception) { isAudioPlaying = false; overlayBoolean.value = false }
        } else if (overlayBoolean.value) {
            val mp = MediaPlayer.create(context, R.raw.doraemon_alevel3q13)
            mp.start()
            mp.setOnCompletionListener { it.release() }
            delay(3000)
            overlayBoolean.value = false
        }
    }

    // ── Submit selections → /submit_quiz_answer ───────────────────────────────
    // Uses submitTherapyAnswer() defined in QuizQuestion1.kt (same package).
    // Each selected word is checked: correct if its last 2 chars match target.
    fun submitAndNavigate() {
        currentUser?.uid?.let { userId ->
            if (selectedIndices.value.isEmpty()) {
                // Nothing selected → submit as incorrect then move on
                submitTherapyAnswer(
                    uid     = userId,
                    qNum    = CURRENT_QUESTION_NUMBER,
                    target  = targetWord,
                    correct = false
                ) { onNextScreen() }
                return@let
            }

            var remaining = selectedIndices.value.size
            selectedIndices.value.forEach { idx ->
                val word      = gridWords.getOrElse(idx) { "" }
                val isCorrect = word.isNotEmpty() &&
                        word.takeLast(2) == targetWord.takeLast(2)
                submitTherapyAnswer(
                    uid     = userId,
                    qNum    = CURRENT_QUESTION_NUMBER,
                    target  = targetWord,
                    correct = isCorrect
                ) {
                    remaining--
                    if (remaining == 0) onNextScreen()
                }
            }
        } ?: onNextScreen()
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter            = painterResource(R.drawable.therapy_level3),
            contentDescription = "",
            contentScale       = ContentScale.FillBounds,
            modifier           = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color    = Color(0xFFF8335D)
            )
        } else {
            Box(
                modifier = Modifier
                    .width(299.dp).height(570.dp)
                    .background(Color(0xC7FFFFFF), RoundedCornerShape(35.dp))
                    .align(Alignment.Center)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Row(
                        modifier              = Modifier.fillMaxWidth().height(80.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Quiz 3 Question 3",
                            style = TextStyle(
                                fontSize   = 34.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                    }

                    // Instruction + sound button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth().height(80.dp)
                            .background(Color.Transparent)
                            .padding(start = 5.dp)
                    ) {
                        Text(
                            text  = questionText,
                            style = TextStyle(
                                fontSize   = 22.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                        Box(modifier = Modifier.offset(x = 10.dp)) {
                            IconButton(
                                onClick  = { overlayBoolean.value = true },
                                enabled  = !isAudioPlaying
                            ) {
                                Image(
                                    modifier           = Modifier.size(35.dp),
                                    painter            = painterResource(R.drawable.sound_button1),
                                    contentDescription = "Speaker",
                                    contentScale       = ContentScale.None
                                )
                            }
                        }
                    }

                    // 4×3 grid
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (row in 0 until 4) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)
                            ) {
                                for (col in 0 until 3) {
                                    val idx = row * 3 + col
                                    if (idx < gridWords.size) {
                                        val isSelected = selectedIndices.value.contains(idx)
                                        QuizOptionCircleQ3(
                                            text       = gridWords[idx],
                                            isSelected = isSelected
                                        ) {
                                            val cur = selectedIndices.value
                                            selectedIndices.value =
                                                if (isSelected) cur - idx else cur + idx
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Next button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 12.dp)
                        .background(Color(0xFFF8335D), RoundedCornerShape(15.dp))
                        .clickable { submitAndNavigate() }
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                ) {
                    Text(
                        text  = "Next",
                        style = TextStyle(
                            fontSize   = 26.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    )
                }
            }

            // Doraemon overlay
            if (overlayBoolean.value) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x4FFFFFFF))) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)
                    ) {
                        Image(painterResource(R.drawable.speech_bubble3), contentDescription = "")
                        Text(
                            text  = questionText,
                            style = TextStyle(
                                fontSize   = 22.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                    }
                    AsyncImage(
                        model              = ImageRequest.Builder(context).data(R.drawable.doraemon).build(),
                        imageLoader        = imageLoader,
                        contentDescription = "Doraemon",
                        contentScale       = ContentScale.FillBounds,
                        modifier           = Modifier
                            .size(327.dp).offset(y = (-120).dp)
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Selectable circle — pink selected state
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuizOptionCircleQ3(
    text: String,
    isSelected: Boolean,
    onOptionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .shadow(elevation = 10.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
            .size(58.dp)
            .background(
                color = if (isSelected) Color(0xFFFFE5EC) else Color.White,
                shape = RoundedCornerShape(58.dp)
            )
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color(0xFFF8335D) else Color.Transparent,
                shape = RoundedCornerShape(58.dp)
            )
            .clickable { onOptionClick() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.height(20.dp),
            text     = text,
            style    = TextStyle(
                fontSize   = 18.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight(400),
                textAlign  = TextAlign.Center,
                color      = if (isSelected) Color(0xFFF8335D) else Color(0xFF000278)
            )
        )
    }
}