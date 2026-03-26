package org.example.frontend.homescreenpage

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException




// --- Colors ---
private val NavBlue = Color(0xDE000278)
private val CoinOrange = Color(0xFFF0A523)
private val ProgressCyan = Color(0xFF1FFFD2)
private val JumpYellow = Color(0xFFFFE100)
private val DarkBlueBorder = Color(0xFF000278)

@Composable
fun HomePage(onNavigateToProgress: () -> Unit) {
    val ip= NetworkConfig.SERVER_IP
    var showScoreDialog by remember { mutableStateOf(false) }
    var userScores by remember { mutableStateOf<List<LevelScore>>(emptyList()) }

    fun fetchScoresFromFlask(userid: String, onResult: (List<LevelScore>) -> Unit) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("http://$ip/api/scores/$userid")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FlaskAPI", "Error fetching scores! ${e.message}", e)
                Handler(Looper.getMainLooper()).post {
                    onResult(emptyList()) // Return an empty list on failure
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val result = response.body?.string()
                Log.d("FlaskAPI", "Scores Response: $result")
                val parsedScores = mutableListOf<LevelScore>()

                if (response.isSuccessful && result != null) {
                    try {
                        //  Parse the JSON response: {"status": "success", "data": [...]}
                        val jsonObject = JSONObject(result)
                        val status = jsonObject.optString("status")

                        if (status == "success") {
                            val dataArray = jsonObject.getJSONArray("data")

                            // Loop through the array and convert to LevelScore objects
                            for (i in 0 until dataArray.length()) {
                                val item = dataArray.getJSONObject(i)
                                val level = item.getString("level")
                                val score = item.getString("score")
                                parsedScores.add(LevelScore(level, score))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FlaskAPI", "JSON Parsing error: ${e.message}")
                    }
                } else {
                    Log.e("FlaskAPI", "Server error or empty response: ${response.code}")
                }

                //Return the parsed list on the Main Thread so Compose can use it
                Handler(Looper.getMainLooper()).post {
                    onResult(parsedScores)
                }
            }
        })
    }
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            fetchScoresFromFlask(userId) { fetchedList ->

                userScores = fetchedList
                showScoreDialog = true
            }

        }
    }
    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Background Image
            Image(
                painter = painterResource(id = R.drawable.homepagebkg),
                contentDescription = "Background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // DIV 1: COINS CARD
                Box(
                    modifier = Modifier
                        .width(370.dp)
                        .height(112.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.frame34),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.matchParentSize()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.coinsvgrepo),
                            contentDescription = "Coins Image",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .padding(1.dp)
                                .width(75.dp)
                                .height(80.dp)
                        )

                        Column(
                            modifier = Modifier
                                .width(151.dp)
                                .height(98.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Coins",
                                style = TextStyle(
                                    fontSize = 40.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = CoinOrange,
                                )
                            )
                            Text(
                                text = "250",
                                style = TextStyle(
                                    fontSize = 40.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = CoinOrange,
                                )
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.monkey),
                            contentDescription = "Monkey",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .width(90.dp)
                                .height(99.dp)
                        )
                    }
                }

                // --- DIV 2: PROGRESS TRACKING (Frame 32) - ANIMATED ---
                // 1. Setup Interaction Source
                // for backened naviagate the home to progress page
                val interactionSourceProgress = remember { MutableInteractionSource() }
                val isPressedProgress by interactionSourceProgress.collectIsPressedAsState()

                // 2. Animate Scale
                val scaleProgress by animateFloatAsState(
                    targetValue = if (isPressedProgress) 0.95f else 1f,
                    label = "scaleProgress"
                )

                Box(
                    modifier = Modifier
                        .width(370.dp)
                        .height(358.dp)
                        // 3. Apply Scale Modifier
                        .scale(scaleProgress)
                        .clickable(
                            interactionSource = interactionSourceProgress, // 4. Pass Source here
                            indication = null
                        ) { onNavigateToProgress() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.frame32),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.matchParentSize()
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Progress Tracking",
                                style = TextStyle(
                                    fontSize = 36.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = ProgressCyan,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.width(264.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.vector1),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(37.dp)
                                    .height(41.dp)
                            )
                        }
                    }
                }

                // --- DIV 3: JUMP IN TO LEVELS (Frame 31)
                // 1. Setup Interaction Source
                // navigate to level page
                val interactionSourceLevels = remember { MutableInteractionSource() }
                val isPressedLevels by interactionSourceLevels.collectIsPressedAsState()

                // 2. Animate Scale
                val scaleLevels by animateFloatAsState(
                    targetValue = if (isPressedLevels) 0.95f else 1f,
                    label = "scaleLevels"
                )

                Box(
                    modifier = Modifier
                        .width(369.dp)
                        .height(258.dp)
                        // 3. Apply Scale Modifier
                        .scale(scaleLevels)
                        .border(width = 1.dp, color = DarkBlueBorder, shape = RoundedCornerShape(size = 35.dp))
                        .clickable(
                            interactionSource = interactionSourceLevels, // 4. Pass Source here
                            indication = null
                        ) { println("Opening Levels...") }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.frame31),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.matchParentSize()
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Jump in to Levels",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontFamily = FontFamily(Font(R.font.baby_gemoy)),
                                fontWeight = FontWeight(400),
                                color = JumpYellow,
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                }
            }

            // 3. BOTTOM NAV BAR
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(430.dp)
                    .height(94.dp)
                    .background(
                        color = NavBlue,
                        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)
                    )
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .width(81.dp)
                            .height(86.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.vector2),
                            contentDescription = "Home",
                            modifier = Modifier
                                .width(31.dp)
                                .height(33.dp)
                        )
                        Text(
                            text = "Home",
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                        )
                    }
                }
            }
            //Show the Popup
            if (showScoreDialog) {
                ScorePopup(
                    scores = userScores,
                    onDismiss = { showScoreDialog = false }
                )
            }
        }

    }
}