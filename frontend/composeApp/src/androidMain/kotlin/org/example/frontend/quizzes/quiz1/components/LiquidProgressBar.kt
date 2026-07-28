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
        drawRect(color = backgroundColor, size = size)

        val width = size.width
        val height = size.height

        val fillWidth = width * animatedProgress

        val waveAmplitude = if (animatedProgress in 0.01f..0.99f) width * 0.02f else 0f

        val liquidPath = Path().apply {
            moveTo(0f, 0f)

            val step = 2f
            var y = 0f
            while (y <= height) {
                val frequency = 2f * Math.PI.toFloat() / height
                val x = fillWidth + sin(y * frequency + wavePhase) * waveAmplitude
                lineTo(x, y)
                y += step
            }

            lineTo(0f, height)
            close()
        }

        drawPath(
            path = liquidPath,
            color = liquidColor
        )
    }
}