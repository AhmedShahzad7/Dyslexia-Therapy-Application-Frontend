package org.example.frontend.Question4

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.frontend.R

val DarkBlueQ4 = Color(0xFF000278)
val WhiteQ4 = Color.White

@Composable
fun Question4(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    var childName by remember { mutableStateOf("") }
    fun handleSelection() {
        scope.launch {
            delay(2000L)
            navController.navigate("LoginScreen") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {

            Image(
                painter = painterResource(R.drawable.loginbackgroundimage),
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
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(316.dp)
                    .height(595.dp)
                    .background(Color(0x216E69FF), RoundedCornerShape(30.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .width(177.dp)
//                            .height(22.dp)
//                            .background(
//                                brush = Brush.linearGradient(
//                                    colorStops = arrayOf(
//                                        1f to DarkBlueQ4,
//                                        0.2f to WhiteQ4
//                                    )
//                                ),
//                                shape = RoundedCornerShape(35.dp)
//                            )
//                            .border(1.dp, Color.Black, RoundedCornerShape(35.dp))
//                    )
//
//                    Text(
//                        text = "4/4",
//                        color = DarkBlueQ4,
//                        style = TextStyle(
//                            fontSize = 24.sp,
//                            fontFamily = FontFamily(Font(R.font.windsol)),
//                            fontWeight = FontWeight.Bold,
//                            fontStyle = FontStyle.Italic
//                        )
//                    )
//                }
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
                                        1f to DarkBlueQ4,
                                        0.2f to WhiteQ4
                                    )
                                ),
                                shape = RoundedCornerShape(35.dp)
                            )
                            .border(1.dp, Color.Black, RoundedCornerShape(35.dp))
                    )

                    Text(
                        modifier = Modifier
                            .width(50.dp)
                            .height(23.dp)
                            .padding(start = 12.dp),
                        text = "4/4",
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
                    text = "What is your Child Name?",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 30.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Normal,
                        color = DarkBlueQ4,

                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(50.dp))

                TextField(
                    value = childName,
                    onValueChange = { childName = it },
                    placeholder = {
                        Text(
                            text = "Enter a name",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily(Font(R.font.windsol)),
                                fontWeight = FontWeight(400),
                                color = Color(0xAB000278),
                                textAlign = TextAlign.Center
                            )
                        )
                    },
                    modifier = Modifier
                        .width(250.dp)
                        .height(60.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(
                        fontSize = 22.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        color = DarkBlueQ4,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(60.dp))

                Box(
                    modifier = Modifier
                        .width(178.dp)
                        .height(66.dp)
                        .clickable {
                            println("Continue clicked! Child Name: $childName")
                            if(childName.isNotBlank())
                            {
                                handleSelection()
                            }
                        }
                        .background(DarkBlueQ4, RoundedCornerShape(35.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Continue",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            fontWeight = FontWeight(400),
                            fontStyle = FontStyle.Normal,
                            color = WhiteQ4,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}
