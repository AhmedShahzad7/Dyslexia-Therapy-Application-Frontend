package org.example.frontend.HomeScreen

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.example.frontend.NetworkConfig
import java.io.File
import java.io.IOException



// 1. Added the 'onResult' parameter to send data back to the UI
fun uploadAudioForTranscription(audioFile: File, serverIp: String, onResult: (String?) -> Unit) {
    val client = OkHttpClient()
    val ip= NetworkConfig.SERVER_IP
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "audio",
            audioFile.name,
            audioFile.asRequestBody("audio/wav".toMediaTypeOrNull())
        )
        .build()

    val request = Request.Builder()
        .url("http://"+ip+"/transcribe")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        // 2. Helper to ensure UI updates happen on the Main Thread
        private fun runOnMainThread(action: () -> Unit) {
            Handler(Looper.getMainLooper()).post(action)
        }

        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            runOnMainThread {
                onResult("Network Error: ${e.localizedMessage}")
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val responseData = response.body?.string()
            runOnMainThread {
                if (response.isSuccessful) {
                    onResult(responseData)
                } else {
                    onResult("Server error: ${response.code}\n$responseData")
                }
            }
        }
    })
}

@Composable
fun VoiceTT() {
    val context = LocalContext.current

    // UI States
    var isRecording by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var transcriptionResult by remember { mutableStateOf("Press record to start...") }

    // Audio Recorder instance
    val audioRecorder = remember { AudioRecorderHelper(context) }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isRecording = true
                audioRecorder.startRecording()
                transcriptionResult = "Recording..."
            } else {
                transcriptionResult = "Microphone permission is required."
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DyslexiAid: Speech to Text Test",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Recording Button
        Button(
            onClick = {
                if (isRecording) {
                    isRecording = false
                    isProcessing = true
                    transcriptionResult = "Sending to server..."
                    val file = audioRecorder.stopRecording()

                    if (file != null) {
                        // NOTE: Use 10.0.2.2 for Emulator, or your computer's local IPv4 for physical device
                        val serverIp = "10.0.2.2"

                        // 3. Call the updated network function
                        uploadAudioForTranscription(file, serverIp) { result ->
                            transcriptionResult = result ?: "Failed to get response"
                            isProcessing = false
                        }
                    } else {
                        transcriptionResult = "Error saving audio file."
                        isProcessing = false
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            enabled = !isProcessing
        ) {
            Text(if (isRecording) "Stop & Send" else "Start Recording")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Result Display
        if (isProcessing) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = transcriptionResult,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// --- Basic Audio Recorder Helper ---
class AudioRecorderHelper(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null

    fun startRecording() {
        audioFile = File(context.cacheDir, "test_audio.wav")
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

    fun stopRecording(): File? {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        return audioFile
    }
}