package org.example.frontend.Question2
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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


data class Option(
    val id: Int,
    val text: String,
    val selected: Boolean = false
)


val DarkBlue = Color(0xFF000278)
val White = Color(0xFFFFFFFF)

@Composable
fun Question2(onBack: () -> Unit) {
    var questionOptions by remember { mutableStateOf(listOf(
        Option(id = 1, text = "Never"),
        Option(id = 2, text = "Rarely"),
        Option(id = 3, text = "Sometimes"),
        Option(id = 4, text = "Frequently"),
        Option(id = 5, text = "Always")
    ))}
    val neverOption = questionOptions.first { it.id == 1 }
    val rarelyOption = questionOptions.first { it.id == 2 }
    val sometimesOption = questionOptions.first { it.id == 3 }
    val frequentlyOption = questionOptions.first { it.id == 4 }
    val AlwaysOption = questionOptions.first { it.id == 5 }


    fun selectOption(choice: Int) {
        questionOptions = questionOptions.map { option ->
            option.copy(selected = (option.id == choice))
        }
    }


        MaterialTheme {
                Box(modifier=Modifier.fillMaxSize().background(Color.White)) {
                    Image(
                        painter = painterResource(R.drawable.onefour),
                        contentDescription = null,
                        modifier=Modifier.graphicsLayer{rotationZ=90f}.graphicsLayer{
                            scaleX=2.5F
                            scaleY=2F
                        }.fillMaxSize()
                    )
                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally,verticalArrangement = Arrangement.Center) {
                        Column(modifier=Modifier
                            .width(316.dp)
                            .height(595.dp)

                            .background(color = Color(0x216E69FF), shape = RoundedCornerShape(size = 30.dp))
                            .padding(start = 10.dp, top = 20.dp, bottom = 20.dp)
                           ) {
                            Row(modifier=Modifier
                                .fillMaxWidth()
                                .height(43.dp)
                                .padding(start = 20.dp)
                              ,
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                                verticalAlignment = Alignment.CenterVertically){
                                Column(modifier=Modifier.border(width = 1.dp, color = Color(0xFF000000),shape = RoundedCornerShape(size = 35.dp))
                                    .width(177.38719.dp)
                                    .height(22.dp)
                                    .background(
                                    brush = Brush.linearGradient(
                                        colorStops = arrayOf(
                                            0.5f to DarkBlue,
                                            0.2f to White
                                        ),
                                        start = Offset.Zero,
                                        end = Offset.Infinite
                                    ),shape = RoundedCornerShape(size = 35.dp)

                                )
                                ){}
                                Text(
                                    modifier = Modifier
                                        .width(38.dp)
                                        .height(23.dp),

                                    text = "2/4",
                                    style = TextStyle(
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily(Font(R.font.windsol)),
                                        fontWeight = FontWeight(400),
                                        color = Color(0xFF000278),
                                        textAlign = TextAlign.Center,
                                    )
                                )
                           }
                            Text(
                                modifier=Modifier
                                    .width(290.dp)
                                    .height(169.dp).padding(top=40.dp),
                                text = "How often does your child confuse letters when writing?",
                                style = TextStyle(
                                    fontSize = 30.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFF000278),
                                    textAlign = TextAlign.Center,
                                )

                            )
                            Column(modifier=Modifier
                                .width(270.dp)
                                .height(310.dp)
                                .background(color = Color(0x00FFFFFF))
                                .padding(start = 20.dp, top = 10.dp, bottom = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
                                horizontalAlignment = Alignment.CenterHorizontally){
                                if(!neverOption.selected){
                                    Column(modifier=Modifier
                                        .width(250.dp)
                                        .height(50.dp)
                                        .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 35.dp))
                                        .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp).clickable(onClick = {selectOption(1)}),
                                        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                                        horizontalAlignment = Alignment.CenterHorizontally){
                                        Text(
                                            text = "Never",
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFF000278),
                                                textAlign = TextAlign.Center,
                                            )
                                        )
                                    }
                                }else{
                                    Row(
                                        modifier = Modifier
                                            .border(width = 5.dp, color = Color(0xFF000278), shape = RoundedCornerShape(size = 35.dp))
                                            .width(250.dp)
                                            .height(50.dp)
                                            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 35.dp))
                                            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Never",
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFF000278),
                                                textAlign = TextAlign.Center,
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(30.dp))
                                        Image(
                                            modifier = Modifier
                                                .width(25.dp)
                                                .height(25.dp),
                                            painter = painterResource(id = R.drawable.checkvector),
                                            contentDescription = "selected checkmark",
                                            contentScale = ContentScale.None
                                        )
                                    }
                                }

                                if(!rarelyOption.selected){
                                    Column(modifier=Modifier
                                        .width(250.dp)
                                        .height(50.dp)
                                        .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 35.dp))
                                        .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp).clickable(onClick = {selectOption(2)}),
                                        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                                        horizontalAlignment = Alignment.CenterHorizontally){
                                        Text(
                                            text = "Rarely",
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFF000278),
                                                textAlign = TextAlign.Center,
                                            )
                                        )
                                    }
                                } else{
                                    Row(
                                        modifier = Modifier
                                            .border(width = 5.dp, color = Color(0xFF000278), shape = RoundedCornerShape(size = 35.dp))
                                            .width(250.dp)
                                            .height(50.dp)
                                            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 35.dp))
                                            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Rarely",
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFF000278),
                                                textAlign = TextAlign.Center,
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(30.dp))
                                        Image(
                                            modifier = Modifier
                                                .width(25.dp)
                                                .height(25.dp),
                                            painter = painterResource(id = R.drawable.checkvector),
                                            contentDescription = "selected checkmark",
                                            contentScale = ContentScale.None
                                        )
                                    }

                                }
                                if(!sometimesOption.selected){
                                    Column(modifier=Modifier
                                        .width(250.dp)
                                        .height(50.dp)
                                        .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 35.dp))
                                        .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp).clickable(onClick = {selectOption(3)}),
                                        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                                        horizontalAlignment = Alignment.CenterHorizontally){
                                        Text(
                                            text = "Sometimes",
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFF000278),
                                                textAlign = TextAlign.Center,
                                            )
                                        )
                                    }
                                }else {
                                    Row(
                                        modifier = Modifier
                                            .border(width = 5.dp, color = Color(0xFF000278), shape = RoundedCornerShape(size = 35.dp))
                                            .width(250.dp)
                                            .height(50.dp)
                                            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 35.dp))
                                            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Sometimes",
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFF000278),
                                                textAlign = TextAlign.Center,
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(30.dp))
                                        Image(
                                            modifier = Modifier
                                                .width(25.dp)
                                                .height(25.dp),
                                            painter = painterResource(id = R.drawable.checkvector),
                                            contentDescription = "selected checkmark",
                                            contentScale = ContentScale.None
                                        )
                                    }
                                }
                                if(!frequentlyOption.selected){
                                    Column(modifier=Modifier
                                        .width(250.dp)
                                        .height(50.dp)
                                        .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 35.dp))
                                        .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp).clickable(onClick = {selectOption(4)}),
                                        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                                        horizontalAlignment = Alignment.CenterHorizontally){
                                        Text(
                                            text = "Frequently",
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFF000278),
                                                textAlign = TextAlign.Center,
                                            )
                                        )
                                    }



                                }else{
                                    Row(
                                        modifier = Modifier
                                            .border(width = 5.dp, color = Color(0xFF000278), shape = RoundedCornerShape(size = 35.dp))
                                            .width(250.dp)
                                            .height(50.dp)
                                            .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 35.dp))
                                            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Frequently",
                                            style = TextStyle(
                                                fontSize = 24.sp,
                                                fontFamily = FontFamily(Font(R.font.windsol)),
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFF000278),
                                                textAlign = TextAlign.Center,
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(30.dp))
                                        Image(
                                            modifier = Modifier
                                                .width(25.dp)
                                                .height(25.dp),
                                            painter = painterResource(id = R.drawable.checkvector),
                                            contentDescription = "selected checkmark",
                                            contentScale = ContentScale.None
                                        )
                                    }

                                }

                              if(!AlwaysOption.selected){
                                  Column(modifier=Modifier
                                      .width(250.dp)
                                      .height(50.dp)
                                      .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 35.dp))
                                      .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 10.dp).clickable(onClick = {selectOption(5)}),
                                      verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                                      horizontalAlignment = Alignment.CenterHorizontally){
                                      Text(
                                          text = "Always",
                                          style = TextStyle(
                                              fontSize = 24.sp,
                                              fontFamily = FontFamily(Font(R.font.windsol)),
                                              fontWeight = FontWeight(400),
                                              color = Color(0xFF000278),
                                              textAlign = TextAlign.Center,
                                          )
                                      )
                                  }

                              }else{
                                  Row(
                                      modifier = Modifier
                                          .border(width = 5.dp, color = Color(0xFF000278), shape = RoundedCornerShape(size = 35.dp))
                                          .width(250.dp)
                                          .height(50.dp)
                                          .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(size = 35.dp))
                                          .padding(start = 10.dp, top = 10.dp, bottom = 10.dp),
                                      horizontalArrangement = Arrangement.Center,
                                      verticalAlignment = Alignment.CenterVertically
                                  ) {
                                      Text(
                                          text = "Always",
                                          style = TextStyle(
                                              fontSize = 24.sp,
                                              fontFamily = FontFamily(Font(R.font.windsol)),
                                              fontWeight = FontWeight(400),
                                              color = Color(0xFF000278),
                                              textAlign = TextAlign.Center,
                                          )
                                      )
                                      Spacer(modifier = Modifier.width(30.dp))
                                      Image(
                                          modifier = Modifier
                                              .width(25.dp)
                                              .height(25.dp),
                                          painter = painterResource(id = R.drawable.checkvector),
                                          contentDescription = "selected checkmark",
                                          contentScale = ContentScale.None
                                      )
                                  }

                              }




                            }
                        }

                    }

                }

        }
}