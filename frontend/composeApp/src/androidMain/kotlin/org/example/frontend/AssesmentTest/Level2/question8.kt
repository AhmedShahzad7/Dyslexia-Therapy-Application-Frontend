package org.example.frontend.AssesmentTest.Level2


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
import lettersViewModelSmall
import org.example.frontend.AssesmentTest.Level4.DrawingBox


@Composable
fun Question8(
    viewModelSmall: lettersViewModelSmall=androidx.lifecycle.viewmodel.compose.viewModel (),
){
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
            val mediaPlayer = MediaPlayer.create(context, R.raw.trace_finger)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(5000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }
    val paths1 = remember { mutableStateListOf<Path>() }
    var currentPath1 by remember { mutableStateOf<Path?>(null) }

// States for Canvas 2
    val paths2 = remember { mutableStateListOf<Path>() }
    var currentPath2 by remember { mutableStateOf<Path?>(null) }

// States for Canvas 3
    val paths3 = remember { mutableStateListOf<Path>() }
    var currentPath3 by remember { mutableStateOf<Path?>(null) }

// States for Canvas 4
    val paths4 = remember { mutableStateListOf<Path>() }
    var currentPath4 by remember { mutableStateOf<Path?>(null) }

    val boxSizeDp =64.dp

    Box(
        modifier=Modifier.fillMaxSize(),
    )
    {
        Image(
            painter = painterResource(id = R.drawable.assessment_level1q3),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )


        Box(
            modifier=Modifier
                .width(299.dp)
                .height(497.dp)
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
                        text = "Question no 8",
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
                            text = "Trace your fingers\n on the Letters",
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
                        .width(250.dp)
                        .height(450.dp)
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
                                                    currentPath1 = newPath
                                                },
                                                onDrag = { change, _ ->
                                                    currentPath1?.lineTo(change.position.x, change.position.y)
                                                    // Trigger recomposition (hacky but works for Path updates)
                                                    currentPath1 = Path().apply {
                                                        currentPath1?.let { addPath(it) }
                                                    }
                                                },
                                                onDragEnd = {
//                                                waterSound.stop()
                                                    currentPath1?.let { paths1.add(it) }
                                                    currentPath1 = null
                                                },
                                                onDragCancel = {
//                                                waterSound.stop()
                                                }
                                            )
                                        }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        with(viewModelSmall) {
                                            drawDottedLettern()
                                        }
//

                                        paths1.forEach { path ->
                                            drawPath(path = path, color = Color.Black, style = Stroke(8f))
                                        }
                                        currentPath1?.let {
                                            drawPath(path = it, color = Color.Black, style = Stroke(8f))
                                        }
                                    }

                                }
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
                                                    currentPath2 = newPath
                                                },
                                                onDrag = { change, _ ->
                                                    currentPath2?.lineTo(change.position.x, change.position.y)
                                                    // Trigger recomposition (hacky but works for Path updates)
                                                    currentPath2 = Path().apply {
                                                        currentPath2?.let { addPath(it) }
                                                    }
                                                },
                                                onDragEnd = {
//                                                waterSound.stop()
                                                    currentPath2?.let { paths2.add(it) }
                                                    currentPath2 = null
                                                },
                                                onDragCancel = {
//                                                waterSound.stop()
                                                }
                                            )
                                        }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        with(viewModelSmall) {
                                            drawDottedLettern()
                                        }
//

                                        paths2.forEach { path ->
                                            drawPath(path = path, color = Color.Black, style = Stroke(8f))
                                        }
                                        currentPath2?.let {
                                            drawPath(path = it, color = Color.Black, style = Stroke(8f))
                                        }
                                    }

                                }
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
                                                    currentPath3 = newPath
                                                },
                                                onDrag = { change, _ ->
                                                    currentPath3?.lineTo(change.position.x, change.position.y)
                                                    // Trigger recomposition (hacky but works for Path updates)
                                                    currentPath3 = Path().apply {
                                                        currentPath3?.let { addPath(it) }
                                                    }
                                                },
                                                onDragEnd = {
//                                                waterSound.stop()
                                                    currentPath3?.let { paths3.add(it) }
                                                    currentPath3 = null
                                                },
                                                onDragCancel = {
//                                                waterSound.stop()
                                                }
                                            )
                                        }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        with(viewModelSmall) {
                                            drawDottedLettern()
                                        }
//

                                        paths3.forEach { path ->
                                            drawPath(path = path, color = Color.Black, style = Stroke(8f))
                                        }
                                        currentPath3?.let {
                                            drawPath(path = it, color = Color.Black, style = Stroke(8f))
                                        }
                                    }

                                }
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
                                                    currentPath4 = newPath
                                                },
                                                onDrag = { change, _ ->
                                                    currentPath4?.lineTo(change.position.x, change.position.y)
                                                    // Trigger recomposition (hacky but works for Path updates)
                                                    currentPath4 = Path().apply {
                                                        currentPath4?.let { addPath(it) }
                                                    }
                                                },
                                                onDragEnd = {
//                                                waterSound.stop()
                                                    currentPath4?.let { paths4.add(it) }
                                                    currentPath4 = null
                                                },
                                                onDragCancel = {
//                                                waterSound.stop()
                                                }
                                            )
                                        }
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        with(viewModelSmall) {
                                            drawDottedLettern()
                                        }
//

                                        paths4.forEach { path ->
                                            drawPath(path = path, color = Color.Black, style = Stroke(8f))
                                        }
                                        currentPath4?.let {
                                            drawPath(path = it, color = Color.Black, style = Stroke(8f))
                                        }
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


                                },
                            contentAlignment = Alignment.Center



                        )
                        {
                            Text(
                                text = "Next",
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
                            text = "Trace your fingers\n on the Letters",
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
}