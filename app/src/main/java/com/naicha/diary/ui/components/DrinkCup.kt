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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import com.naicha.diary.data.DrinkCategory
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.Outline
import com.naicha.diary.ui.theme.Pearl
import com.naicha.diary.ui.theme.Strawberry
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

enum class CupStyle { Tea, Coffee, Glass, Can }

fun cupStyleFor(category: DrinkCategory): CupStyle = when (category) {
    DrinkCategory.Tea -> CupStyle.Tea
    DrinkCategory.Coffee -> CupStyle.Coffee
    DrinkCategory.Alcohol -> CupStyle.Glass
    DrinkCategory.Other -> CupStyle.Can
}

@Composable
fun DrinkCup(
    modifier: Modifier = Modifier,
    liquidColor: Color = Caramel,
    strawColor: Color = Strawberry,
    toppingCount: Int = 6,
    animated: Boolean = true,
    style: CupStyle = CupStyle.Tea,
) {
    val phase = if (animated) {
        val transition = rememberInfiniteTransition(label = "cup")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing)),
            label = "cupPhase",
        )
        value
    } else {
        0f
    }

    val dots = remember(toppingCount) {
        val random = Random(toppingCount * 37 + 11)
        List(toppingCount) {
            Offset(random.nextFloat(), random.nextFloat()) to (0.028f + random.nextFloat() * 0.016f)
        }
    }

    Canvas(modifier = modifier) {
        when (style) {
            CupStyle.Tea -> drawTeaCup(liquidColor, strawColor, dots, phase)
            CupStyle.Coffee -> drawCoffeeCup(liquidColor, dots, phase)
            CupStyle.Glass -> drawWineGlass(liquidColor, dots, phase)
            CupStyle.Can -> drawCan(liquidColor, phase)
        }
    }
}

// ── 茶饮杯 ────────────────────────────────────────────────────────

private fun DrawScope.drawTeaCup(
    liquidColor: Color,
    strawColor: Color,
    pearls: List<Pair<Offset, Float>>,
    phase: Float,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val capTop = h * 0.20f
    val rimY = h * 0.30f
    val baseY = h * 0.95f
    val topHalf = w * 0.31f
    val botHalf = w * 0.235f

    drawGlow(cx, h * 0.55f, w * 0.62f, liquidColor)
    drawStraw(cx, topHalf, h, rimY, strawColor, phase)
    drawTumblerBody(cx, rimY, baseY, topHalf, botHalf)

    val liquidTop = rimY + (baseY - rimY) * 0.40f
    val liquidPath = taperPath(cx, rimY, baseY, topHalf, botHalf, w * 0.018f, liquidTop)
    drawLiquid(liquidPath, liquidTop, liquidColor, phase)

    clipPath(liquidPath) {
        drawPearls(cx, rimY, baseY, topHalf, botHalf, pearls, phase)
    }

    drawHighlight(cx, rimY, baseY, topHalf, botHalf)
    drawFlatCap(cx, capTop, rimY, topHalf)
    drawBaseShadow(cx, baseY, botHalf)
    drawSteam(cx, capTop, h, phase)
}

// ── 咖啡杯 ────────────────────────────────────────────────────────

