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
import org.example.frontend.R

private val BlueText = Color(0xFF000278)
private val TranslucentWhite = Color(0xC7FFFFFF)
private val SelectionOrange = Color(0xFFFFA500)

data class CartoonChar(
    val id: Int,
    val name: String,
    val imageRes: Int
)

@Composable
fun CartoonSelectionScreen() {
    val context = LocalContext.current

    // --- STATE VARIABLES ---
    val speaker_boolean = remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("Select your favorite Cartoon Character") }
    var selectedCharacterId by remember { mutableStateOf<Int?>(null) }

    // --- IMAGE LOADER (Required for Doraemon GIF) ---
    // This builder enables GIF support in Coil
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

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {

            // LAYER 1: Background
            Image(
                painter = painterResource(id = R.drawable.cartoonselectionbkg),
                contentDescription = "Background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f)
            )

            // LAYER 2: Main Content
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .zIndex(1f),
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
                    Spacer(modifier = Modifier.height(20.dp))

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

                        // Sound Button
                        IconButton(
                            onClick = {
                                popupMessage = "Select your favorite Cartoon Character"
                                speaker_boolean.value = true // This triggers the new popup
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Character Grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                            CharacterItem(characters[0], selectedCharacterId) { selectedCharacterId = it }
                            CharacterItem(characters[1], selectedCharacterId) { selectedCharacterId = it }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                            CharacterItem(characters[2], selectedCharacterId) { selectedCharacterId = it }
                            CharacterItem(characters[3], selectedCharacterId) { selectedCharacterId = it }
                        }
                    }
                }
            }

            // LAYER 3: NEW POPUP LOGIC
            // This replaces the old manual Box logic
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
    onSelect: (Int) -> Unit
) {
    val isSelected = char.id == selectedId

    Box(
        modifier = Modifier
            .width(115.dp)
            .height(140.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onSelect(char.id) }
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

// --- DORAEMON POPUP COMPONENT ---
// If you have this in a separate file, you can remove it from here and import it instead.
@Composable
fun DoraemonPopup(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    message: String,
    imageLoader: ImageLoader
) {
    val context = LocalContext.current

    // Handle Audio and Timer Logic inside the component
    LaunchedEffect(isVisible) {
        if (isVisible) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel1q4)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            // Wait 5 seconds then close
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
                // --- SPEECH BUBBLE (Center Right) ---
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = -120.dp)
                        .padding(end = 10.dp) // Add slight padding from screen edge
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
                            fontSize = 22.sp, // Reduced from 25.sp to fit better
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF000278),
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp // Adds breathing room between lines
                        ),
                        modifier = Modifier
                            .width(200.dp) // IMPORTANT: Forces text to wrap inside the bubble
                            .padding(bottom = 30.dp) // Moves text up slightly to avoid the bubble "tail"
                    )
                }

                // --- DORAEMON GIF (Bottom Left) ---
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