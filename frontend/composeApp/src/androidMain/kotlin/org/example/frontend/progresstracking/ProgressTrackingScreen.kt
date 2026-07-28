package org.example.frontend.progresstracking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.viewmodel.compose.viewModel
import org.example.frontend.R

private val DarkBlue = Color(0xFF000278)
private val YellowText = Color(0xFFFFE100)
private val YellowCard = Color(0xFFFFE100)
private val PinkCard = Color(0xFFD214BF)
private val OrangeProgress = Color(0xFFF0A523)
private val LightBlueCard = Color(0xFF33BDF8)
private val GreenCard = Color(0xFFC5F4C3)
private val RedProgress = Color(0xFFF02323)

@Composable
fun ProgressTrackingScreen(
    userId: String,
    onHomeClick: () -> Unit,
    onNavigateToErrorList: () -> Unit,
    viewModel: ProgressTrackingViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadData(userId)
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.progressbkg),
                contentDescription = "Background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Progress Tracking",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Bold,
                        color = YellowText,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(155.dp)
                            .background(color = YellowCard, shape = RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Progress\nTracking",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    color = DarkBlue,
                                    textAlign = TextAlign.Center
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = state.overallProgress, // Dynamic Binding
                                style = TextStyle(
                                    fontSize = 28.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight.Bold,
                                    color = DarkBlue
                                )
                            )
                        }
                    }

                    // Common Error List Button
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(155.dp)
                            .background(color = DarkBlue, shape = RoundedCornerShape(30.dp))
                            .clickable { onNavigateToErrorList() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Common\nError test\nList",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                color = YellowCard,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .width(380.dp)
                        .background(color = Color(0x216E69FF), shape = RoundedCornerShape(30.dp))
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    StatCard(
                        backgroundColor = PinkCard,
                        title = "LEVEL ATTEMPT",
                        titleColor = OrangeProgress,
                        progress = state.levelFloat,
                        progressColor = OrangeProgress,
                        progressText = state.levelText,
                        height = 180
                    )

                    StatCard(
                        backgroundColor = LightBlueCard,
                        title = "QUIZ ATTEMPT",
                        titleColor = DarkBlue,
                        progress = state.quizFloat,
                        progressColor = DarkBlue,
                        progressText = state.quizText,
                        height = 180
                    )

                    Box(
                        modifier = Modifier
                            .width(350.dp)
                            .height(92.dp)
                            .background(color = GreenCard, shape = RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier.width(300.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SCREEN TIME",
                                    style = TextStyle(
                                        fontSize = 22.sp,
                                        fontFamily = FontFamily(Font(R.font.windsol)),
                                        color = RedProgress
                                    )
                                )
                                Text(
                                    text = state.screenTimeText,
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = FontFamily(Font(R.font.windsol)),
                                        fontWeight = FontWeight.Bold,
                                        color = RedProgress
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .width(300.dp)
                                    .height(25.dp)
                                    .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(state.screenTimeFloat)
                                        .fillMaxHeight()
                                        .background(RedProgress, RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(94.dp)
                    .background(
                        color = Color(0xD9000278),
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
fun StatCard(
    backgroundColor: Color,
    title: String,
    titleColor: Color,
    progress: Float,
    progressColor: Color,
    progressText: String,
    height: Int
) {
    Box(
        modifier = Modifier
            .width(350.dp)
            .height(height.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(30.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 26.sp,
                    fontFamily = FontFamily(Font(R.font.windsol)),
                    color = titleColor
                )
            )
            Spacer(modifier = Modifier.height(15.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                CircularProgressIndicator(
                    progress = 1f,
                    color = Color.White,
                    strokeWidth = 12.dp,
                    modifier = Modifier.fillMaxSize()
                )
                CircularProgressIndicator(
                    progress = progress,
                    color = progressColor,
                    strokeWidth = 12.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = progressText,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        color = titleColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}