private fun DrawScope.drawCoffeeCup(
    liquidColor: Color,
    bubbles: List<Pair<Offset, Float>>,
    phase: Float,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val lidTop = h * 0.18f
    val rimY = h * 0.31f
    val baseY = h * 0.92f
    val topHalf = w * 0.30f
    val botHalf = w * 0.225f

    drawGlow(cx, h * 0.52f, w * 0.60f, liquidColor)

    // 杯身
    val radius = botHalf * 0.34f
    val body = Path().apply {
        moveTo(cx - topHalf, rimY)
        lineTo(cx + topHalf, rimY)
        lineTo(cx + botHalf, baseY - radius)
        quadraticTo(cx + botHalf, baseY, cx + botHalf - radius, baseY)
        lineTo(cx - botHalf + radius, baseY)
        quadraticTo(cx - botHalf, baseY, cx - botHalf, baseY - radius)
        close()
    }
    drawPath(
        body,
        Brush.horizontalGradient(
            listOf(Color(0xFFF1EDE8), Color.White, Color(0xFFFBF8F4), Color(0xFFEAE3DA)),
        ),
    )

    // 咖啡液
    val liquidTop = rimY + (baseY - rimY) * 0.26f
    val liquidPath = taperPath(cx, rimY, baseY, topHalf, botHalf, w * 0.020f, liquidTop)
    clipPath(liquidPath) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(liquidColor.copy(alpha = 0.80f), liquidColor, liquidColor.copy(alpha = 0.90f)),
            ),
            topLeft = Offset(cx - topHalf, liquidTop),
            size = Size(topHalf * 2f, baseY - liquidTop),
        )
    }

    // 奶泡拉花
    val foamY = liquidTop
    clipPath(liquidPath) {
        drawRect(
            color = Color.White.copy(alpha = 0.55f),
            topLeft = Offset(cx - topHalf, foamY),
            size = Size(topHalf * 2f, h * 0.035f),
        )
        bubbles.take(5).forEachIndexed { index, (unit, factor) ->
            val r = w * factor * 0.9f
            val bob = sin(phase + index) * h * 0.006f
            drawCircle(
                color = Color.White.copy(alpha = 0.42f),
                radius = r,
                center = Offset(
                    cx + (unit.x - 0.5f) * topHalf * 1.4f,
                    foamY + h * 0.05f + unit.y * h * 0.12f + bob,
                ),
            )
        }
    }

    drawPath(body, Outline.copy(alpha = 0.9f), style = Stroke(width = w * 0.009f))

    // 纸杯套
    val sleeveTop = rimY + (baseY - rimY) * 0.34f
    val sleeveBottom = rimY + (baseY - rimY) * 0.78f
    val sleevePath = Path().apply {
        val tHalf = topHalf + (botHalf - topHalf) * ((sleeveTop - rimY) / (baseY - rimY))
        val bHalf = topHalf + (botHalf - topHalf) * ((sleeveBottom - rimY) / (baseY - rimY))
        moveTo(cx - tHalf, sleeveTop)
        lineTo(cx + tHalf, sleeveTop)
        lineTo(cx + bHalf, sleeveBottom)
        lineTo(cx - bHalf, sleeveBottom)
        close()
    }
    drawPath(
        sleevePath,
        Brush.verticalGradient(
            listOf(liquidColor.copy(alpha = 0.30f), liquidColor.copy(alpha = 0.42f)),
        ),
    )
    drawPath(sleevePath, liquidColor.copy(alpha = 0.35f), style = Stroke(width = w * 0.006f))

    drawHighlight(cx, rimY, baseY, topHalf, botHalf)

    // 杯盖
    val lidHalf = topHalf + w * 0.032f
    val lidHeight = (rimY - lidTop).coerceAtLeast(h * 0.07f)
    val lidRadius = lidHeight * 0.34f
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFFFFDFA), Color(0xFFF2E7D8))),
        topLeft = Offset(cx - lidHalf, lidTop),
        size = Size(lidHalf * 2f, lidHeight),
        cornerRadius = CornerRadius(lidRadius, lidRadius),
    )
    drawRoundRect(
        color = Outline.copy(alpha = 0.9f),
        topLeft = Offset(cx - lidHalf, lidTop),
        size = Size(lidHalf * 2f, lidHeight),
        cornerRadius = CornerRadius(lidRadius, lidRadius),
        style = Stroke(width = w * 0.009f),
    )
    // 饮用口
    drawOval(
        color = Color(0xFFDCCBB6),
        topLeft = Offset(cx + lidHalf * 0.18f, lidTop + lidHeight * 0.22f),
        size = Size(lidHalf * 0.52f, lidHeight * 0.34f),
    )

    drawBaseShadow(cx, baseY, botHalf)
    drawSteam(cx, lidTop, h, phase)
}

