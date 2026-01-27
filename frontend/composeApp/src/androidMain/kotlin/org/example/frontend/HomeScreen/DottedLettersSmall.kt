import androidx.lifecycle.ViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke


class lettersViewModelSmall : ViewModel() {
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLettera() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // Start near top of oval
            moveTo(w * 0.55f, h * 0.30f)

            // Left upper curve
            cubicTo(
                w * 0.35f, h * 0.20f,
                w * 0.20f, h * 0.40f,
                w * 0.28f, h * 0.58f
            )

            // Bottom curve
            cubicTo(
                w * 0.35f, h * 0.78f,
                w * 0.65f, h * 0.78f,
                w * 0.70f, h * 0.55f
            )

            // Close oval at top
            cubicTo(
                w * 0.75f, h * 0.40f,
                w * 0.65f, h * 0.30f,
                w * 0.55f, h * 0.32f
            )

            // Smooth exit into tail (important)
            cubicTo(
                w * 0.78f, h * 0.45f,
                w * 0.78f, h * 0.75f,
                w * 0.62f, h * 0.82f
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

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterb() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // 1. Shifted stem to left (0.3f) to make room for a bigger belly
            // 2. Started higher (0.05f) for a longer ascender
            moveTo(w * 0.30f, h * 0.05f)

            // 3. Went lower (0.90f) for a longer stem
            lineTo(w * 0.30f, h * 0.90f)

            // 4. Belly: Bottom curve (Starting from bottom of stem, going wide right)
            cubicTo(
                w * 0.30f, h * 0.90f, // Start anchor
                w * 0.90f, h * 0.90f, // Control point: Pull wide to the right immediately
                w * 0.90f, h * 0.70f  // Destination: Middle-right of the belly
            )

            // 5. Belly: Top curve (Curving back into the middle of the stem)
            cubicTo(
                w * 0.90f, h * 0.50f, // Control point: Keep top right round
                w * 0.60f, h * 0.50f, // Control point: Guide into the stem
                w * 0.30f, h * 0.55f  // Destination: Connect back to stem at ~55% height
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterc() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // Start at the top-right opening
            moveTo(w * 0.85f, h * 0.45f)

            // Curve 1: Top arch (Right -> Left -> Down)
            // Pulls up and left to create the top rounded shape
            cubicTo(
                w * 0.70f, h * 0.30f,  // Control point 1: Arch up
                w * 0.25f, h * 0.30f,  // Control point 2: Arch far left
                w * 0.25f, h * 0.65f   // Destination: Middle-left (spine of the c)
            )

            // Curve 2: Bottom arch (Down -> Right -> Up)
            // Pulls down and right to create the bottom cup
            cubicTo(
                w * 0.25f, h * 0.95f,  // Control point 1: Arch down
                w * 0.70f, h * 0.95f,  // Control point 2: Arch right
                w * 0.85f, h * 0.80f   // Destination: Bottom-right opening
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterd() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // 1. Start at the top of the stem (Long ascender)
            moveTo(w * 0.70f, h * 0.05f)

            // 2. Draw the stem straight down
            lineTo(w * 0.70f, h * 0.90f)

            // 3. Draw the belly (Looping backwards from bottom to middle)
            // This creates a continuous line from the stem into the curve
            cubicTo(
                w * 0.15f, h * 0.90f, // Control 1: Pull wide to the bottom-left
                w * 0.15f, h * 0.50f, // Control 2: Pull wide to the top-left
                w * 0.70f, h * 0.50f  // Destination: Connect back to the stem at x-height
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

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLettere() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // 1. Start at the left side of the horizontal "crossbar"
            moveTo(w * 0.25f, h * 0.55f)

            // 2. Draw the crossbar straight to the right
            lineTo(w * 0.85f, h * 0.55f)

            // 3. Top Loop: Curve up and back to the left
            // This forms the top half of the 'e'
            cubicTo(
                w * 0.90f, h * 0.30f, // Control 1: Pull up-right to open the loop
                w * 0.25f, h * 0.30f, // Control 2: Pull up-left to round the top
                w * 0.25f, h * 0.60f  // Destination: Back to the left spine
            )

            // 4. Bottom Cup: Curve down and to the right (like a 'c')
            cubicTo(
                w * 0.25f, h * 0.95f, // Control 1: Pull deep down-left
                w * 0.75f, h * 0.95f, // Control 2: Pull deep down-right
                w * 0.85f, h * 0.80f  // Destination: Bottom right tail
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

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterf() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // 1. The Stem: Straight vertical line from top to bottom
            // Centered horizontally at 0.50w
            moveTo(w * 0.50f, h * 0.05f) // Start at top ascender height
            lineTo(w * 0.50f, h * 0.90f) // End at baseline

            // 2. The Crossbar: Horizontal line across the stem
            // Placed at 0.40h (same as previous version)
            moveTo(w * 0.30f, h * 0.40f) // Left side
            lineTo(w * 0.70f, h * 0.40f) // Right side
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterg() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // --- 1. The Belly (A circle sitting on the baseline) ---
            // Start at the top-right of the belly
            moveTo(w * 0.75f, h * 0.35f)

            // Curve Left and Down (Left side of circle)
            cubicTo(
                w * 0.25f, h * 0.35f, // Control: Top-Left
                w * 0.25f, h * 0.65f, // Control: Bottom-Left
                w * 0.50f, h * 0.65f  // Destination: Bottom-Middle of belly
            )

            // Curve Right and Up (Closing the circle back to the stem)
            cubicTo(
                w * 0.65f, h * 0.65f, // Control: Bottom-Right
                w * 0.75f, h * 0.55f, // Control: Mid-Right
                w * 0.75f, h * 0.35f  // Destination: Back to start
            )

            // --- 2. The Tail (Descender) ---
            // Go straight down from the top of the belly
            lineTo(w * 0.75f, h * 0.85f)

            // Curve the tail to the left
            cubicTo(
                w * 0.75f, h * 0.98f, // Control: Deep down
                w * 0.45f, h * 0.98f, // Control: Hooking left
                w * 0.35f, h * 0.90f  // Destination: Tip of the tail
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterh() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // 1. The Stem: Long vertical line
            // Start high and curve slightly to the baseline
            moveTo(w * 0.30f, h * 0.10f)
            quadraticBezierTo(
                w * 0.28f, h * 0.50f, // Control point for a slight curve
                w * 0.30f, h * 0.90f  // Destination at the baseline
            )

            // 2. The Arch and Hook
            // Start from the middle of the stem
            moveTo(w * 0.30f, h * 0.50f)

            // First part of the arch: Curve up and over
            cubicTo(
                w * 0.35f, h * 0.25f, // Control 1: Pull up and right
                w * 0.75f, h * 0.25f, // Control 2: Round out the top
                w * 0.75f, h * 0.60f  // Destination: Start of the leg down
            )

            // Second part: Curve down and hook back up
            cubicTo(
                w * 0.75f, h * 0.85f, // Control 1: Continue down
                w * 0.82f, h * 0.98f, // Control 2: Bottom of the hook (low and wide)
                w * 0.95f, h * 0.80f  // Destination: Tip of the hook (curving up)
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetteri() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // --- 1. The Dot ---
            // A small circle or short line at the top
            // Moving the cursor to the dot position creates a separate contour
            addOval(
                androidx.compose.ui.geometry.Rect(
                    center = androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.20f),
                    radius = w * 0.05f
                )
            )

            // --- 2. The Stem with a Hook ---
            // Start below the dot
            moveTo(w * 0.50f, h * 0.40f)

            // Draw down and hook slightly to the right
            quadraticBezierTo(
                w * 0.50f, h * 0.85f, // Control: Go straight down first
                w * 0.65f, h * 0.90f  // Destination: Curve right at the bottom
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

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterj() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // --- 1. The Dot ---
            // Aligned with the new stem position (0.75w)
            addOval(
                androidx.compose.ui.geometry.Rect(
                    center = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.20f),
                    radius = w * 0.05f
                )
            )

            // --- 2. The Stem ---
            // Start at x-height
            moveTo(w * 0.75f, h * 0.40f)

            // Go down to near the bottom before curving
            lineTo(w * 0.75f, h * 0.80f)

            // --- 3. The Big Round Belly ---
            // A large, wide scoop to the left
            cubicTo(
                w * 0.75f, h * 1.05f, // Control 1: Go deep down (below baseline) for roundness
                w * 0.25f, h * 1.05f, // Control 2: Stretch far left (wide belly)
                w * 0.25f, h * 0.80f  // Destination: End high on the left
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterk() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // 1. The Stem: Long vertical line (Ascender)
            // Starts high (0.05) and goes to baseline (0.90)
            moveTo(w * 0.30f, h * 0.05f)
            lineTo(w * 0.30f, h * 0.90f)

            // 2. The Upper Arm
            // Start from the right and draw IN to the stem
            // This creates a nice "open" angle
            moveTo(w * 0.80f, h * 0.35f) // Top-right tip
            lineTo(w * 0.30f, h * 0.60f) // Connect to stem below the middle

            // 3. The Lower Leg
            // Start from the junction of the arm and stem
            moveTo(w * 0.45f, h * 0.52f) // Start slightly out on the arm stroke
            lineTo(w * 0.85f, h * 0.90f) // Kick out to bottom-right
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

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterl() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // 1. The Stem: Long vertical line (Ascender)
            // Start high (0.05) and go straight down
            moveTo(w * 0.50f, h * 0.05f)
            lineTo(w * 0.50f, h * 0.85f)

            // 2. The Tail: A gentle hook to the right
            quadraticBezierTo(
                w * 0.50f, h * 0.98f, // Control: Curve down and round
                w * 0.65f, h * 0.90f  // Destination: Tip of the tail
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

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterm() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // --- 1. First Stem (Left) ---
            // Start at x-height and go down
            moveTo(w * 0.15f, h * 0.40f)
            lineTo(w * 0.15f, h * 0.90f)

            // --- 2. First Arch (Middle Hump) ---
            // Start from the middle of the first stem
            moveTo(w * 0.15f, h * 0.60f)

            // Curve up, over, and down to the middle stem
            cubicTo(
                w * 0.20f, h * 0.35f, // Control 1: Up-Right
                w * 0.50f, h * 0.35f, // Control 2: Round Top
                w * 0.50f, h * 0.90f  // Destination: Bottom of Middle Stem
            )

            // --- 3. Second Arch (Right Hump) ---
            // Start from the middle of the middle stem
            moveTo(w * 0.50f, h * 0.60f)

            // Curve up, over, and down to the final stem
            cubicTo(
                w * 0.55f, h * 0.35f, // Control 1: Up-Right
                w * 0.85f, h * 0.35f, // Control 2: Round Top
                w * 0.85f, h * 0.90f  // Destination: Bottom of Right Stem
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLettern() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // 1. The Left Stem
            // Start at x-height (0.40) and go down to baseline (0.90)
            moveTo(w * 0.25f, h * 0.40f)
            lineTo(w * 0.25f, h * 0.90f)

            // 2. The Arch
            // Retrace up the stem slightly to start the curve (0.60)
            moveTo(w * 0.25f, h * 0.60f)

            // Curve up, over to the right, and down
            cubicTo(
                w * 0.30f, h * 0.35f, // Control 1: Pull Up-Right
                w * 0.75f, h * 0.35f, // Control 2: Round out the top
                w * 0.75f, h * 0.90f  // Destination: Bottom of the right leg
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

    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterp() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // 1. The Long Stem (High in canvas)
            // Starts at 0.10f and ends at 0.85f
            moveTo(w * 0.30f, h * 0.10f)
            lineTo(w * 0.30f, h * 0.85f)

            // 2. The Compact Belly (Small Height)
            // Starts lower (0.30f) and ends higher (0.55f) to reduce vertical size
            moveTo(w * 0.30f, h * 0.30f)

            // Top Curve (Flatter)
            cubicTo(
                w * 0.30f, h * 0.25f, // Control 1: Slight arch up
                w * 0.75f, h * 0.25f, // Control 2: Top right corner
                w * 0.75f, h * 0.42f  // Destination: Middle right
            )

            // Bottom Curve (Flatter)
            cubicTo(
                w * 0.75f, h * 0.60f, // Control 1: Bottom right corner
                w * 0.30f, h * 0.60f, // Control 2: Back toward stem
                w * 0.30f, h * 0.55f  // Destination: Connect back to stem
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterq() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // --- 1. The Small Belly ---
            // Situated in the visual center (Vertical: 0.40 to 0.60)
            // Start at the top of the belly (Stem junction)
            moveTo(w * 0.55f, h * 0.40f)

            // Curve Left (tightly) and back to the stem
            cubicTo(
                w * 0.35f, h * 0.40f, // Control 1: Pull slightly left
                w * 0.35f, h * 0.60f, // Control 2: Round bottom left
                w * 0.55f, h * 0.60f  // Destination: Bottom connection
            )

            // --- 2. The Stem (Descender) ---
            // Start at the top of the belly
            moveTo(w * 0.55f, h * 0.40f)

            // Go straight down
            lineTo(w * 0.55f, h * 0.85f)

            // --- 3. The Long Sharp Tail ---
            // Kick sharply up and to the far right
            // Extends from bottom (0.85) up to (0.60)
            lineTo(w * 0.85f, h * 0.60f)
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetteru() {
        val path = Path().apply {
            val w = size.width
            val h = size.height

            // --- 1. The Deep Cup Shape ---
            // Start at top left
            moveTo(w * 0.35f, h * 0.40f)

            // Draw further down before starting the curve
            lineTo(w * 0.35f, h * 0.65f)

            // Deep Curve (The Belly)
            // We pull the control points down to 0.80f to "weight" the bottom
            cubicTo(
                w * 0.35f, h * 0.80f, // Control 1: Deep left
                w * 0.65f, h * 0.80f, // Control 2: Deep right
                w * 0.65f, h * 0.65f  // Destination: Bottom of right stem
            )

            // --- 2. The Right Stem ---
            // Vertical line from top to bottom
            moveTo(w * 0.65f, h * 0.40f)
            lineTo(w * 0.65f, h * 0.75f) // Ends at the base of the deep belly
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetters() {
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
    fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDottedLetterz() {
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