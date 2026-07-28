package org.example.frontend.Levelselection

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.example.frontend.NetworkConfig
import org.example.frontend.R
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

val babyGemoyFontFamily = FontFamily(
    Font(R.font.baby_gemoy, FontWeight.Normal)
)

data class PlanetData(
    val imageRes: Int,
    val label: String,
    val boxColor: Color,
    val textColor: Color,
    val isLabelAbove: Boolean,
    val isLocked: Boolean = true,
    val levelNumber: Int = 0,
    val isQuiz: Boolean = false,
    val quizNumber: Int = 0
)

@Composable
fun Levelselection(
    onNavigateHome: () -> Unit,
    onNavigateToLevel: (Int) -> Unit,
    onNavigateToQuiz: (Int) -> Unit
) {
    val context = LocalContext.current
    // Index mapping: 0=Lvl1, 1=Quiz1, 2=Lvl2, 3=Quiz2, 4=Lvl3, 5=Quiz3, 6=Lvl4
    var maxUnlockedIndex by remember { mutableIntStateOf(0) }

    // Force Landscape Orientation
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        onDispose {
            if (originalOrientation != null) {
                activity.requestedOrientation = originalOrientation
            }
        }
    }

    // Fetch Scores & Progression Flags dynamically
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val ip = NetworkConfig.SERVER_IP
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url("http://$ip/api/scores/$userId")
                .get()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("LevelSelection", "Failed to fetch scores", e)
                    Log.d("FlaskAPI", "Response: error")
                }

                override fun onResponse(call: Call, response: Response) {
                    val result = response.body?.string()

                    if (response.isSuccessful && result != null) {
                        try {
                            Log.d("FlaskAPI", "Response: $result")
                            val jsonObject = JSONObject(result)
                            if (jsonObject.optString("status") == "success") {

                                val newMaxIndex = jsonObject.optInt("assessment_unlocked_index", 0)

                                Handler(Looper.getMainLooper()).post {
                                    maxUnlockedIndex = newMaxIndex
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("LevelSelection", "JSON Parsing error", e)
                        }
                    }
                }
            })
        }
    }

    val planets = listOf(
        PlanetData(R.drawable.p1, "LEVEL 1", Color(0xFF00006B), Color(0xFF27B51A), isLabelAbove = false, isLocked = maxUnlockedIndex < 0, levelNumber = 1),
        PlanetData(R.drawable.p2, "QUIZ 1", Color(0xFF27B51A), Color(0xFFEB4335), isLabelAbove = true,  isLocked = maxUnlockedIndex < 1, isQuiz = true, quizNumber = 1),
        PlanetData(R.drawable.p3, "LEVEL 2", Color(0xFFF8335D), Color(0xFF27B51A), isLabelAbove = false, isLocked = maxUnlockedIndex < 2, levelNumber = 2),
        PlanetData(R.drawable.p4, "QUIZ 2", Color(0xFFFBBC05), Color(0xFF4285F4), isLabelAbove = true,  isLocked = maxUnlockedIndex < 3, isQuiz = true, quizNumber = 2),
        PlanetData(R.drawable.p5, "LEVEL 3", Color(0xFF8A38F5), Color(0xFFFFE100), isLabelAbove = true,  isLocked = maxUnlockedIndex < 4, levelNumber = 3),
        PlanetData(R.drawable.p6, "QUIZ 3", Color(0xFF4285F4), Color(0xFF000278), isLabelAbove = true,  isLocked = maxUnlockedIndex < 5, isQuiz = true, quizNumber = 3),
        PlanetData(R.drawable.p7, "LEVEL 4", Color(0xFF27B51A), Color(0xFF1517B2), isLabelAbove = false, isLocked = maxUnlockedIndex < 6, levelNumber = 4)
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.level_world),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            itemsIndexed(planets) { index, planet ->
                val isHigh = index % 2 != 0
                PlanetNode(
                    planet = planet,
                    modifier = Modifier.offset(y = if (isHigh) (-60).dp else 60.dp),
                    onClick = {
                        if (planet.isLocked) {
                            Toast.makeText(context, "Complete previous levels to unlock!", Toast.LENGTH_SHORT).show()
                        } else if (planet.isQuiz) {
                            // Route directly to respective dynamic Quiz instances
                            onNavigateToQuiz(planet.quizNumber)
                        } else {
                            // Route to respective Therapy levels
                            onNavigateToLevel(planet.levelNumber)
                        }
                    }
                )
            }
        }

        Image(
            painter = painterResource(id = R.drawable.exit),
            contentDescription = "Exit Level Selection",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
                .size(50.dp)
                .clickable { onNavigateHome() }
        )
    }
}

@Composable
fun PlanetNode(planet: PlanetData, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (planet.isLabelAbove) {
            PlanetLabel(planet)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clickable { onClick() }
        ) {
            Image(
                painter = painterResource(id = planet.imageRes),
                contentDescription = planet.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().let {
                    if (planet.isLocked) it.background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(100.dp)) else it
                }
            )

            if (planet.isLocked) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.lock),
                        contentDescription = "Locked",
                        modifier = Modifier.size(45.dp)
                    )
                }
            }
        }

        if (!planet.isLabelAbove) {
            Spacer(modifier = Modifier.height(12.dp))
            PlanetLabel(planet)
        }
    }
}

@Composable
fun PlanetLabel(planet: PlanetData) {
    Box(
        modifier = Modifier
            .background(
                color = planet.boxColor.copy(alpha = 0.75f),
                shape = RoundedCornerShape(35.dp)
            )
            .border(
                width = 2.dp,
                color = Color.White,
                shape = RoundedCornerShape(35.dp)
            )
            .padding(15.dp)
    ) {
        Text(
            text = planet.label,
            color = planet.textColor,
            fontFamily = babyGemoyFontFamily,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}