// ── 酒杯 ──────────────────────────────────────────────────────────

private fun DrawScope.drawWineGlass(
    liquidColor: Color,
    bubbles: List<Pair<Offset, Float>>,
    phase: Float,
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val topY = h * 0.10f
    val bowlBottom = h * 0.56f
    val halfTop = w * 0.29f

    drawGlow(cx, h * 0.40f, w * 0.60f, liquidColor)

    val bowl = Path().apply {
        moveTo(cx - halfTop, topY)
        lineTo(cx + halfTop, topY)
        quadraticTo(cx + halfTop * 0.94f, bowlBottom, cx + w * 0.02f, bowlBottom)
        lineTo(cx - w * 0.02f, bowlBottom)
        quadraticTo(cx - halfTop * 0.94f, bowlBottom, cx - halfTop, topY)
        close()
    }

    // 杯肚玻璃
    drawPath(
        bowl,
        Brush.horizontalGradient(
            listOf(
                Color(0x22FFFFFF), Color(0x55FFFFFF),
                Color(0x33FFFFFF), Color(0x22FFFFFF),
            ),
        ),
    )

    // 酒液
    val liquidTop = topY + (bowlBottom - topY) * 0.44f
    clipPath(bowl) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    liquidColor.copy(alpha = 0.78f),
                    liquidColor,
                    liquidColor.copy(alpha = 0.92f),
                ),
            ),
            topLeft = Offset(cx - halfTop, liquidTop),
            size = Size(halfTop * 2f, bowlBottom - liquidTop),
        )
        drawRect(
            color = Color.White.copy(alpha = 0.30f),
            topLeft = Offset(cx - halfTop, liquidTop),
            size = Size(halfTop * 2f, h * 0.014f),
        )
        bubbles.take(6).forEachIndexed { index, (unit, factor) ->
            val r = w * factor * 0.55f
            val bob = sin(phase * 1.4f + index * 1.1f) * h * 0.02f
            drawCircle(
                color = Color.White.copy(alpha = 0.34f),
                radius = r,
                center = Offset(
                    cx + (unit.x - 0.5f) * halfTop * 1.3f,
                    liquidTop + h * 0.06f + unit.y * h * 0.24f + bob,
                ),
            )
        }
    }

    drawPath(bowl, Color.White.copy(alpha = 0.55f), style = Stroke(width = w * 0.010f))

    // 高光
    drawPath(
        Path().apply {
            moveTo(cx - halfTop * 0.72f, topY + h * 0.05f)
            lineTo(cx - halfTop * 0.50f, topY + h * 0.05f)
            quadraticTo(cx - halfTop * 0.52f, bowlBottom - h * 0.06f, cx - w * 0.03f, bowlBottom - h * 0.02f)
            lineTo(cx - w * 0.06f, bowlBottom - h * 0.02f)
            quadraticTo(cx - halfTop * 0.80f, bowlBottom - h * 0.10f, cx - halfTop * 0.72f, topY + h * 0.05f)
            close()
        },
        color = Color.White.copy(alpha = 0.50f),
    )

    // 杯柄
    val stemTop = bowlBottom - h * 0.01f
    val stemBottom = h * 0.88f
    drawRoundRect(
        brush = Brush.horizontalGradient(
            listOf(Color(0x33FFFFFF), Color(0x77FFFFFF), Color(0x33FFFFFF)),
        ),
        topLeft = Offset(cx - w * 0.018f, stemTop),
        size = Size(w * 0.036f, stemBottom - stemTop),
        cornerRadius = CornerRadius(w * 0.018f, w * 0.018f),
    )

    // 底座
    drawOval(
        brush = Brush.horizontalGradient(
            listOf(Color(0x33FFFFFF), Color(0x88FFFFFF), Color(0x33FFFFFF)),
        ),
        topLeft = Offset(cx - w * 0.26f, stemBottom - h * 0.012f),
        size = Size(w * 0.52f, h * 0.040f),
    )
    drawOval(
        color = Color.White.copy(alpha = 0.45f),
        topLeft = Offset(cx - w * 0.26f, stemBottom - h * 0.012f),
        size = Size(w * 0.52f, h * 0.040f),
        style = Stroke(width = w * 0.008f),
    )
}

