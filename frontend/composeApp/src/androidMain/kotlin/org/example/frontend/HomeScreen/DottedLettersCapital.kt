import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.lifecycle.ViewModel

class lettersViewModelCapital: ViewModel() {
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterA() {
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.8f)
            lineTo(size.width * 0.5f, size.height * 0.2f)
            lineTo(size.width * 0.8f, size.height * 0.8f)

            moveTo(size.width * 0.35f, size.height * 0.55f)
            lineTo(size.width * 0.65f, size.height * 0.55f)
        }
        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f), 0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterB() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val mid = size.height * 0.5f
            val right = size.width * 0.7f

            // Vertical spine
            moveTo(left, top)
            lineTo(left, bottom)

            // Top curve
            moveTo(left, top)
            cubicTo(
                right, top,
                right, mid * 0.9f,
                left, mid
            )

            // Bottom curve
            moveTo(left, mid)
            cubicTo(
                right, mid * 1.1f,
                right, bottom,
                left, bottom
            )
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f), 0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterC() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.7f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val centerY = size.height * 0.5f

            // Start at top-right curve
            moveTo(right, top)

            // Upper curve to left
            cubicTo(
                left, top,
                left, centerY,
                left, centerY
            )

            // Lower curve back toward bottom-right
            cubicTo(
                left, bottom,
                right, bottom,
                right, bottom
            )
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f), 0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterD() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.7f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val midY = size.height * 0.5f

            // Vertical spine
            moveTo(left, top)
            lineTo(left, bottom)

            // Curved right side
            moveTo(left, top)
            cubicTo(
                right, top,
                right, bottom,
                left, bottom
            )
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f), 0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterE() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val mid = size.height * 0.5f

            // Vertical spine
            moveTo(left, top)
            lineTo(left, bottom)

            // Top horizontal
            moveTo(left, top)
            lineTo(right, top)

            // Middle horizontal
            moveTo(left, mid)
            lineTo(right * 0.85f, mid) // Slightly shorter than top/bottom

            // Bottom horizontal
            moveTo(left, bottom)
            lineTo(right, bottom)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f), 0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterF() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val mid = size.height * 0.5f

            // Vertical spine
            moveTo(left, top)
            lineTo(left, bottom)

            // Top horizontal
            moveTo(left, top)
            lineTo(right, top)

            // Middle horizontal
            moveTo(left, mid)
            lineTo(right * 0.8f, mid) // Slightly shorter than top
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f), 0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterG() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // Start near top-right
            moveTo(w * 0.62f, h * 0.18f)

            // Upper curve going left
            cubicTo(
                w * 0.40f, h * 0.10f,
                w * 0.22f, h * 0.30f,
                w * 0.22f, h * 0.48f
            )

            // Big outer curve downwards
            cubicTo(
                w * 0.22f, h * 0.75f,
                w * 0.48f, h * 0.88f,
                w * 0.60f, h * 0.72f
            )

            // Inner horizontal stroke (the G bar)
            cubicTo(
                w * 0.66f, h * 0.64f,
                w * 0.58f, h * 0.58f,
                w * 0.44f, h * 0.58f
            )

            // Turn and go right again
            cubicTo(
                w * 0.60f, h * 0.56f,
                w * 0.76f, h * 0.56f,
                w * 0.76f, h * 0.64f
            )

            // Downward tail
            cubicTo(
                w * 0.76f, h * 0.74f,
                w * 0.70f, h * 0.86f,
                w * 0.66f, h * 0.88f
            )
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f), 0f
                )
            )
        )
    }


    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterH() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val mid = size.height * 0.5f

            // Left vertical line
            moveTo(left, top)
            lineTo(left, bottom)

            // Right vertical line
            moveTo(right, top)
            lineTo(right, bottom)

            // Middle horizontal bar
            moveTo(left, mid)
            lineTo(right, mid)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterI() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val centerX = size.width * 0.5f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f

            // Top horizontal bar
            moveTo(left, top)
            lineTo(right, top)

            // Vertical stem
            moveTo(centerX, top)
            lineTo(centerX, bottom)

            // Bottom horizontal bar
            moveTo(left, bottom)
            lineTo(right, bottom)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterJ() {
        val path = Path().apply {
            val left = size.width * 0.22f
            val right = size.width * 0.75f
            val centerX = size.width * 0.5f
            val top = size.height * 0.2f
            val bottom = size.height * 0.75f
            val curveBottom = size.height * 1.0f

            // Top horizontal bar
            moveTo(left, top)
            lineTo(right, top)

            // Vertical stem
            moveTo(centerX, top)
            lineTo(centerX, bottom)

            // Bottom hook curve
            cubicTo(
                centerX, curveBottom,
                left, curveBottom,
                left, bottom
            )
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterK() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val mid = size.height * 0.5f

            // Vertical spine
            moveTo(left, top)
            lineTo(left, bottom)

            // Upper diagonal
            moveTo(left, mid)
            lineTo(right, top)

            // Lower diagonal
            moveTo(left, mid)
            lineTo(right, bottom)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterL() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f

            // Vertical spine
            moveTo(left, top)
            lineTo(left, bottom)

            // Bottom horizontal
            moveTo(left, bottom)
            lineTo(right, bottom)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterM() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val midX = size.width * 0.5f

            // Left vertical spine
            moveTo(left, bottom)
            lineTo(left, top)

            // Right vertical spine
            moveTo(right, bottom)
            lineTo(right, top)

            // Diagonal from left top to middle bottom
            moveTo(left, top)
            lineTo(midX, bottom)

            // Diagonal from middle bottom to right top
            moveTo(midX, bottom)
            lineTo(right, top)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterN() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f

            // Left vertical spine
            moveTo(left, bottom)
            lineTo(left, top)

            // Right vertical spine
            moveTo(right, bottom)
            lineTo(right, top)

            // Diagonal
            moveTo(left, top)
            lineTo(right, bottom)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterO() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f

            // Draw an oval from top-left to bottom-right
            addOval(androidx.compose.ui.geometry.Rect(left, top, right, bottom))
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterP() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.7f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val midY = size.height * 0.5f

            // Vertical spine
            moveTo(left, top)
            lineTo(left, bottom)

            // Top curve
            moveTo(left, top)
            cubicTo(
                right, top,
                right, midY,
                left, midY
            )
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterQ() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f

            // Outer oval
            addOval(androidx.compose.ui.geometry.Rect(left, top, right, bottom))

            // Extended diagonal tail coming out of the circle
            moveTo(size.width * 0.55f, size.height * 0.65f)
            lineTo(size.width * 0.8f, size.height * 0.85f)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterR() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.7f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val midY = size.height * 0.5f

            // Vertical spine
            moveTo(left, top)
            lineTo(left, bottom)

            // Top curve (like P)
            moveTo(left, top)
            cubicTo(
                right, top,
                right, midY,
                left, midY
            )

            // Diagonal leg
            moveTo(left, midY)
            lineTo(right, bottom)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterS() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // Define bounds with padding
            val left = w * 0.2f
            val right = w * 0.8f
            val top = h * 0.15f
            val bottom = h * 0.85f

            // Define the "Belly" height (where the curve turns vertical)
            val topBellyY = h * 0.38f
            val bottomBellyY = h * 0.62f

            // 1. Start at Top-Right Tip
            moveTo(right, top + (h * 0.1f))

            // 2. Top Arch (Tip -> Left Side)
            cubicTo(
                right, top,              // Control 1: Top-Right Corner
                left, top,               // Control 2: Top-Left Corner
                left, topBellyY          // End: Left side of the spine
            )

            // 3. The Spine (Left Side -> Right Side)
            // This is the critical part for "Straightness".
            // We pull down from the left and up from the right to force a steep angle.
            cubicTo(
                left, h * 0.55f,         // Control 1: Pulls down vertically
                right, h * 0.45f,        // Control 2: Pulls up vertically
                right, bottomBellyY      // End: Right side of the spine
            )

            // 4. Bottom Arch (Right Side -> Bottom Tip)
            cubicTo(
                right, bottom,           // Control 1: Bottom-Right Corner
                left, bottom,            // Control 2: Bottom-Left Corner
                left, bottom - (h * 0.1f)// End: Bottom-Left Tip
            )
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterT() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f
            val centerX = size.width * 0.5f

            // Top horizontal bar
            moveTo(left, top)
            lineTo(right, top)

            // Vertical spine
            moveTo(centerX, top)
            lineTo(centerX, bottom)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterU() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f

            // Left vertical spine
            moveTo(left, top)
            lineTo(left, bottom)

            // Bottom curve from left to right (more pronounced)
            moveTo(left, bottom)
            cubicTo(
                left, bottom + size.height * 0.15f,            // deeper control point left
                right, bottom + size.height * 0.15f,           // deeper control point right
                right, bottom                                   // end at bottom-right
            )

            // Right vertical spine up
            moveTo(right, bottom)
            lineTo(right, top)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterV() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f

            // Left diagonal
            moveTo(left, top)
            lineTo((left + right) / 2, bottom) // midpoint at bottom

            // Right diagonal
            moveTo(right, top)
            lineTo((left + right) / 2, bottom)


        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterW() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // Define boundaries
            val left = w * 0.15f
            val right = w * 0.85f
            val top = h * 0.2f
            val bottom = h * 0.8f

            // Calculate the horizontal width of the W
            val totalWidth = right - left

            // A 'W' has 4 strokes, so we need points at 0%, 25%, 50%, 75%, 100% across
            val x1 = left
            val x2 = left + (totalWidth * 0.25f)
            val x3 = left + (totalWidth * 0.5f) // Center
            val x4 = left + (totalWidth * 0.75f)
            val x5 = right

            // 1. Start Top-Left
            moveTo(x1, top)

            // 2. Down to Bottom-Left
            lineTo(x2, bottom)

            // 3. Up to Middle-Top
            lineTo(x3, top)

            // 4. Down to Bottom-Right
            lineTo(x4, bottom)

            // 5. Up to Top-Right
            lineTo(x5, top)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterX() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f

            // Diagonal from top-left to bottom-right
            moveTo(left, top)
            lineTo(right, bottom)

            // Diagonal from top-right to bottom-left
            moveTo(right, top)
            lineTo(left, bottom)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterY() {
        val path = Path().apply {
            val left = size.width * 0.2f
            val right = size.width * 0.8f
            val top = size.height * 0.15f       // start higher
            val bottom = size.height * 0.85f    // extend lower
            val midX = (left + right) / 2
            val midY = size.height * 0.45f      // meeting point of diagonals

            // Left diagonal from top-left to middle
            moveTo(left, top)
            lineTo(midX, midY)

            // Right diagonal from top-right to middle
            moveTo(right, top)
            lineTo(midX, midY)

            // Vertical spine down from middle to bottom
            moveTo(midX, midY)
            lineTo(midX, bottom)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterZ() {
        val path = Path().apply {
            val left = size.width * 0.25f
            val right = size.width * 0.75f
            val top = size.height * 0.2f
            val bottom = size.height * 0.8f

            // Top horizontal
            moveTo(left, top)
            lineTo(right, top)

            // Diagonal from top-right to bottom-left
            moveTo(right, top)
            lineTo(left, bottom)

            // Bottom horizontal
            moveTo(left, bottom)
            lineTo(right, bottom)
        }

        drawPath(
            path = path,
            color = Color.LightGray,
            style = Stroke(
                width = 6f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 12f),
                    0f
                )
            )
        )
    }


}