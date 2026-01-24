package org.example.frontend.AssesmentTest.Level1

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import org.example.frontend.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import coil.decode.GifDecoder
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.compose.ui.draw.shadow
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Question2(onNextScreen: () -> Unit){
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val overlay_boolean= remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }
    var buttonColor1 by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var buttonColor2 by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var buttonColor3 by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var buttonColor4 by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var buttonColor5 by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var buttonColor6 by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var buttonColor7 by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var buttonColor8 by remember { mutableStateOf(Color(0xFFFFFFFF)) }

    //GIPHY HANDLER
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


    //FLASK HANDLE
    val question_number="2"
    val ip_address="http://192.168.0.17:5000"
    fun sendDirectionToFlask(userid:String,arrow_selected:String ,onResult: (String) -> Unit) {
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userid)
            .addFormDataPart("question_number", question_number)
            .addFormDataPart("arrow_selected", arrow_selected)
            .build()

        val request = Request.Builder()
            .url(ip_address+"/predict_direction_mcq")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FlaskAPI", "Error! ${e.message}", e)
                Handler(Looper.getMainLooper()).post {
                    onResult("Error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string() ?: "No response"
                Log.d("FlaskAPI", "Response: $result")
                Handler(Looper.getMainLooper()).post {
                    onResult(result)
                }
            }
        })
    }


    //PRESSING SPEAKER FUNCTION
    fun Clicked_Speaker(){
        overlay_boolean.value = true
        speaker_boolean.value = true
    }
    LaunchedEffect(overlay_boolean.value) {
        if (overlay_boolean.value) {
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel1q2)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(3000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }

    Box(
        modifier=Modifier.fillMaxSize(),
    ){
        Image(
            painter=painterResource(R.drawable.assessment_level1q2),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )


        //Original Screen
        Box(
            modifier=Modifier
                .width(299.dp)
                .height(497.dp)
                .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                .align(Alignment.Center)
        ){
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                //Question Row
                Row(
                    modifier=Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically

                ){
                    Text(
                        text = "Question no 2",
                        style = TextStyle(
                            fontSize = 34.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xF527B51A),
                            textAlign = TextAlign.Center,
                        )
                    )
                }
                //Intruction Row
                Row(
                    modifier=Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(color = Color(0x00FFFFFF))
                        .padding(start = 20.dp, top=20.dp)
                ){
                    Text(
                        text = "Click the Left Arrow",
                        style = TextStyle(
                            fontSize = 25.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF27B51A),
                            textAlign = TextAlign.Center,
                        )
                    )
                    //Speaker Button
                    Box(
                       modifier=Modifier.offset(y=-10.dp)
                    ){
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
                //ARROWS BOX
                Box(
                    modifier=Modifier
                        .fillMaxWidth()
                        .height(308.dp),
                    contentAlignment = Alignment.TopCenter
                ){
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp),

                    ){
                        //ARROW 1
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)

                        ){
                            Box(
                                modifier=Modifier
                                    .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                                    .width(75.dp)
                                    .height(75.dp)
                                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
                                    .border(width = 5.dp, color = buttonColor1,shape = RoundedCornerShape(size = 75.dp))
                                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                                    .clickable{
                                        buttonColor1 = Color(0xFF27B51A)

                                        scope.launch{
                                            delay(500)
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            if (currentUser != null) {
                                                val userId = currentUser.uid
                                                sendDirectionToFlask(userId,"Up") { result ->
                                                    onNextScreen()
                                                }
                                            }
                                        }

                                    }


                            ){
                                Image(
                                    painter=painterResource(R.drawable.up),
                                    contentDescription = "",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
//ARROOW 2
                            Box(
                                modifier=Modifier
                                    .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                                    .width(75.dp)
                                    .height(75.dp)
                                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
                                    .border(width = 5.dp, color = buttonColor2, shape = RoundedCornerShape(size = 75.dp))
                                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                                    .clickable{
                                        buttonColor2 = Color(0xFF27B51A)

                                        scope.launch{
                                            delay(500)
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            if (currentUser != null) {
                                                val userId = currentUser.uid
                                                sendDirectionToFlask(userId,"Down") { result ->
                                                    onNextScreen()
                                                }
                                            }
                                        }

                                    }
                            ){
                                Image(
                                    painter=painterResource(R.drawable.down),
                                    contentDescription = "",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

//ARROW 3
                            Box(
                                modifier=Modifier
                                    .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                                    .width(75.dp)
                                    .height(75.dp)
                                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
                                    .border(width = 5.dp, color = buttonColor3, shape = RoundedCornerShape(size = 75.dp))
                                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                                    .clickable{
                                        buttonColor3 = Color(0xFF27B51A)
                                        scope.launch{
                                            delay(500)
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            if (currentUser != null) {
                                                val userId = currentUser.uid
                                                sendDirectionToFlask(userId,"Left") { result ->
                                                    onNextScreen()
                                                }
                                            }
                                        }

                                    }
                            ){
                                Image(
                                    painter=painterResource(R.drawable.left),
                                    contentDescription = "",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

//ARROW 4
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)

                        ){
                            Box(
                                modifier=Modifier
                                    .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                                    .width(75.dp)
                                    .height(75.dp)
                                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
                                    .border(width = 5.dp, color = buttonColor4, shape = RoundedCornerShape(size = 75.dp))
                                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                                    .clickable{
                                        buttonColor4 = Color(0xFF27B51A)

                                        scope.launch{
                                            delay(500)
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            if (currentUser != null) {
                                                val userId = currentUser.uid
                                                sendDirectionToFlask(userId,"Left") { result ->
                                                    onNextScreen()
                                                }
                                            }
                                        }

                                    }
                            ){
                                Image(
                                    painter=painterResource(R.drawable.left),
                                    contentDescription = "",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

//ARROW 5
                            Box(
                                modifier=Modifier
                                    .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                                    .width(75.dp)
                                    .height(75.dp)
                                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
                                    .border(width = 5.dp, color = buttonColor5, shape = RoundedCornerShape(size = 75.dp))
                                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                                    .clickable{
                                        buttonColor5 = Color(0xFF27B51A)

                                        scope.launch{
                                            delay(500)
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            if (currentUser != null) {
                                                val userId = currentUser.uid
                                                sendDirectionToFlask(userId,"NE") { result ->
                                                    onNextScreen()
                                                }
                                            }
                                        }

                                    }
                            ){
                                Image(
                                    painter=painterResource(R.drawable.northeast_arrow),
                                    contentDescription = "",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

//ARROW 6
                            Box(
                                modifier=Modifier
                                    .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                                    .width(75.dp)
                                    .height(75.dp)
                                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
                                    .border(width = 5.dp, color = buttonColor6, shape = RoundedCornerShape(size = 75.dp))
                                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                                    .clickable{
                                        buttonColor6 = Color(0xFF27B51A)

                                        scope.launch{
                                            delay(500)
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            if (currentUser != null) {
                                                val userId = currentUser.uid
                                                sendDirectionToFlask(userId,"NW") { result ->
                                                    onNextScreen()
                                                }
                                            }
                                        }

                                    }
                            ){
                                Image(
                                    painter=painterResource(R.drawable.nothwest_arrow),
                                    contentDescription = "",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }




                        }

//ARROW 7
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)

                        ){
                            Box(
                                modifier=Modifier
                                    .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                                    .width(75.dp)
                                    .height(75.dp)
                                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
                                    .border(width = 5.dp, color = buttonColor7,shape = RoundedCornerShape(size = 75.dp))
                                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                                    .clickable{
                                        buttonColor7 = Color(0xFF27B51A)

                                        scope.launch{
                                            delay(500)
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            if (currentUser != null) {
                                                val userId = currentUser.uid
                                                sendDirectionToFlask(userId,"SE") { result ->
                                                    onNextScreen()
                                                }
                                            }
                                        }

                                    }
                            ){
                                Image(
                                    painter=painterResource(R.drawable.southeast_arrow),
                                    contentDescription = "",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

//ARROW 8
                            Box(
                                modifier=Modifier
                                    .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
                                    .width(75.dp)
                                    .height(75.dp)
                                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
                                    .border(width = 5.dp, color = buttonColor8, shape = RoundedCornerShape(size = 75.dp))
                                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp)
                                    .clickable{
                                        buttonColor8 = Color(0xFF27B51A)
                                        scope.launch{
                                            delay(500)
                                            val currentUser = FirebaseAuth.getInstance().currentUser
                                            if (currentUser != null) {
                                                val userId = currentUser.uid
                                                sendDirectionToFlask(userId,"Left") { result ->
                                                    onNextScreen()
                                                }
                                            }
                                        }

                                    }
                            ){
                                Image(
                                    painter=painterResource(R.drawable.left),
                                    contentDescription = "",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }



                        }



                    }



                }

            }
        } //END OF ORIGINAL SCREEN


        //GREY OVERLAY HANDLED BY IF STATEMENT
        if(overlay_boolean.value){
            Box(
                modifier=Modifier
                    .offset(x = 0.dp, y = 0.dp)
                    .width(430.dp)
                    .height(932.dp)
                    .background(color = Color(0x4FFFFFFF))
                    .fillMaxSize()

            ){
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
                            text = "Click the Left Arrow",
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


    } //END OF PAGE BOX
}