// ── 罐装 ──────────────────────────────────────────────────────────

private fun DrawScope.drawCan(liquidColor: Color, phase: Float) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val topY = h * 0.14f
    val bottomY = h * 0.93f
    val half = w * 0.26f
    val radius = half * 0.45f

    drawGlow(cx, h * 0.52f, w * 0.58f, liquidColor)

    val body = Path().apply {
        moveTo(cx - half, topY + radius)
        quadraticTo(cx - half, topY, cx - half + radius, topY)
        lineTo(cx + half - radius, topY)
        quadraticTo(cx + half, topY, cx + half, topY + radius)
        lineTo(cx + half, bottomY - radius)
        quadraticTo(cx + half, bottomY, cx + half - radius, bottomY)
        lineTo(cx - half + radius, bottomY)
        quadraticTo(cx - half, bottomY, cx - half, bottomY - radius)
        close()
    }

    drawPath(
        body,
        Brush.horizontalGradient(
            listOf(
                liquidColor.copy(alpha = 0.92f),
                Color.White.copy(alpha = 0.55f).compositeOver(liquidColor),
                liquidColor,
                liquidColor.copy(alpha = 0.80f),
            ),
            startX = cx - half,
            endX = cx + half,
        ),
    )

    // 拉环顶盖
    drawOval(
        color = Color(0xFFE4E4E4),
        topLeft = Offset(cx - half, topY - h * 0.030f),
        size = Size(half * 2f, h * 0.062f),
    )
    drawOval(
        color = Color(0xFFB9B9B9),
        topLeft = Offset(cx - half, topY - h * 0.030f),
        size = Size(half * 2f, h * 0.062f),
        style = Stroke(width = w * 0.008f),
    )
    drawOval(
        color = Color(0xFF9E9E9E),
        topLeft = Offset(cx - w * 0.055f, topY - h * 0.018f),
        size = Size(w * 0.110f, h * 0.034f),
    )
    drawCircle(
        color = Color(0xFF7A7A7A),
        radius = w * 0.020f,
        center = Offset(cx, topY - h * 0.001f),
    )

    // 高光
    val shine = Path().apply {
        moveTo(cx - half * 0.66f, topY + h * 0.10f)
        lineTo(cx - half * 0.46f, topY + h * 0.10f)
        lineTo(cx - half * 0.52f, bottomY - h * 0.10f)
        lineTo(cx - half * 0.74f, bottomY - h * 0.10f)
        close()
    }
    drawPath(shine, Color.White.copy(alpha = 0.42f))

    // 装饰横带
    val bandTop = topY + (bottomY - topY) * 0.52f + sin(phase) * h * 0.004f
    clipPath(body) {
        drawRect(
            color = Color.White.copy(alpha = 0.22f),
            topLeft = Offset(cx - half, bandTop),
            size = Size(half * 2f, h * 0.055f),
        )
    }

    drawPath(body, Color.Black.copy(alpha = 0.10f), style = Stroke(width = w * 0.008f))

    drawBaseShadow(cx, bottomY, half)
}

// ── 通用绘制 ──────────────────────────────────────────────────────

