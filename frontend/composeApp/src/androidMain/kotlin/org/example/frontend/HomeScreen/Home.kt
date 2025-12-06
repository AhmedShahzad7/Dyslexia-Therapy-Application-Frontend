package org.example.frontend.HomeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(){
    Box(
        modifier = Modifier.fillMaxSize().background(color = Color.White),
        Alignment.Center,
    )
    {
        Text(
            text = "Home Screen",
            color = Color(0xFF800080),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            )
    }
}

//@Preview
//@Composable
//fun HomeScreenPreview(){
//    HomeScreen()
//}
