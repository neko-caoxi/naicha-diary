package com.naicha.diary.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.Matcha
import com.naicha.diary.ui.theme.Strawberry
import com.naicha.diary.ui.theme.Taro
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class Blob(
    val x: Float,
    val y: Float,
    val scale: Float,
    val color: Color,
)

@Composable
fun PearlField(
    modifier: Modifier = Modifier,
    count: Int = 16,
    seed: Int = 7,
    alpha: Float = 1.6f,
) {
    val transition = rememberInfiniteTransition(label = "field")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "fieldPhase",
    )

    val blobs = remember(seed, count) {
        val random = Random(seed)
        val palette = listOf(Caramel, Taro, Matcha, Strawberry)
        List(count) {
            Blob(
                x = random.nextFloat(),
                y = random.nextFloat(),
                scale = random.nextFloat(),
                color = palette[random.nextInt(palette.size)],
            )
        }
    }

    Canvas(modifier = modifier) {
        blobs.forEachIndexed { index, blob ->
            val radius = size.minDimension * (0.02f + blob.scale * 0.05f)
            val bob = sin(phase + index * 0.7f) * size.height * 0.03f
            val drift = sin(phase * 0.6f + index * 1.3f) * size.width * 0.02f
            val center = Offset(blob.x * size.width + drift, blob.y * size.height + bob)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        blob.color.copy(alpha = 0.22f * alpha),
                        blob.color.copy(alpha = 0f),
                    ),
                    center = center,
                    radius = radius * 2.6f,
                ),
                radius = radius * 2.6f,
                center = center,
            )
            drawCircle(
                color = blob.color.copy(alpha = 0.16f * alpha),
                radius = radius * 0.5f,
                center = center,
            )
        }
    }
}