private fun taperPath(
    cx: Float,
    rimY: Float,
    baseY: Float,
    topHalf: Float,
    botHalf: Float,
    inset: Float,
    top: Float,
): Path {
    val radius = botHalf * 0.30f
    return Path().apply {
        moveTo(cx - topHalf + inset, top)
        lineTo(cx + topHalf - inset, top)
        lineTo(cx + botHalf - inset, baseY - inset - radius)
        quadraticTo(cx + botHalf - inset, baseY - inset, cx + botHalf - inset - radius, baseY - inset)
        lineTo(cx - botHalf + inset + radius, baseY - inset)
        quadraticTo(cx - botHalf + inset, baseY - inset, cx - botHalf + inset, baseY - inset - radius)
        close()
    }
}

private fun DrawScope.drawLiquid(path: Path, top: Float, color: Color, phase: Float) {
    drawPath(
        path,
        Brush.verticalGradient(
            listOf(color.copy(alpha = 0.72f), color, color.copy(alpha = 0.88f)),
        ),
    )
    drawPath(path, color.copy(alpha = 0.55f), style = Stroke(width = size.width * 0.006f))
    val wave = sin(phase * 1.6f) * size.height * 0.005f
    drawRect(
        color = Color.White.copy(alpha = 0.26f),
        topLeft = Offset(0f, top + wave),
        size = Size(size.width, size.height * 0.020f),
    )
}

private fun DrawScope.drawGlow(cx: Float, cy: Float, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0f)),
            center = Offset(cx, cy),
            radius = radius,
        ),
        radius = radius,
        center = Offset(cx, cy),
    )
}

private fun DrawScope.drawStraw(
    cx: Float,
    topHalf: Float,
    h: Float,
    rimY: Float,
    strawColor: Color,
    phase: Float,
) {
    val topX = cx + topHalf * 0.42f
    val bottomX = cx + topHalf * 0.06f
    val topY = h * 0.015f
    val width = size.width * 0.072f
    val wobble = sin(phase) * size.width * 0.006f

    val path = Path().apply {
        moveTo(topX - width / 2f + wobble, topY)
        lineTo(topX + width / 2f + wobble, topY)
        lineTo(bottomX + width / 2f, rimY + h * 0.06f)
        lineTo(bottomX - width / 2f, rimY + h * 0.06f)
        close()
    }
    drawPath(path, strawColor)
    drawPath(
        path = Path().apply {
            moveTo(topX - width / 2f + wobble, topY)
            lineTo(topX - width / 6f + wobble, topY)
            lineTo(bottomX - width / 6f, rimY + h * 0.06f)
            lineTo(bottomX - width / 2f, rimY + h * 0.06f)
            close()
        },
        color = Color.White.copy(alpha = 0.35f),
    )
    drawPath(path, Color.Black.copy(alpha = 0.06f), style = Stroke(width = size.width * 0.006f))
}

private fun DrawScope.drawTumblerBody(
    cx: Float,
    rimY: Float,
    baseY: Float,
    topHalf: Float,
    botHalf: Float,
) {
    val radius = botHalf * 0.34f
    val path = Path().apply {
        moveTo(cx - topHalf, rimY)
        lineTo(cx + topHalf, rimY)
        lineTo(cx + botHalf, baseY - radius)
        quadraticTo(cx + botHalf, baseY, cx + botHalf - radius, baseY)
        lineTo(cx - botHalf + radius, baseY)
        quadraticTo(cx - botHalf, baseY, cx - botHalf, baseY - radius)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFF3F0EC), Color.White, Color(0xFFFBF8F4), Color(0xFFEDE7E0),
            ),
        ),
    )
    drawPath(path, Outline.copy(alpha = 0.9f), style = Stroke(width = size.width * 0.009f))
}

