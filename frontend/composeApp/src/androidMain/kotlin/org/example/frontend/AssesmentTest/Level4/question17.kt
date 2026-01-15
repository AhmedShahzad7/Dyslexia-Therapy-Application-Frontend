package org.example.frontend.AssesmentTest.Level4


import android.media.MediaPlayer
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import org.example.frontend.R
import coil.ImageLoader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Question17(){
    val context = LocalContext.current
    val overlay_boolean= remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }

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

    fun Clicked_Speaker(){
        overlay_boolean.value = true
        speaker_boolean.value = true
    }
    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.rewrite_sentence)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(5000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }
    val sentences = listOf(
        "was it on or no",
        "The big dog",
        "On mat sat a cat"
    )
val currentindex=remember { mutableStateOf(0) }

    val currentSentenceString = sentences[currentindex.value]
    val lettersOnly = currentSentenceString.filter { it.isLetter() }


    val density = LocalDensity.current
    val targetPixels = 64
    val boxSizeDp =64.dp
    val boxSizePx = with(density) { targetPixels.dp.toPx().toInt() }

    Box(
        modifier=Modifier.fillMaxSize(),
    )
    {
        Image(
            painter = painterResource(id = R.drawable.assessment_level1q2),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )


        Box(
            modifier=Modifier
                .width(370.dp)
                .height(570.dp)
                .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                .align(Alignment.Center)

        )
        {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier=Modifier
                        .fillMaxWidth().padding(top = 32.dp)
                        .height(62.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically

                )
                {
                    Text(
                        text = "Question no 17",
                        Modifier
                            .width(245.dp)
                            .height(62.dp),
                        style = TextStyle(
                            fontSize = 34.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xF527B51A),
                            textAlign = TextAlign.Center,

                            )
                    )
                }
                Row(
                    modifier=Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(color = Color(0x00FFFFFF))
                        .padding(start = 20.dp)

                )
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    )
                    {



                        Text(
                            text = "Rewrite the sentence shown \n below",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF27B51A),
                                textAlign = TextAlign.Center,
                            )
                        )
                        Box(
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            IconButton(
                                onClick = {
                                    Clicked_Speaker()
                                }
                            ) {
                                Image(
                                    modifier = Modifier
                                        .width(35.dp)
                                        .height(35.dp),
                                    painter = painterResource(id = R.drawable.sound_button),
                                    contentDescription = "selected checkmark",
                                    contentScale = ContentScale.None
                                )
                            }
                        }
                    }
                }
                Box(
                    Modifier
                        .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                        .width(350.dp)
                        .height(550.dp)
                        .background(color = Color(0xE5FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                        .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                )
                {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        Text(
                            text = sentences[currentindex.value],
                            style = TextStyle(
                                fontSize = 30.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xF527B51A),
                                textAlign = TextAlign.Center,
                            )

                        )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                                maxItemsInEachRow = 4 // Optional: limit boxes per row
                            ) {
                                // We iterate over the letters.
                                // We use 'key' to force the boxes to clear/redraw when the sentence changes (currentindex changes)
                                lettersOnly.forEachIndexed { index, char ->
                                    key("${currentindex.value}_$index") {
                                        DrawingBox(
                                            modifier = Modifier
                                                .padding(4.dp) // Spacing between boxes
                                                .size(50.dp) // Adjusted size to fit more boxes
                                        )
                                    }
                                }
                            }


                        Box(
                            Modifier
                                .padding(bottom = 16.dp)
                                .width(150.dp)
                                .height(50.dp)
                                .background(color = Color(0xF527B51A), shape = RoundedCornerShape(size = 35.dp))
                                .clickable {
                                    if (currentindex.value<sentences.lastIndex)
                                    {
                                        currentindex.value++
                                    }

                                },
                            contentAlignment = Alignment.Center



                        )
                        {
                            Text(
                                text = "Submit",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFFFFFFF),

                                    ),


                                )
                        }
                    }
                }
            }

        } //Ending Original Screen

        //Character reading question
        if(overlay_boolean.value) {

            Box(
                modifier = Modifier
                    .offset(x = 0.dp, y = 0.dp)
                    .width(430.dp)
                    .height(932.dp)
                    .background(color = Color(0x4FFFFFFF))
                    .fillMaxSize()

            ) {
                //Speech Bubble Location

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
                    ) {
                        Image(
                            painter = painterResource(R.drawable.speech_bubble),
                            contentDescription = "",
                        )
                        Text(
                            text = "Rewrite the sentence shown \n below",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF27B51A),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                    // --- DORAEMON (Bottom Left) ---

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(R.drawable.doraemon2)
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
}

@Composable
fun DrawingBox(modifier: Modifier = Modifier)
{
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    val boxSizeDp =64.dp
    Column(
        modifier = Modifier.width(boxSizeDp )
            .wrapContentHeight()
            .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {


        Box(
            modifier = Modifier
                .size(boxSizeDp) // Use the variable
                .background(color = Color.White)
                .clipToBounds()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
//                                                waterSound.start()
                            val newPath = Path().apply { moveTo(offset.x, offset.y) }
                            currentPath = newPath
                        },
                        onDrag = { change, _ ->
                            currentPath?.lineTo(change.position.x, change.position.y)
                            // Trigger recomposition (hacky but works for Path updates)
                            currentPath = Path().apply {
                                currentPath?.let { addPath(it) }
                            }
                        },
                        onDragEnd = {
//                                                waterSound.stop()
                            currentPath?.let { paths.add(it) }
                            currentPath = null
                        },
                        onDragCancel = {
//                                                waterSound.stop()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
//

                paths.forEach { path ->
                    drawPath(path = path, color = Color.Black, style = Stroke(8f))
                }
                currentPath?.let {
                    drawPath(path = it, color = Color.Black, style = Stroke(8f))
                }
            }
        }
    }
}