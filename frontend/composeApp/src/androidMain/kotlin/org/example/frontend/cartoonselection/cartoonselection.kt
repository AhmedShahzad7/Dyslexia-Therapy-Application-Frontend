package org.example.frontend.cartoonselection

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.frontend.R
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import okhttp3.*
import java.io.IOException
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

// Define Colors
private val BlueText = Color(0xFF000278)
private val TranslucentWhite = Color(0xC7FFFFFF)
private val SelectionOrange = Color(0xFFFFA500)
private val GreenText = Color(0xFF27B51A)

data class CartoonChar(
    val id: Int,
    val name: String,
    val imageRes: Int
)

@Composable
fun CartoonSelectionScreen(onNextScreen: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- STATE VARIABLES ---
    val speaker_boolean = remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("Select your favorite Cartoon Character") }

    // Tracks which character is selected
    var selectedCharacterId by remember { mutableStateOf<Int?>(null) }

    // Lock logic
    var isSelectionLocked by remember { mutableStateOf(false) }

    // --- NETWORK CONFIG ---
    val ip_address = "http:// 192.168.10.4:5000" // Emulator IP (Change to your PC IP for real device)

    fun sendToFlask(cartoonName: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: "TEST_USER_123"

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId)
            .addFormDataPart("cartoon_name", cartoonName)
            .build()

        val request = Request.Builder()
            .url("$ip_address/select_cartoon")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FlaskAPI", "Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("FlaskAPI", "Response: ${response.body?.string()}")
            }
        })
    }

    // --- IMAGE LOADER ---
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val characters = listOf(
        CartoonChar(1, "Mickey", R.drawable.mickey),
        CartoonChar(2, "Daffy", R.drawable.daffy),
        CartoonChar(3, "Tom", R.drawable.tom),
        CartoonChar(4, "Pooh", R.drawable.pooh)
    )

    // --- HANDLE SELECTION ---
    fun handleSelection(id: Int, name: String) {
        if (isSelectionLocked) return

        selectedCharacterId = id
        isSelectionLocked = true
        sendToFlask(name)
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {

            // Background
            Image(
                painter = painterResource(id = R.drawable.cartoonselectionbkg),
                contentDescription = "Background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize().zIndex(0f)
            )

            // Main Content
            Column(
                modifier = Modifier.align(Alignment.Center).zIndex(1f),
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
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)
                ) {
                    // Spacer reduced to 10dp to save space
                    Spacer(modifier = Modifier.height(10.dp))

                    // Instructions Row
                    Row(
                        modifier = Modifier.width(280.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "Select your favorite\nCartoon Character",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = BlueText,
                                textAlign = TextAlign.Center
                            ),
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        IconButton(
                            onClick = {
                                popupMessage = "Select your favorite Cartoon Character"
                                speaker_boolean.value = true
                            },
                            modifier = Modifier.size(45.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color = BlueText, shape = CircleShape)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.soundbutton_am1),
                                    contentDescription = "sound",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    // Spacer reduced to 10dp to save space
                    Spacer(modifier = Modifier.height(10.dp))

                    // Character Grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                            CharacterItem(characters[0], selectedCharacterId) { handleSelection(characters[0].id, characters[0].name) }
                            CharacterItem(characters[1], selectedCharacterId) { handleSelection(characters[1].id, characters[1].name) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                            CharacterItem(characters[2], selectedCharacterId) { handleSelection(characters[2].id, characters[2].name) }
                            CharacterItem(characters[3], selectedCharacterId) { handleSelection(characters[3].id, characters[3].name) }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // --- NEXT BUTTON (Updated Design) ---
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd) // Position at Bottom Right
                                .padding(end = 10.dp, bottom = 10.dp) // Add spacing from the edges
                                .background(
                                    // Green if selected, Gray if not
                                    color = if (selectedCharacterId != null) Color(0xFFFFA500) else Color.Gray,
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .clickable {
                                    if (selectedCharacterId != null) {
                                        onNextScreen()
                                    } else {
                                        Toast.makeText(context, "Please select a character first!", Toast.LENGTH_SHORT).show()
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

            // Popup
            DoraemonPopup(
                isVisible = speaker_boolean.value,
                onDismiss = { speaker_boolean.value = false },
                message = popupMessage,
                imageLoader = imageLoader
            )
        }
    }
}

@Composable
private fun CharacterItem(
    char: CartoonChar,
    selectedId: Int?,
    onSelect: () -> Unit
) {
    val isSelected = char.id == selectedId

    Box(
        modifier = Modifier
            .width(115.dp)
            .height(125.dp) // Reduced height from 140 to 125 to prevent overflow
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSelect() }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = if (isSelected) 4.dp else 0.dp,
                    color = if (isSelected) SelectionOrange else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                )
                .background(color = Color(0x00FFFFFF), RoundedCornerShape(10.dp))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = char.imageRes),
                contentDescription = char.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(SelectionOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun DoraemonPopup(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    message: String,
    imageLoader: ImageLoader
) {
    val context = LocalContext.current

    LaunchedEffect(isVisible) {
        if (isVisible) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel1q4)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(5000)
            onDismiss()
        }
    }

    if (isVisible) {
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = 0.dp)
                .width(430.dp)
                .height(932.dp)
                .background(color = Color(0x4FFFFFFF))
                .fillMaxSize()
                .zIndex(10f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onDismiss()
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0x4FFFFFFF))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = -120.dp)
                        .padding(end = 10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.speech_bubble1),
                        contentDescription = "Speech Bubble",
                        modifier = Modifier.size(width = 300.dp, height = 250.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = message,
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF000278),
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        ),
                        modifier = Modifier
                            .width(200.dp)
                            .padding(bottom = 30.dp)
                    )
                }

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