private fun DrawScope.drawPearls(
    cx: Float,
    rimY: Float,
    baseY: Float,
    topHalf: Float,
    botHalf: Float,
    pearls: List<Pair<Offset, Float>>,
    phase: Float,
) {
    val areaTop = rimY + (baseY - rimY) * 0.58f
    val areaBottom = baseY - size.height * 0.035f
    val areaHeight = areaBottom - areaTop

    pearls.forEachIndexed { index, (unit, radiusFactor) ->
        val radius = size.width * radiusFactor
        val bob = sin(phase + index * 0.9f) * size.height * 0.008f
        val y = areaTop + unit.y * areaHeight + bob
        val spread = botHalf + (topHalf - botHalf) * ((baseY - y) / (baseY - rimY))
        val x = cx + (unit.x - 0.5f) * (spread - radius) * 1.55f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6B4F3D), Pearl),
                center = Offset(x - radius * 0.3f, y - radius * 0.3f),
                radius = radius * 1.5f,
            ),
            radius = radius,
            center = Offset(x, y),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.42f),
            radius = radius * 0.26f,
            center = Offset(x - radius * 0.34f, y - radius * 0.36f),
        )
    }
}

private fun DrawScope.drawHighlight(
    cx: Float,
    rimY: Float,
    baseY: Float,
    topHalf: Float,
    botHalf: Float,
) {
    val path = Path().apply {
        moveTo(cx - topHalf * 0.72f, rimY + size.height * 0.06f)
        lineTo(cx - topHalf * 0.52f, rimY + size.height * 0.06f)
        lineTo(cx - botHalf * 0.48f, baseY - size.height * 0.09f)
        quadraticTo(
            cx - botHalf * 0.60f, baseY - size.height * 0.03f,
            cx - botHalf * 0.74f, baseY - size.height * 0.08f,
        )
        close()
    }
    drawPath(path, Color.White.copy(alpha = 0.55f))
}

private fun DrawScope.drawFlatCap(cx: Float, capTop: Float, rimY: Float, topHalf: Float) {
    val capHalf = topHalf + size.width * 0.035f
    val capHeight = (rimY - capTop).coerceAtLeast(size.height * 0.06f)
    val radius = capHeight * 0.42f

    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFFFFDFA), Color(0xFFF6EADB))),
        topLeft = Offset(cx - capHalf, capTop),
        size = Size(capHalf * 2f, capHeight),
        cornerRadius = CornerRadius(radius, radius),
    )
    drawRoundRect(
        color = Outline.copy(alpha = 0.9f),
        topLeft = Offset(cx - capHalf, capTop),
        size = Size(capHalf * 2f, capHeight),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = size.width * 0.009f),
    )
    drawRoundRect(
        color = Color(0xFFE8D6C0),
        topLeft = Offset(cx - capHalf + size.width * 0.02f, capTop + capHeight * 0.70f),
        size = Size(capHalf * 2f - size.width * 0.04f, capHeight * 0.24f),
        cornerRadius = CornerRadius(radius * 0.5f, radius * 0.5f),
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = capHeight * 0.16f,
        center = Offset(cx - capHalf * 0.55f, capTop + capHeight * 0.36f),
    )
}

private fun DrawScope.drawBaseShadow(cx: Float, baseY: Float, botHalf: Float) {
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.Black.copy(alpha = 0.14f), Color.Transparent),
            center = Offset(cx, baseY + size.height * 0.012f),
            radius = botHalf * 1.5f,
        ),
        topLeft = Offset(cx - botHalf * 1.35f, baseY - size.height * 0.005f),
        size = Size(botHalf * 2.7f, size.height * 0.045f),
    )
}

private fun DrawScope.drawSteam(cx: Float, capTop: Float, h: Float, phase: Float) {
    repeat(3) { index ->
        val offset = sin(phase * 1.2f + index * 1.7f)
        val x = cx - size.width * 0.10f + index * size.width * 0.10f + offset * size.width * 0.02f
        val baseY = capTop - h * 0.03f
        val length = h * 0.09f * (0.7f + 0.3f * sin(phase + index))
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0f), Color.White.copy(alpha = 0.45f)),
                startY = baseY - length,
                endY = baseY,
            ),
            start = Offset(x, baseY - length),
            end = Offset(x + offset * size.width * 0.02f, baseY),
            strokeWidth = size.width * 0.022f,
            cap = StrokeCap.Round,
        )
    }
}
