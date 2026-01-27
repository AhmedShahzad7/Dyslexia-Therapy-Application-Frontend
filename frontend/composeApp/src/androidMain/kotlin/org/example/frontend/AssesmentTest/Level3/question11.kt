package org.example.frontend.AssesmentTest.Level3

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
import org.json.JSONArray


@Composable
fun Question11(onNextScreen: () -> Unit){
    val context = LocalContext.current
    val overlay_boolean= remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }
    val part_boolean= remember { mutableStateOf(false) }

    //BACKEND HANDLER VARIABLES
    val temp_store=remember { mutableStateListOf<String>() }


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
    //SENDING ERRORS TO FLASK VIA TEMP STORE VARIABLE
    val question_number="11"
    fun sendErrorsToFlask(userid:String,answers:List<String>,onResult: (String) -> Unit) {
        val answersJsonString = JSONArray(answers).toString()
        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userid)
            .addFormDataPart("answers_list", answersJsonString)
            .addFormDataPart("question_number", question_number)
            .build()

        val request = Request.Builder()
            .url("http://192.168.1.9:5000/check_answers_q11")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FlaskAPI", "Error! ${e.message}", e)
                onResult("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string() ?: "No response"
                Log.d("FlaskAPI", "Response: $result")
                onResult(result)
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
            val mediaPlayer = MediaPlayer.create(context, R.raw.doraemon_alevel3q11)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            delay(3000)
            overlay_boolean.value = false
            speaker_boolean.value = false
        }
    }
    //PRESSED OPTION FUNCTION
    fun Clicked_Option(item:String){
        if(!part_boolean.value) {
            temp_store.add(item);
            part_boolean.value = true
        }
        else if(part_boolean.value){
            temp_store.add(item);
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                sendErrorsToFlask(userId,temp_store) {
                result ->Log.d("API_RESULT", result)
                }
                onNextScreen()
            }
        }
    }







    //DISPLAY-----------------------
    Box(
        modifier=Modifier.fillMaxSize(),
    ){
        Image(
            painter=painterResource(R.drawable.assessment_level3q11),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )


        //Original Screen
        Box(
            modifier=Modifier
                .width(299.dp)
                .height(550.dp)
                .background(color = Color(0xC7FFFFFF), shape = RoundedCornerShape(size = 35.dp))
                .align(Alignment.Center)
        ){
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                //Question Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Text(
                        text = "Question no 11",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(color = Color(0x00FFFFFF))
                        .padding(start = 5.dp)
                ) {
                    Text(
                        text = "Circle the option that\n matches with the word",
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
                        modifier = Modifier.offset(y = 0.dp)
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
                //WORD BOX
                //Q11 (A)
                if(!part_boolean.value){
                    Row(
                        modifier = Modifier.height(55.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ){
                        //PART TEXT
                        Text(
                            text = "ben",
                            style = TextStyle(
                                fontSize = 50.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xF527B51A),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                    //PART OPTIONS
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 7.dp, top = 58.dp, end = 7.dp, bottom = 58.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        OptionCircle("den",
                            onOptionClick = {    Clicked_Option("den")}
                            )
                        OptionCircle("ben",
                            onOptionClick = {    Clicked_Option("ben")}
                            )
                        OptionCircle("hen",
                            onOptionClick = {    Clicked_Option("hen")}
                            )
                    }



                }
                else if(part_boolean.value){
                    Row(
                        modifier = Modifier.height(55.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ){
//PART TEXT
                        Text(
                            text = "pen",
                            style = TextStyle(
                                fontSize = 50.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xF527B51A),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
//PART OPTIONS
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 7.dp, top = 58.dp, end = 7.dp, bottom = 58.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        OptionCircle("qen",
                            onOptionClick = {
                                Clicked_Option("qen")
                            })
                        OptionCircle("ten",
                            onOptionClick = {    Clicked_Option("ten")}
                            )
                        OptionCircle("pen",
                            onOptionClick = {    Clicked_Option("pen")}
                            )
                    }

                }




















            }//END OF MIDDLE BOX
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
                            text = "Circle the option that\n matches with the word",
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


    } //END OF PAGE BOX
}

//REPEATING CIRCLE FUNCTION INSTEAD OF MULTIPLE BOXES ADDING FUNCTION TO PASS CIRCLE TEXT
@Composable
private fun OptionCircle(
    text: String,
    onOptionClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .shadow(elevation = 25.dp, spotColor = Color(0x40000000), ambientColor = Color(0x40000000))
            .width(75.dp)
            .height(75.dp)
            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 75.dp))
            .clickable { onOptionClick() }
            .padding(top = 5.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.height(20.dp),
            text = text,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight(400),
                textAlign = TextAlign.Center,
                color = Color(0xFF000278),
            )
        )

    }
}

