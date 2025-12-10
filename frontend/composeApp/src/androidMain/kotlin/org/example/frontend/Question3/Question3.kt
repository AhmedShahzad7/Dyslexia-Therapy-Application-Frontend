package org.example.frontend.Question3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.frontend.R
import androidx.compose.ui.layout.ContentScale

val DarkBlue = Color(0xFF000278)
val White = Color.White

@Composable
fun Question3(onNextScreen: () -> Unit) {
    val scope = rememberCoroutineScope()
    var selectedGender by remember { mutableStateOf("") }


    fun handleSelection(gender: String) {
        selectedGender = gender
        scope.launch {
            delay(2000L)
            onNextScreen()
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.loginbackgroundimage),
                contentDescription = null,
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = 90f
                        scaleX = 2.5f
                        scaleY = 2f
                    }
                    .fillMaxSize()
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Column(
                    modifier = Modifier
                        .width(316.dp)
                        .height(595.dp)
                        .background(Color(0x216E69FF), RoundedCornerShape(30.dp))
                        .padding(20.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(43.dp)
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(177.dp)
                                .height(22.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colorStops = arrayOf(
                                            0.7f to DarkBlue,
                                            0.3f to White
                                        )
                                    ),
                                    shape = RoundedCornerShape(35.dp)
                                )
                                .border(1.dp, Color.Black, RoundedCornerShape(35.dp))
                        )


                        Text(
                            modifier = Modifier
                                .width(50.dp)
                                .height(23.dp).padding(start =12.dp),

                                    text = "3/4",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xFF000278),
                                textAlign = TextAlign.Center,
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = "What is your child’s gender",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight.Normal,
                            color = DarkBlue
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // 3. Updated Buttons to use handleSelection
                    GenderButton(
                        text = "Girl",
                        isSelected = selectedGender == "Girl",
                        enabled = selectedGender.isEmpty(),
                        onClick = { handleSelection("Girl") } ,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    GenderButton(
                        text = "Boy",
                        isSelected = selectedGender == "Boy",
                        enabled = selectedGender.isEmpty(),
                        onClick = { handleSelection("Boy") },
                        modifier = Modifier.align(Alignment.CenterHorizontally)

                    )
                }
            }
        }
    }
}


@Composable
fun GenderButton(
    text: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val DarkBlue = Color(0xFF000278)

    Box(
        modifier = modifier
            .width(250.dp)
            .height(50.dp)
            .padding(vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(30.dp))
            .border(
                width = 5.dp,
                color = if (isSelected) DarkBlue else Color.White,
                shape = RoundedCornerShape(30.dp)
            )
            .clickable(enabled = enabled) { onClick() }
    ) {

        // ✅ Centered text
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            color = DarkBlue,
            fontSize = 24.sp,
            fontFamily = FontFamily(Font(R.font.windsol)),
            textAlign = TextAlign.Center
        )

        // ✅ Rounded tick on right
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(32.dp)
                    .background(Color.White, RoundedCornerShape(50)) ,
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.checkvector),
                    contentDescription = "selected",
                    modifier = Modifier.size(18.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.None
                )
            }
        }
    }
}

