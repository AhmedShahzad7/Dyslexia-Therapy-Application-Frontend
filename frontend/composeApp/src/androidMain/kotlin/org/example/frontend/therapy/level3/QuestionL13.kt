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
// QUESTION L13  —  "Circle the words that rhyme the same"
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun QuestionL13(onNextScreen: () -> Unit) {
    val CURRENT_QUESTION_NUMBER = 13
    val ip = NetworkConfig.SERVER_IP
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var questionText by remember { mutableStateOf("Circle the words that\n rhyme same") }
    var dynamicAudioUrl by remember { mutableStateOf<String?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    val overlay_boolean = remember { mutableStateOf(false) }
    val tempStore = remember { mutableStateListOf<String>() }
    var gridWords by remember { mutableStateOf<List<String>>(emptyList()) }

    var targetWord by remember { mutableStateOf("") }

    val currentUser = FirebaseAuth.getInstance().currentUser

    fun setupDefaults() {
        if (gridWords.isEmpty()) {
            gridWords = listOf("lap", "cap", "bun", "map", "tub", "bat", "nut", "nap", "man", "bag", "tap", "bed")
            targetWord = "lap"
        }
    }

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
                                targetWord = json.optString("target_word", "")
                                val dataArray = json.optJSONArray("data")
                                if (dataArray != null && dataArray.length() >= 12) {
                                    val newGrid = mutableListOf<String>()
                                    for (i in 0 until 12) newGrid.add(dataArray.getString(i))
                                    gridWords = newGrid
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

    fun toggleSelection(word: String) {
        if (tempStore.contains(word)) tempStore.remove(word) else tempStore.add(word)
    }

    fun submitAndNavigate() {
        currentUser?.uid?.let { userId ->
            if (tempStore.isEmpty()) {
                submitTherapyAnswer(userId, CURRENT_QUESTION_NUMBER, targetWord, true) { onNextScreen() }
                return@let
            }
            var remaining = tempStore.size
            tempStore.toList().forEach { word ->
                val isCorrect = word.takeLast(2) == targetWord.takeLast(2)
                submitTherapyAnswer(userId, CURRENT_QUESTION_NUMBER, targetWord, isCorrect) {
                    remaining--
                    if (remaining == 0) onNextScreen()
                }
            }
        } ?: onNextScreen()
    }

    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value && !dynamicAudioUrl.isNullOrEmpty()) {
            isAudioPlaying = true
            try {
                val mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).setUsage(AudioAttributes.USAGE_MEDIA).build())
                    setDataSource(dynamicAudioUrl)
                    setOnPreparedListener { mp -> mp.start() }
                    setOnCompletionListener { mp -> mp.release(); isAudioPlaying = false; overlay_boolean.value = false }
                }
                mediaPlayer.prepareAsync()
            } catch (e: Exception) { isAudioPlaying = false; overlay_boolean.value = false }
        } else if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel3q13)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { it.release() }
            delay(3000)
            overlay_boolean.value = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(R.drawable.therapy_level3), contentDescription = "", contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFF8335D))
        } else {
            Box(modifier = Modifier.width(299.dp).height(550.dp).background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(35.dp)).align(Alignment.Center)) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Question no $CURRENT_QUESTION_NUMBER", style = TextStyle(fontSize = 34.sp, fontFamily = FontFamily(Font(R.font.windsol)), fontWeight = FontWeight(400), color = Color(0xFFF8335D), textAlign = TextAlign.Center))
                    }
                    Row(modifier = Modifier.fillMaxWidth().height(100.dp).background(color = Color.Transparent).padding(start = 5.dp)) {
                        Text(text = questionText, style = TextStyle(fontSize = 25.sp, fontFamily = FontFamily(Font(R.font.windsol)), fontWeight = FontWeight(400), color = Color(0xFFF8335D), textAlign = TextAlign.Center))
                        Box(modifier = Modifier.offset(x = 10.dp)) {
                            IconButton(onClick = { overlay_boolean.value = true }, enabled = !isAudioPlaying) {
                                Image(modifier = Modifier.size(35.dp), painter = painterResource(id = R.drawable.sound_button1), contentDescription = "Speaker")
                            }
                        }
                    }
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(15.dp)) {
                        for (row in 0 until 4) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(space = 50.dp, alignment = Alignment.CenterHorizontally)) {
                                for (col in 0 until 3) {
                                    val index = row * 3 + col
                                    if (index < gridWords.size) {
                                        val word = gridWords[index]
                                        OptionCircleQ13(text = word, isSelected = tempStore.contains(word)) { toggleSelection(it) }
                                    }
                                }
                            }
                        }
                    }
                }
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 10.dp, bottom = 10.dp).background(Color(0xFFF8335D), RoundedCornerShape(15.dp)).clickable { submitAndNavigate() }.padding(horizontal = 20.dp, vertical = 5.dp)) {
                    Text(text = "Next", style = TextStyle(fontSize = 26.sp, fontFamily = FontFamily(Font(R.font.windsol)), fontWeight = FontWeight.Bold, color = Color.White))
                }
            }
            if (overlay_boolean.value) {
                Box(modifier = Modifier.fillMaxSize().background(color = Color(0x4FFFFFFF))) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)) {
                        Image(painter = painterResource(R.drawable.speech_bubble3), contentDescription = "")
                        Text(text = questionText, style = TextStyle(fontSize = 25.sp, fontFamily = FontFamily(Font(R.font.windsol)), fontWeight = FontWeight(400), color = Color(0xFFF8335D), textAlign = TextAlign.Center))
                    }
                    AsyncImage(model = ImageRequest.Builder(context).data(R.drawable.doraemon).build(), imageLoader = imageLoader, contentDescription = "Doraemon", contentScale = ContentScale.FillBounds, modifier = Modifier.size(327.dp).offset(y = (-120).dp).align(Alignment.BottomStart))
                }
            }
        }
    }
}

@Composable
private fun OptionCircleQ13(text: String, isSelected: Boolean, onOptionClick: (String) -> Unit) {
    Column(modifier = Modifier.shadow(elevation = 25.dp).size(50.dp).background(color = Color.White, shape = RoundedCornerShape(75.dp)).border(width = if (isSelected) 3.dp else 0.dp, color = if (isSelected) Color(0xFFF8335D) else Color.Transparent, shape = RoundedCornerShape(75.dp)).padding(top = 5.dp).clickable { onOptionClick(text) }, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(modifier = Modifier.height(20.dp), text = text, style = TextStyle(fontSize = 20.sp, fontFamily = FontFamily(Font(R.font.windsol)), fontWeight = FontWeight(400), textAlign = TextAlign.Center, color = Color(0xFF000278)))
    }
}