package org.example.frontend.homescreenpage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

//  Data Model for the scores
data class LevelScore(val level: String, val score: String)


@Composable
fun ScorePopup(
    scores: List<LevelScore>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .width(350.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(25.dp)
                )
                .border(2.dp, Color(0xFF000278), RoundedCornerShape(25.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Assessment Scores",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xDE000278)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (scores.isEmpty()) {
                    Text(
                        text = "No scores available yet!",
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(scores) { scoreItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0F0F0), RoundedCornerShape(10.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = scoreItem.level.replace("_", " "),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = scoreItem.score,
                                    color = Color(0xFFF0A523),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Close Button
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1FFFD2), RoundedCornerShape(15.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 30.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Close",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}