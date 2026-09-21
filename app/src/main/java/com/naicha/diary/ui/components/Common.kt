package com.naicha.diary.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.Outline
import com.naicha.diary.ui.theme.PearlSoft
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surface,
    elevation: Dp = 5.dp,
    shadowColor: Color = Caramel,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = shadowColor.copy(alpha = 0.30f),
                spotColor = shadowColor.copy(alpha = 0.30f),
            )
            .clip(shape)
            .background(color)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun TagChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    accent: Color = Caramel,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) accent else MaterialTheme.colorScheme.surfaceContainerLow
    val fg = if (selected) Color.White else PearlSoft
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
fun RatingStars(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 22.dp,
    accent: Color = Color(0xFFFFB300),
    onRate: ((Int) -> Unit)? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { index ->
            StarShape(
                filled = index <= rating,
                color = accent,
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (onRate != null) Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onRate(index) } else Modifier
                    ),
            )
            if (index != 5) Spacer(Modifier.width(2.dp))
        }
    }
}

@Composable
private fun StarShape(filled: Boolean, color: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val path = starPath(size.minDimension, size.minDimension)
        if (filled) {
            drawPath(path, color)
        } else {
            drawPath(path, color.copy(alpha = 0.22f))
        }
    }
}

fun starPath(width: Float, height: Float): Path {
    val cx = width / 2f
    val cy = height / 2f
    val outer = minOf(width, height) / 2f
    val inner = outer * 0.45f
    val path = Path()
    for (i in 0 until 10) {
        val angle = -PI / 2 + i * PI / 5
        val radius = if (i % 2 == 0) outer else inner
        val x = cx + (radius * cos(angle)).toFloat()
        val y = cy + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}

@Composable
fun BrandBadge(
    badge: String,
    color: Color,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
    logo: Int = 0,
    squircle: Boolean = false,
) {
    val shape: Shape = if (squircle) RoundedCornerShape(size * 0.30f) else CircleShape
    val deep = lerp(color, Color.Black, 0.22f)
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = size * 0.10f,
                shape = shape,
                ambientColor = color.copy(alpha = 0.45f),
                spotColor = color.copy(alpha = 0.45f),
            )
            .clip(shape)
            .background(Color.White)
            .border(0.6.dp, Color(0x14000000), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (logo != 0) {
            Image(
                painter = painterResource(logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(color, deep))),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                            )
                        )
                )
            }
            Text(
                text = badge,
                fontSize = (size.value * if (badge.length > 1) 0.33f else 0.43f).sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
fun BrandLogo(
    logo: Int,
    color: Color,
    badge: String,
    size: Dp,
    modifier: Modifier = Modifier,
    squircle: Boolean = false,
) {
    val shape: Shape = if (squircle) RoundedCornerShape(size * 0.30f) else CircleShape
    if (logo != 0) {
        Box(
            modifier = modifier
                .size(size)
                .shadow(
                    elevation = size * 0.08f,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.25f),
                )
                .clip(shape)
                .background(Color.White)
                .border(0.8.dp, Color(0x18000000), shape),
        ) {
            Image(
                painter = painterResource(logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(
                    Brush.linearGradient(listOf(color, lerp(color, Color.Black, 0.22f)))
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = badge,
                fontSize = (size.value * 0.40f).sp,
                color = Color.White,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: List<Color> = listOf(Caramel, Color(0xFFE0A574)),
    leading: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.955f else 1f,
        animationSpec = spring(stiffness = 900f, dampingRatio = 0.6f),
        label = "btnScale",
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.5f
            }
            .shadow(
                elevation = if (pressed) 3.dp else 10.dp,
                shape = RoundedCornerShape(50),
                ambientColor = colors.first().copy(alpha = 0.5f),
                spotColor = colors.first().copy(alpha = 0.5f),
            )
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    colors = if (enabled) colors else colors.map { it.copy(alpha = 0.5f) },
                )
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 26.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(accent.copy(alpha = 0.13f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = accent,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun BouncyBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 900f, dampingRatio = 0.65f),
        label = "bouncy",
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        content = content,
    )
}

@Composable
fun HairLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Outline.copy(alpha = 0.7f)),
    )
}

@Composable
fun DotIndicator(count: Int, selected: Int, accent: Color = Caramel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (index == selected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (index == selected) accent else Outline)
            )
        }
    }
}
