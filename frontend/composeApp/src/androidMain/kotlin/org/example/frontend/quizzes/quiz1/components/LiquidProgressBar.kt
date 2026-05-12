// Change this line at the top of LiquidProgressBar.kt:
package org.example.frontend.quizzes.quiz1.components
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun LiquidProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    liquidColor: Color = Color(0xFF00E5FF),
    backgroundColor: Color = Color(0xFF1A237E).copy(alpha = 0.3f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ProgressFill"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "WaveTransition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WavePhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        // 1. Draw the background track
        drawRect(color = backgroundColor, size = size)

        val width = size.width
        val height = size.height

        // 2. Calculate the "filling" width based on progress
        val fillWidth = width * animatedProgress

        // 3. Set wave amplitude (how much the leading edge wobbles)
        // We only wave if progress is between 1% and 99%
        val waveAmplitude = if (animatedProgress in 0.01f..0.99f) width * 0.02f else 0f

        val liquidPath = Path().apply {
            // Start at Top-Left
            moveTo(0f, 0f)

            // Draw the leading wave edge (Vertical line from top to bottom)
            val step = 2f
            var y = 0f
            while (y <= height) {
                // Wave frequency is based on the height of the bar
                val frequency = 2f * Math.PI.toFloat() / height
                // The X position is the current progress width + the sine offset
                val x = fillWidth + sin(y * frequency + wavePhase) * waveAmplitude
                lineTo(x, y)
                y += step
            }

            // Line to Bottom-Left
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = liquidPath,
            color = liquidColor
        )
    }
}