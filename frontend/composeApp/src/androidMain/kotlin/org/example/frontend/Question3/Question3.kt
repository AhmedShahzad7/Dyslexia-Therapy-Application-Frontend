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

val DarkBlue = Color(0xFF000278)
val White = Color.White

@Composable
fun Question3(onNextScreen: () -> Unit) {
    val scope = rememberCoroutineScope()
    var selectedGender by remember { mutableStateOf("") }


    fun handleSelection(gender: String) {
        selectedGender = gender
        scope.launch {
            delay(200)
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

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "3/4",
                            color = DarkBlue,
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic
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
                        onClick = { handleSelection("Girl") }
                    )
                    GenderButton(
                        text = "Boy",
                        isSelected = selectedGender == "Boy",
                        onClick = { handleSelection("Boy") }
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
    onClick: () -> Unit
) {
    val DarkBlue = Color(0xFF000278)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(50.dp)
            .clickable { onClick() }
            .background(
                color = Color.White,
                shape = RoundedCornerShape(30.dp)
            )
            .border(
                width = 2.dp,
                color = if (isSelected) DarkBlue else Color.White,
                shape = RoundedCornerShape(30.dp)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                color = DarkBlue,
                fontSize = 22.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight.SemiBold
            )


            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(Color.White, RoundedCornerShape(50))
                        .border(1.dp, DarkBlue, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}