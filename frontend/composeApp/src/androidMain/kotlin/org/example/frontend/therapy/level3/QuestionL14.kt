package org.example.frontend.therapy.level3

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
// QUESTION L14  —  'Circle all "[target_word]"'
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuestionL14(onNextScreen: () -> Unit) {
    val CURRENT_QUESTION_NUMBER = 14
    val ip      = NetworkConfig.SERVER_IP
    val context = LocalContext.current

    var isLoading       by remember { mutableStateOf(true) }
    var questionText    by remember { mutableStateOf("Circle the word\n shown below") }
    var dynamicAudioUrl by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying  by remember { mutableStateOf(false) }

    // ---> 1. INDEPENDENT LOCAL STATE FOR CARTOON GIF <---
    var cartoonResId by remember { mutableStateOf(R.drawable.mickey1) }

    val overlay_boolean = remember { mutableStateOf(false) }
    val selectedIndices = remember { mutableStateOf(setOf<Int>()) }
    var gridWords       by remember { mutableStateOf<List<String>>(emptyList()) }
    var targetWord      by remember { mutableStateOf("") }

    val currentUser = FirebaseAuth.getInstance().currentUser

    // ---> 2. LOCAL RESOURCE MAPPER <---
    fun mapCartoonStringToDrawable(cartoon: String): Int {
        return when (cartoon.lowercase().trim()) {
            "mickey" -> R.drawable.mickey1
            "pooh" -> R.drawable.pooh1
            "tom" -> R.drawable.tom1
            "duffy" -> R.drawable.duffy2
            else -> R.drawable.mickey1
        }
    }

    fun setupDefaults() {
        targetWord   = "bog"
        gridWords    = listOf("bog", "dog", "bog", "log", "fog", "bog", "hog", "bog", "cog", "jog", "nog", "tog")
        questionText = "Circle the word\n shown below"
        cartoonResId = R.drawable.mickey1
    }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            val client  = OkHttpClient()
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
                                val json          = JSONObject(responseData)

                                // ---> 3. PARSE AND MAP SELECTION LOCALLY <---
                                val helperStr = json.optString("cartoon_selection", "mickey")
                                cartoonResId = mapCartoonStringToDrawable(helperStr)

                                val fetchedTarget = json.optString("target_word", "")
                                dynamicAudioUrl   = if (json.isNull("audio_url")) null else json.getString("audio_url")
                                questionText      = json.optString("instruction_text", "Circle the word\n shown below")
                                val dataArray     = json.optJSONArray("data")
                                if (dataArray != null && dataArray.length() > 0) {
                                    val newGrid = mutableListOf<String>()
                                    for (i in 0 until dataArray.length()) newGrid.add(dataArray.getString(i))
                                    gridWords  = newGrid
                                    targetWord = fetchedTarget
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

    fun toggleIndex(index: Int) {
        val current = selectedIndices.value
        selectedIndices.value = if (current.contains(index)) current - index else current + index
    }

    fun submitAndNavigate() {
        currentUser?.uid?.let { userId ->
            val targetIndices = gridWords.indices.filter { gridWords[it] == targetWord }.toSet()
            val isCorrect     = selectedIndices.value == targetIndices
            submitTherapyAnswer(
                userID     = userId,
                qNum       = CURRENT_QUESTION_NUMBER,
                targetWord = targetWord,
                isCorrect  = isCorrect
            ) { onNextScreen() }
        } ?: onNextScreen()
    }

    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value && !dynamicAudioUrl.isNullOrEmpty()) {
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
                    setOnCompletionListener { it.release(); isAudioPlaying = false; overlay_boolean.value = false }
                    setOnErrorListener      { it, _, _ -> it.release(); isAudioPlaying = false; overlay_boolean.value = false; true }
                }
                mp.prepareAsync()
            } catch (e: Exception) { isAudioPlaying = false; overlay_boolean.value = false }
        } else if (overlay_boolean.value) {
            val mp = MediaPlayer.create(context, R.raw.doraemon_alevel3q14)
            mp.start()
            mp.setOnCompletionListener { it.release() }
            delay(3000)
            overlay_boolean.value = false
        }
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
            // ── Card: wrapContentHeight so nothing overflows ───────────────────
            Box(
                modifier = Modifier
                    .width(299.dp)
                    .wrapContentHeight()
                    .background(Color(0xC7FFFFFF), RoundedCornerShape(35.dp))
                    .align(Alignment.Center)
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.padding(top = 8.dp)
                ) {
                    // ── Question number ───────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth().height(80.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text  = "Question no $CURRENT_QUESTION_NUMBER",
                            style = TextStyle(
                                fontSize   = 34.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                    }

                    // ── Instruction + sound button ────────────────────────────
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth().height(90.dp)
                            .background(Color.Transparent)
                            .padding(start = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = questionText,
                            modifier = Modifier.weight(1f),
                            style    = TextStyle(
                                fontSize   = 22.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color      = Color(0xFFF8335D),
                                textAlign  = TextAlign.Center
                            )
                        )
                        Box(modifier = Modifier.offset(x = 5.dp)) {
                            IconButton(
                                onClick  = { overlay_boolean.value = true },
                                enabled  = !isAudioPlaying
                            ) {
                                Image(
                                    modifier           = Modifier.size(35.dp),
                                    painter            = painterResource(R.drawable.sound_button1),
                                    contentDescription = "Speaker"
                                )
                            }
                        }
                    }

                    // ── Target word badge ─────────────────────────────────────
                    if (targetWord.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                .border(2.dp, Color(0xFFF8335D), RoundedCornerShape(12.dp))
                                .padding(horizontal = 24.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = targetWord,
                                style = TextStyle(
                                    fontSize   = 42.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color      = Color(0xFFF8335D),
                                    textAlign  = TextAlign.Center
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── 4 × 3 word grid ───────────────────────────────────────
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (row in 0 until 4) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                            ) {
                                for (col in 0 until 3) {
                                    val index = row * 3 + col
                                    if (index < gridWords.size) {
                                        WordCircleL14(
                                            text       = gridWords[index],
                                            isSelected = selectedIndices.value.contains(index),
                                            onTap      = { toggleIndex(index) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Next button — INSIDE the Column ───────────────────────
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(end = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
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
                }
            }

            // ── Dynamic helper overlay ────────────────────────────────────────
            if (overlay_boolean.value) {
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

                    // ---> 4. USE LOCAL STATE IN ASYNCIMAGE <---
                    AsyncImage(
                        model              = ImageRequest.Builder(context)
                            .data(cartoonResId) // Passes the local state variable directly
                            .build(),
                        imageLoader        = imageLoader,
                        contentDescription = "Dynamic Guidance Character Helper",
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
// Word circle — selectable, pink selected state
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WordCircleL14(text: String, isSelected: Boolean, onTap: () -> Unit) {
    Column(
        modifier = Modifier
            .shadow(8.dp)
            .size(72.dp)
            .background(
                color = if (isSelected) Color(0xFFE8F5E9) else Color.White,
                shape = RoundedCornerShape(72.dp)
            )
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) Color(0xFFF8335D) else Color.Transparent,
                shape = RoundedCornerShape(72.dp)
            )
            .clickable { onTap() },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text  = text,
            style = TextStyle(
                fontSize   = 18.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight(400),
                textAlign  = TextAlign.Center,
                color      = if (isSelected) Color(0xFFF8335D) else Color(0xFF000278)
            )
        )
    }
}