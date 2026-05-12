package org.example.frontend.quizzes.quiz3

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
// QUIZ QUESTION 1  —  "Circle the option that matches the word"  (MCQ)
//
// FIX: The backend /get_personalized_question_quiz3 routes on "1"/"2"/"3",
//      not on "11". We now fetch with question_number=1 and submit with
//      question_number=1 so Firestore tracks Q1 correctly for the 75% flag.
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuizQuestion1(onNextScreen: () -> Unit) {

    // "1" → backend MCQ branch; stored as 1 in Firestore
    val FETCH_Q_NUM  = "1"
    val SUBMIT_Q_NUM = 1

    val ip      = NetworkConfig.SERVER_IP
    val context = LocalContext.current

    var isLoading        by remember { mutableStateOf(true) }
    var questionText     by remember { mutableStateOf("Circle the option that\n matches with the word") }
    var dynamicAudioUrl  by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying   by remember { mutableStateOf(false) }
    var targetWords      by remember { mutableStateOf<List<String>>(emptyList()) }
    var optionsLists     by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var currentPartIndex by remember { mutableStateOf(0) }

    val overlayBoolean = remember { mutableStateOf(false) }
    val currentUser    = FirebaseAuth.getInstance().currentUser

    fun setupDefaults() {
        if (targetWords.isEmpty()) {
            targetWords  = listOf("pen")
            optionsLists = listOf(listOf("ten", "qen", "pen"))
        }
    }

    // ── Fetch ─────────────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            val client  = OkHttpClient()
            val request = Request.Builder()
                .url("http://$ip/get_personalized_question_quiz3?user_id=$uid&question_number=$FETCH_Q_NUM")
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
                                val dataArray = json.optJSONArray("data")
                                if (dataArray != null && dataArray.length() > 0) {
                                    val newTargets = mutableListOf<String>()
                                    val newOptions = mutableListOf<List<String>>()
                                    for (i in 0 until dataArray.length()) {
                                        val item = dataArray.getJSONObject(i)
                                        newTargets.add(item.optString("target", "word"))
                                        val optsJson = item.optJSONArray("options")
                                        val opts     = mutableListOf<String>()
                                        if (optsJson != null)
                                            for (j in 0 until optsJson.length())
                                                opts.add(optsJson.getString(j))
                                        newOptions.add(opts)
                                    }
                                    targetWords  = newTargets
                                    optionsLists = newOptions
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

    // ── Doraemon / audio overlay ──────────────────────────────────────────────
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
            val mp = MediaPlayer.create(context, R.raw.doraemon_alevel3q11)
            mp.start()
            mp.setOnCompletionListener { it.release() }
            delay(3000)
            overlayBoolean.value = false
        }
    }

    // ── Option tap → submit then advance ─────────────────────────────────────
    fun onOptionClicked(selected: String) {
        val target    = targetWords[currentPartIndex]
        val isCorrect = (selected == target)
        val advance   = {
            if (currentPartIndex < targetWords.size - 1) currentPartIndex++
            else onNextScreen()
        }
        currentUser?.uid?.let { uid ->
            submitTherapyAnswer(uid, SUBMIT_Q_NUM, target, isCorrect) { advance() }
        } ?: advance()
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
                    .width(299.dp).height(550.dp)
                    .background(Color(0xC7FFFFFF), RoundedCornerShape(35.dp))
                    .align(Alignment.Center)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().height(100.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Quiz 3 Question 1",
                            style = TextStyle(
                                fontSize   = 34.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth().height(100.dp)
                            .background(Color.Transparent)
                            .padding(start = 5.dp)
                    ) {
                        Text(
                            text  = questionText,
                            style = TextStyle(
                                fontSize   = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                        Box(modifier = Modifier.offset(y = 0.dp)) {
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

                    if (targetWords.isNotEmpty() && currentPartIndex < targetWords.size) {
                        Row(
                            modifier              = Modifier.height(55.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text  = targetWords[currentPartIndex],
                                style = TextStyle(
                                    fontSize   = 50.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color      = Color(0xFFF8335D),
                                    textAlign  = TextAlign.Center
                                )
                            )
                        }

                        Row(
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(start = 7.dp, top = 58.dp, end = 7.dp, bottom = 58.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            optionsLists[currentPartIndex].forEach { option ->
                                QuizOptionCircle(text = option) { onOptionClicked(option) }
                            }
                        }
                    }
                }
            }

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
                                fontSize   = 25.sp,
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
// Shared option circle — plain (no selection state)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun QuizOptionCircle(text: String, onOptionClick: () -> Unit) {
    Column(
        modifier = Modifier
            .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
            .size(75.dp)
            .background(Color(0xFFFFFFFF), RoundedCornerShape(75.dp))
            .clickable { onOptionClick() }
            .padding(top = 5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.height(20.dp),
            text     = text,
            style    = TextStyle(
                fontSize   = 20.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight(400),
                textAlign  = TextAlign.Center,
                color      = Color(0xFF000278)
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared submit helper — used by QuizQuestion1, QuizQuestion2, QuizQuestion3
// Posts to /submit_quiz_answer which updates the 75% flag in Firestore.
// ─────────────────────────────────────────────────────────────────────────────
fun submitTherapyAnswer(
    uid: String,
    qNum: Int,
    target: String,
    correct: Boolean,
    onDone: () -> Unit
) {
    val client  = OkHttpClient()
    val body    = FormBody.Builder()
        .add("user_id",         uid)
        .add("question_number", qNum.toString())
        .add("target_word",     target)
        .add("is_correct",      correct.toString())
        .build()
    val request = Request.Builder()
        .url("http://${NetworkConfig.SERVER_IP}/submit_quiz_answer")
        .post(body).build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Handler(Looper.getMainLooper()).post { onDone() }
        }
        override fun onResponse(call: Call, response: Response) {
            Handler(Looper.getMainLooper()).post { onDone() }
        }
    })
}