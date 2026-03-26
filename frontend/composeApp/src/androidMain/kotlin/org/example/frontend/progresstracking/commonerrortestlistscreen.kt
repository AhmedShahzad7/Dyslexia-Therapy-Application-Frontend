package org.example.frontend.progresstracking

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.example.frontend.R
import org.example.frontend.api.RetrofitClient

// --- Colors ---
private val DarkBlue = Color(0xFF000278)
private val YellowText = Color(0xFFFFE100)
private val TranslucentWhite = Color(0xBDFFFFFF)
private val NavBlue = Color(0xDE000278)

@Composable
fun CommonErrorTestListScreen(onHomeClick: () -> Unit, userId: String) {
    // Context for showing the Toast message
    val context = LocalContext.current

    // State Management for dynamic data fetching
    var errorList by remember { mutableStateOf<List<DyslexiaError>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch data from Flask Backend with "Ping" logic
    LaunchedEffect(userId) {
        try {
            val response = RetrofitClient.apiService.getCommonErrors(userId)
            errorList = response

            // Connection was successful, but check if there's actually data
            if (response.isEmpty()) {
                Toast.makeText(context, "Connected! No errors found for this user.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // This triggers if the IP is wrong, Firewall is on, or phone isn't on the same Wi-Fi
            println("Network Error: ${e.message}")
            Toast.makeText(
                context,
                "Server Unreachable! Check IP: 10.109.22.5",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            isLoading = false
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Image
            Image(
                painter = painterResource(id = R.drawable.progressbkg),
                contentDescription = "Background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Title
                Text(
                    text = "Common Error Test\nList",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Bold,
                        color = YellowText,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(bottom = 30.dp)
                )

                // Error Cards List
                if (isLoading) {
                    Text(
                        text = "Connecting to Server...",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.windsol))
                    )
                } else if (errorList.isEmpty()) {
                    Text(
                        text = "No errors found!",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.windsol))
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                    ) {
                        items(errorList) { error ->
                            ErrorCard(levelTitle = error.level, errorDetail = error.detail)
                        }
                        item { Spacer(modifier = Modifier.height(110.dp)) }
                    }
                }
            }

            // 3. Bottom Navigation Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(94.dp)
                    .background(
                        color = NavBlue,
                        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)
                    )
                    .clickable { onHomeClick() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.vector2),
                        contentDescription = "Home",
                        modifier = Modifier.size(35.dp)
                    )
                    Text(
                        text = "Home",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorCard(levelTitle: String, errorDetail: String) {
    Column(
        modifier = Modifier
            .width(371.dp)
            .background(color = TranslucentWhite, shape = RoundedCornerShape(size = 20.dp))
            .padding(horizontal = 20.dp, vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = levelTitle,
            style = TextStyle(
                fontSize = 28.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
            )
        )
        Text(
            text = errorDetail,
            style = TextStyle(
                fontSize = 20.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight.Normal,
                color = DarkBlue,
            )
        )
    }
}