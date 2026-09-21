package com.naicha.diary.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

@Composable
fun AnimatedNumber(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displaySmall,
    color: Color = MaterialTheme.colorScheme.onSurface,
    durationMillis: Int = 700,
) {
    val animated by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis),
        label = "animatedNumber",
    )
    Text(text = animated.toString(), modifier = modifier, style = style, color = color)
}
