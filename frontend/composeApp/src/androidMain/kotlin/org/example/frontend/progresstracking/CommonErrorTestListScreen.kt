package org.example.frontend.progresstracking

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

// --- Design Palette Constants ---
private val DarkBlue = Color(0xFF000278)
private val YellowText = Color(0xFFFFE100)
private val PureWhite = Color(0xFFFFFFFF)
private val TranslucentSurface = Color(0xE6FFFFFF) // 90% Opacity Solid Card Base
private val NavBlue = Color(0xD9000278)

// Domain Categorization Accents
private val SpatialAccent = Color(0xFFD214BF)   // Pink
private val AlphabetAccent = Color(0xFF33BDF8)  // Light Blue
private val WordAccent = Color(0xFFF0A523)      // Orange
private val DefaultAccent = Color(0xFF10B981)   // Green

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommonErrorTestListScreen(onHomeClick: () -> Unit, userId: String) {
    val context = LocalContext.current
    var groupedErrors by remember { mutableStateOf<Map<String, List<DyslexiaError>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        try {
            val response = RetrofitClient.apiService.getCommonErrors(userId)
            // Group payload by source category cleanly to separate Assessments from Active Quizzes
            groupedErrors = response.groupBy { it.sourceCategory.ifBlank { "Uncategorized Sessions" } }

            if (response.isEmpty()) {
                Toast.makeText(context, "No targeted practice records found.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Sync offline: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image Fill
            Image(
                painter = painterResource(id = R.drawable.progressbkg),
                contentDescription = "Theme Background",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 50.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stylish Screen Header
                Text(
                    text = "Common Error\nAnalytics",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontFamily = FontFamily(Font(R.font.windsol)),
                        fontWeight = FontWeight.Bold,
                        color = YellowText,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Layout Router
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = YellowText, strokeWidth = 5.dp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Analyzing Mistake Profiles...",
                                    style = TextStyle(
                                        color = PureWhite,
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily(Font(R.font.windsol))
                                    )
                                )
                            }
                        }
                        groupedErrors.isEmpty() -> {
                            Text(
                                text = "Perfect Progress! No active errors logged.",
                                style = TextStyle(
                                    color = PureWhite,
                                    fontSize = 22.sp,
                                    fontFamily = FontFamily(Font(R.font.windsol)),
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(horizontal = 40.dp)
                            )
                        }
                        else -> {
                            // Categorized Scrolling List
                            // Modernized Categorized Layout execution routing
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp)
                            ) {
                                // Group payload dynamically by server tags
                                groupedErrors.forEach { (category, errors) ->

                                    // Sticky section headers map top-level scopes (Assessments vs Quizzes)
                                    stickyHeader {
                                        CategorySectionHeader(title = category)
                                    }

                                    // Render detailed mistake profiles below active anchor headers
                                    items(errors) { error ->
                                        ModernErrorCard(errorItem = error)
                                    }
                                }

                                // Bottom padding bumper protects visual contents from overlapping footer layouts
                                item { Spacer(modifier = Modifier.height(120.dp)) }
                            }
                        }
                    }
                }
            }

            // Fixed Bottom Bar Navigation Interface
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
                        contentDescription = "Return Home",
                        modifier = Modifier.size(35.dp)
                    )
                    Text(
                        text = "Home",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = FontFamily(Font(R.font.windsol)),
                            color = PureWhite
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xE6000278), shape = RoundedCornerShape(12.dp))
            .border(1.dp, YellowText, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title.uppercase(),
            style = TextStyle(
                fontSize = 22.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight.Bold,
                color = YellowText
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModernErrorCard(errorItem: DyslexiaError) {
    // Determine target accent tint via domain naming indicators
    val accentTint = when {
        errorItem.levelTitle.contains("Level_1", ignoreCase = true) ||
                errorItem.levelTitle.contains("Spatial", ignoreCase = true) -> SpatialAccent

        errorItem.levelTitle.contains("Level_2", ignoreCase = true) ||
                errorItem.levelTitle.contains("Letter", ignoreCase = true) -> AlphabetAccent

        errorItem.levelTitle.contains("Level_3", ignoreCase = true) ||
                errorItem.levelTitle.contains("Word", ignoreCase = true) -> WordAccent

        else -> DefaultAccent
    }

    // Main Item Layout Container
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(TranslucentSurface)
            .border(width = 1.dp, color = Color(0x33000000), shape = RoundedCornerShape(20.dp))
            .padding(end = 16.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Visual Border Tab accenting layout side
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .background(accentTint)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Content Area Container
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Title Header mapped cleanly
            Text(
                text = errorItem.levelTitle.replace("_", " "),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.windsol)),
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
            )

            // Dynamic Chip Flow Layout mapping targeted concepts
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                errorItem.errorConcepts.forEach { concept ->
                    ConceptChip(text = concept.trim(), tint = accentTint)
                }
            }
        }
    }
}

@Composable
fun ConceptChip(text: String, tint: Color) {
    Box(
        modifier = Modifier
            .background(color = tint.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = tint.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.windsol)),
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
        )
    }
}