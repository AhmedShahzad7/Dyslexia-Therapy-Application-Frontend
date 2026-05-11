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
import androidx.compose.ui.draw.clipToBounds // ---> CRITICAL FIX: Explicitly imported required drawing extension <---
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.launch
import okhttp3.*
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import java.io.IOException

@Composable
fun QuestionL2_Shell(
    sessionItem: SessionQuestion, // DIRECT INJECTION: Received from the dynamic Router
    uiSequenceNumber: Int,        // Dynamic visual numbering (1, 2, 3...)
    onNext: () -> Unit            // Instructs the router to advance the array progression
) {
    val ip = NetworkConfig.SERVER_IP
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val overlayBoolean = remember { mutableStateOf(false) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    // Grid Options & Tracking Selection
    val randomizedDirections = remember(sessionItem) {
        listOf("Up", "Down", "Left", "Right", "NE", "NW", "SE", "SW").shuffled()
    }
    var selectedIndex by remember { mutableStateOf<Int?>(-1) }

    // Maps string direction directly to drawable resource
    fun getDrawable(dir: String): Int {
        return when (dir.uppercase()) {
            "UP" -> R.drawable.up
            "DOWN" -> R.drawable.down
            "LEFT" -> R.drawable.left
            "RIGHT" -> R.drawable.right
            "NE" -> R.drawable.northeast_arrow
            "NW" -> R.drawable.nothwest_arrow
            "SE" -> R.drawable.southeast_arrow
            "SW" -> R.drawable.southwest_arrow
            else -> R.drawable.left
        }
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) { add(ImageDecoderDecoder.Factory()) }
                else { add(GifDecoder.Factory()) }
            }.build()
    }

    // --- SEND SELECTION TO BACKEND ---
    fun verifySelection(selectedArrow: String, dbSlot: Int, target: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            onNext()
            return
        }
        isVerifying = true

        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", currentUser.uid)
            .addFormDataPart("question_number", dbSlot.toString()) // Securely anchors updates to the exact DB slot
            .addFormDataPart("target_word", target)
            .addFormDataPart("arrow_selected", selectedArrow)
            .build()

        val request = Request.Builder()
            .url("http://$ip/verify_therapy_mcq")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FlaskAPI", "Verify failed", e)
                Handler(Looper.getMainLooper()).post {
                    isVerifying = false
                    onNext()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Handler(Looper.getMainLooper()).post {
                    isVerifying = false
                    onNext() // Triggers the Router to mount the next question in the sequence
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
                Log.e("Audio", "Playback error", e)
                isAudioPlaying = false
                overlayBoolean.value = false
            }
        } else if (overlayBoolean.value) {
            delay(3000)
            overlayBoolean.value = false
        }
    }

    // --- UI LAYOUT ---
    Box(modifier = Modifier.fillMaxSize()) {
        // ---> UNIFORM LEVEL 1 THEME: Mapped background asset to the specified level1_q2 composition <---
        Image(
            painter = painterResource(R.drawable.level1_q2),
            contentDescription = "Thematic Playful Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // ==========================================
        // UNIFORM GLASSMORPHIC CARD (LEVEL 1 THEME)
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
                            Color(0x66FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(38.dp)
                )
                // TRANSLUCENT GLASS EFFECT
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
                horizontalAlignment = Alignment.CenterHorizontally
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
                        color = Color(0xFFFF8FC4), // Uniform pastel pink
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
                        enabled = !isAudioPlaying,
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
                // DYNAMIC ARROWS GRID AREA
                // ==========================
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF8FC4), // Matched indicator color to the header text theme
                            modifier = Modifier.size(45.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            randomizedDirections.chunked(3).forEachIndexed { rowIndex, rowItems ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowItems.forEachIndexed { colIndex, direction ->
                                        val itemIndex = rowIndex * 3 + colIndex
                                        val isSelected = selectedIndex == itemIndex

                                        // ---> THEMATIC HIGHLIGHT: Highlight selections using the energetic green #33CC66 to preserve global aesthetic consistency <---
                                        val borderColor = if (isSelected) Color(0xFF33CC66) else Color(0x55FFFFFF)
                                        val cardBackground = if (isSelected) Color(0x66FFFFFF) else Color(0x33FFFFFF)

                                        Box(
                                            modifier = Modifier
                                                .shadow(
                                                    elevation = 8.dp,
                                                    shape = RoundedCornerShape(22.dp),
                                                    ambientColor = Color(0x20000000),
                                                    spotColor = Color(0x40FF99CC)
                                                )
                                                .width(75.dp)
                                                .height(75.dp)
                                                .background(
                                                    color = cardBackground,
                                                    shape = RoundedCornerShape(22.dp)
                                                )
                                                .border(
                                                    width = 2.5.dp,
                                                    color = borderColor,
                                                    shape = RoundedCornerShape(22.dp)
                                                )
                                                .clipToBounds() // Guaranteed layout stability via added header declaration
                                                .clickable {
                                                    if (selectedIndex == -1 && !isVerifying) {
                                                        selectedIndex = itemIndex
                                                        scope.launch {
                                                            delay(500)
                                                            verifySelection(
                                                                selectedArrow = direction,
                                                                dbSlot = sessionItem.dbQuestionNumber,
                                                                target = sessionItem.targetWord
                                                            )
                                                        }
                                                    }
                                                }
                                                .padding(14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = getDrawable(direction)),
                                                contentDescription = direction,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

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
                    modifier = Modifier.align(Alignment.CenterEnd).offset(y = (-120).dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.level1_speechbubble),
                        contentDescription = "Speech Bubble"
                    )
                    Text(
                        text = sessionItem.instructionText,
                        modifier = Modifier.padding(horizontal = 28.dp),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            color = Color(0xFF7A3E66), // Uniform high-contrast overlay text color
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    )
                }
                AsyncImage(
                    model = ImageRequest.Builder(context).data(R.drawable.doraemon2).build(),
                    imageLoader = imageLoader,
                    contentDescription = "Character Overlay GIF",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(327.dp).offset(y = (-120).dp).align(Alignment.BottomStart)
                )
            }
        }
    }
}