package org.example.frontend.therapy.level1

import android.media.AudioAttributes
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.launch
import okhttp3.*
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import java.io.IOException
import java.util.Locale

@Composable
fun QuestionL3_Shell(
    sessionItem: SessionQuestion, // Injected dynamically from the Router
    uiSequenceNumber: Int,        // Dynamic visual numbering (1, 2, 3...)
    onNext: () -> Unit            // Advances the router array
) {
    val ip = NetworkConfig.SERVER_IP
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val overlayBoolean = remember { mutableStateOf(false) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    // GIPHY HANDLER
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

    // --- 1. DYNAMIC ASSET ROTATION ---
    // Reads whatever the real target error is and rotates the single down2 graphic accordingly
    val targetRotationDegrees = remember(sessionItem.targetWord) {
        when (sessionItem.targetWord.trim().lowercase(Locale.ROOT)) {
            "down" -> 0f
            "left" -> 90f
            "up" -> 180f
            "right" -> 270f
            else -> 0f
        }
    }

    // --- 2. DYNAMIC ANSWER GENERATOR & SHUFFLER ---
    // Takes the real target word, pairs it with an opposite distractor, and shuffles button placement
    val buttonOptions = remember(sessionItem.targetWord) {
        val correctWord = sessionItem.targetWord.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }

        val distractorWord = when (correctWord.lowercase(Locale.ROOT)) {
            "up" -> "Down"
            "down" -> "Up"
            "left" -> "Right"
            "right" -> "Left"
            else -> "Down" // Failsafe
        }

        listOf(correctWord, distractorWord).shuffled()
    }

    // Track button border colors dynamically based on the generated list size
    val buttonColors = remember(buttonOptions) {
        mutableStateListOf<Color>().apply {
            buttonOptions.forEach { _ -> add(Color(0x55FFFFFF)) }
        }
    }

    // Track button background states dynamically
    val buttonBackgrounds = remember(buttonOptions) {
        mutableStateListOf<Color>().apply {
            buttonOptions.forEach { _ -> add(Color(0x33FFFFFF)) }
        }
    }

    // --- 3. VERIFICATION HANDLER ---
    fun verifySelection(selectedDirection: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            onNext()
            return
        }
        isVerifying = true

        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", currentUser.uid)
            .addFormDataPart("question_number", sessionItem.dbQuestionNumber.toString())
            .addFormDataPart("target_word", sessionItem.targetWord)
            .addFormDataPart("arrow_selected", selectedDirection)
            .build()

        val request = Request.Builder()
            .url("http://$ip/verify_therapy_q3")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FlaskAPI_L3", "Verify failed", e)
                Handler(Looper.getMainLooper()).post {
                    isVerifying = false
                    onNext()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string() ?: "No response"
                Log.d("FlaskAPI_L3", "Response: $result")
                Handler(Looper.getMainLooper()).post {
                    isVerifying = false
                    onNext()
                }
            }
        })
    }

    // --- DYNAMIC AUDIO PLAYBACK ---
    LaunchedEffect(overlayBoolean.value) {
        if (overlayBoolean.value && sessionItem.audioUrl != null) {
            isAudioPlaying = true
            try {
                MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(sessionItem.audioUrl)
                    prepareAsync()
                    setOnPreparedListener { start() }
                    setOnCompletionListener {
                        release()
                        isAudioPlaying = false
                        overlayBoolean.value = false
                    }
                    setOnErrorListener { _, _, _ ->
                        release()
                        isAudioPlaying = false
                        overlayBoolean.value = false
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e("Audio_L3", "Playback error", e)
                isAudioPlaying = false
                overlayBoolean.value = false
            }
        } else if (overlayBoolean.value) {
            delay(3000)
            overlayBoolean.value = false
        }
    }

    // --- UI LAYOUT ---
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // ---> THEME BACKGROUND: Playful Pastel Flat-Lay <---
        Image(
            painter = painterResource(R.drawable.level1_q3),
            contentDescription = "Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // ==========================================
        // UNIFORM GLASSMORPHIC CARD (40% MILKY BASE)
        // ==========================================
        Box(
            modifier = Modifier
                .width(330.dp)
                .height(535.dp)
                .shadow(
                    elevation = 25.dp,
                    shape = RoundedCornerShape(38.dp),
                    ambientColor = Color(0x40FFFFFF),
                    spotColor = Color(0x55FF99CC)
                )
                // OUTER GLASS GLOW
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66FFFFFF),
                            Color(0x44FFFFFF),
                            Color(0x22FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                // GLASS BORDER
                .border(
                    width = 1.8.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xAAFFFFFF),
                            Color(0x55FFB6D9),
                            Color(0x44FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                // ---> TRANSLUCENT MILKY EFFECT: Perfectly matched 40% opacity base (0x66FFFFFF) <---
                .background(
                    color = Color(0xCCFFFFFF),
                    shape = RoundedCornerShape(38.dp)
                )
                .blur(0.3.dp)
                .align(Alignment.Center)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ==========================
                // HEADER
                // ==========================
                Text(
                    text = "Question $uiSequenceNumber",
                    style = TextStyle(
                        fontSize = 34.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF8FC4), // Light pastel pink
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(top = 20.dp)
                )

                // ==========================
                // QUESTION TEXT AREA
                // ==========================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sessionItem.instructionText,
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF9ECF),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.weight(1f).padding(vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // ===========================================
                    // AUDIO BUTTON (UNIFORM LEVEL 1 SPEAKER ASSET)
                    // ===========================================
                    IconButton(
                        onClick = { overlayBoolean.value = true },
                        enabled = !isAudioPlaying && !isVerifying,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.level1_speaker),
                            contentDescription = "Speaker"
                        )
                    }
                }

                // ==========================
                // DYNAMIC ROTATED ARROW
                // ==========================
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF8FC4),
                            modifier = Modifier.size(45.dp)
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.down2),
                            contentDescription = "Target Graphic",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = targetRotationDegrees
                                }
                        )
                    }
                }

                // ==========================
                // DYNAMIC BUTTON STACK
                // ==========================
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    buttonOptions.forEachIndexed { index, optionText ->
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(25.dp),
                                    ambientColor = Color(0x20000000),
                                    spotColor = Color(0x40FF99CC)
                                )
                                .width(180.dp)
                                .height(55.dp)
                                .background(
                                    color = buttonBackgrounds[index],
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .border(
                                    width = 2.5.dp,
                                    color = buttonColors[index],
                                    shape = RoundedCornerShape(25.dp)
                                )
                                .clipToBounds()
                                .clickable {
                                    if (!isVerifying) {
                                        // ---> THEMATIC HIGHLIGHT: Highlight buttons using energetic green #33CC66 to confirm choice <---
                                        buttonColors[index] = Color(0xFF33CC66)
                                        buttonBackgrounds[index] = Color(0x66FFFFFF)
                                        scope.launch {
                                            delay(500)
                                            verifySelection(optionText)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = optionText,
                                style = TextStyle(
                                    fontSize = 26.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF8FC4), // Matching font color
                                    textAlign = TextAlign.Center,
                                )
                            )
                        }
                    }
                }

                // ==========================
                // SKIP BUTTON
                // ==========================
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .background(
                            color = Color(0x33FFFFFF),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0x55FFFFFF),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .clickable { if (!isVerifying) onNext() }
                        .padding(horizontal = 30.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Skip",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFF9ECF)
                        )
                    )
                }
            }
        } // END OF MAIN SHELL

        // ==================================================
        // UNIFORM CHARACTER OVERLAY (LEVEL 1 SPEECH BUBBLE)
        // ==================================================
        if (overlayBoolean.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0x4FFFFFFF))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = (-120).dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.level1_speechbubble),
                        contentDescription = "Instruction prompt bubble",
                    )
                    Text(
                        text = sessionItem.instructionText,
                        modifier = Modifier.padding(horizontal = 28.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            color = Color(0xFF7A3E66),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    )
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(R.drawable.doraemon)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = "Mascot helper guidance",
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