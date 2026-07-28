package org.example.frontend.AssesmentTest.Level4


import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import org.example.frontend.R
import coil.ImageLoader
import com.google.firebase.auth.FirebaseAuth
import org.example.frontend.AssesmentTest.Level2.AudioRecorderHelper
import java.io.File
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.example.frontend.AssesmentTest.Level4.uploadSentenceAudio
import org.example.frontend.NetworkConfig
import java.io.IOException


@Composable
fun Question16(onNextScreen:()->Unit){
    val context = LocalContext.current
    val overlay_boolean= remember { mutableStateOf(false) }
    val speaker_boolean = remember { mutableStateOf(false) }
    val ip = NetworkConfig.SERVER_IP

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
            val mediaPlayer = MediaPlayer.create(context, R.raw.read_sentence)
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
        "The cat is big",
        "I can run fast",
        "He had a red hat"
    )

    val currentIndex = remember { mutableStateOf(0) }
    val isplaying = remember { mutableStateOf(false) }

    val audioRecorder = remember { AudioRecorderHelperSentence(context) }
    var recordedFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var transcriptionText by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isplaying.value = true
                audioRecorder.startRecordingSentence()
            } else {
                Log.e("Audio", "Microphone permission denied")
            }
        }
    )


    Box(
        modifier=Modifier.fillMaxSize(),
    )
    {
        Image(
            painter = painterResource(id = R.drawable.assessmenttestquestion1),
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
                        text = "Question no 16",
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
                            text = "Read sentence shown\n below",
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
                        .width(259.dp)
                        .height(294.55463.dp)
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
                                text = sentences[currentIndex.value],
                                style = TextStyle(
                                    fontSize = 30.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xF527B51A),
                                    textAlign = TextAlign.Center,
                                )
                            )
                        Image(
                            painter = painterResource(
                                id=if (isplaying.value) R.drawable.pause else R.drawable.play
                            ),
                            contentDescription = "",
                            contentScale = ContentScale.None,
                            modifier=Modifier
                                .padding(33.33333.dp)
                                .width(91.66666.dp)
                                .height(91.66666.dp)
                                .background(color = Color(0xF527B51A), shape = RoundedCornerShape(50))
                                .clickable {
                                    if (!isplaying.value) {
                                        // Start Recording
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        // Stop Recording
                                        isplaying.value = false
                                        recordedFile = audioRecorder.stopRecordingSentence()
                                    }
                                },
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)

                        )
                        Box(
                            Modifier
                                .padding(bottom = 16.dp)
                                .width(150.dp)
                                .height(50.dp)
                                .background(color = Color(0xF527B51A), shape = RoundedCornerShape(size = 35.dp))
                                .clickable(enabled = !isProcessing && recordedFile != null) {
                                    val currentUser = FirebaseAuth.getInstance().currentUser
                                    if (currentUser != null) {
                                        val userId = currentUser.uid
                                        val targetSound = sentences[currentIndex.value]

                                        isProcessing = true
                                        recordedFile?.let { file ->
                                            uploadSentenceAudio(
                                                file,
                                                ip,
                                                targetSound,
                                                userId
                                            ) { result ->
                                                isProcessing = false
                                                transcriptionText = result ?: "Error"
                                                Log.d("FlaskAPI", "Phoneme Match Result: $transcriptionText")

                                                if (transcriptionText.contains("\"is_correct\": true")) {
                                                    if (currentIndex.value < sentences.lastIndex) {
                                                        currentIndex.value++
                                                        recordedFile = null
                                                    } else {
                                                        onNextScreen()
                                                    }
                                                } else {
                                                    if (currentIndex.value < sentences.lastIndex) {
                                                        currentIndex.value++
                                                        recordedFile = null
                                                    } else {
                                                        onNextScreen()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center



                        )
                        {
                            Text(
                                text = if (isProcessing) "Sending..." else "Submit",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFFFFFFFF),
                                )
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
                            text = "Read sentence shown\n below",
                            style = TextStyle(
                                fontSize = 25.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF27B51A),
                                textAlign = TextAlign.Center,
                            )
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
}


fun uploadSentenceAudio(
    audioFile: File,
    serverIp: String,
    targetSound: String,
    userId: String,
    onResult: (String?) -> Unit
) {
    try {
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("target_sound", targetSound)
            .addFormDataPart("user_id", userId)
            .addFormDataPart("question_number", "16") // Hardcoded to Question 9
            .addFormDataPart(
                "audio",
                audioFile.name,
                audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
            )
            .build()

        val baseUrl = if (serverIp.startsWith("http")) serverIp else "http://$serverIp"

        val request = Request.Builder()
            .url("$baseUrl/question16")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            private fun runOnMainThread(action: () -> Unit) {
                Handler(Looper.getMainLooper()).post(action)
            }
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnMainThread { onResult("Network Error: ${e.localizedMessage}") }
            }
            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnMainThread {
                    if (response.isSuccessful) onResult(responseData)
                    else onResult("Server error: ${response.code}\n$responseData")
                }
            }
        })
    } catch (e: Exception) {
        e.printStackTrace()
        Handler(Looper.getMainLooper()).post {
            onResult("App Error: ${e.message}")
        }
    }
}

class AudioRecorderHelperSentence(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecordingSentence() {
        audioFile = File(context.cacheDir, "temp_speech_phoneme.wav")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile?.absolutePath)
            prepare()
            start()
        }
    }

    fun stopRecordingSentence(): File? {
        recorder?.apply {
            try { stop() } catch (e: Exception) { e.printStackTrace() }
            release()
        }
        recorder = null
        return audioFile
    }
}