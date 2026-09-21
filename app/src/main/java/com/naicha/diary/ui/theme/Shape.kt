package com.naicha.diary.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val DrinkShapes = Shapes(
    extraSmall = RoundedCornerShape(14.dp),
    small = RoundedCornerShape(20.dp),
    medium = RoundedCornerShape(26.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

/** iOS 风格的连续圆角（squircle），比普通圆角更圆润自然 */
class SquircleShape(private val cornerFraction: Float = 0.32f) : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density,
    ): androidx.compose.ui.graphics.Outline {
        val w = size.width
        val h = size.height
        val r = size.minDimension * cornerFraction
        val c = r * 0.45f
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(c, 0f)
            lineTo(w - c, 0f)
            cubicTo(w - c * 0.35f, 0f, w, c * 0.35f, w, c)
            lineTo(w, h - c)
            cubicTo(w, h - c * 0.35f, w - c * 0.35f, h, w - c, h)
            lineTo(c, h)
            cubicTo(c * 0.35f, h, 0f, h - c * 0.35f, 0f, h - c)
            lineTo(0f, c)
            cubicTo(0f, c * 0.35f, c * 0.35f, 0f, c, 0f)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}
