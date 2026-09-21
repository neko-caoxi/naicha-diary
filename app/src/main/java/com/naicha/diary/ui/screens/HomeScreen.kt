package com.naicha.diary.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naicha.diary.data.Catalog
import com.naicha.diary.data.Drink
import com.naicha.diary.data.DrinkCategory
import com.naicha.diary.ui.components.AnimatedNumber
import com.naicha.diary.ui.components.BouncyBox
import com.naicha.diary.ui.components.BrandBadge
import com.naicha.diary.ui.components.DrinkCup
import com.naicha.diary.ui.components.cupStyleFor
import com.naicha.diary.ui.components.PearlField
import com.naicha.diary.ui.components.RatingStars
import com.naicha.diary.ui.components.SectionHeader
import com.naicha.diary.ui.components.SoftCard
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.PearlSoft
import com.naicha.diary.ui.theme.Strawberry
import com.naicha.diary.ui.theme.Taro
import com.naicha.diary.util.TimeUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    items: List<Drink>,
    onRecord: () -> Unit,
    onAiRecord: () -> Unit,
    onOpenWall: () -> Unit,
    onOpenDetail: (Drink) -> Unit,
    modifier: Modifier = Modifier,
) {
    val todayItems = remember(items) {
        items.filter { TimeUtil.isToday(it.timestamp) }
    }
    val todayCount = todayItems.size
    val todayCalories = todayItems.sumOf { it.calories }
    val todaySpend = todayItems.sumOf { it.price }
    val recent = remember(items) { items.sortedByDescending { it.timestamp }.take(30) }
    val dateLabel = remember {
        SimpleDateFormat("M月d日 EEEE", Locale.CHINA).format(Date())
    }

    Box(modifier.fillMaxSize()) {
        PearlField(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            count = 14,
            seed = 3,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 130.dp),
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 22.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "${TimeUtil.greeting()} 🧋",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                HeroCard(
                    todayCount = todayCount,
                    todayCalories = todayCalories,
                    todaySpend = todaySpend,
                    todayStyle = todayItems.maxByOrNull { it.timestamp }
                        ?.categoryType ?: DrinkCategory.Tea,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                )
            }

            item {
                QuickActions(
                    onRecord = onRecord,
                    onAiRecord = onAiRecord,
                    onOpenWall = onOpenWall,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                )
            }

            item {
                SectionHeader(
                    title = "最近喝过",
                    subtitle = if (recent.isEmpty()) "第一杯，从今天开始" else "共 ${items.size} 杯回忆",
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                )
            }

            if (recent.isEmpty()) {
                item { EmptyHint(onRecord = onRecord) }
            } else {
                items(recent, key = { it.id }) { tea ->
                    TeaRow(
                        tea = tea,
                        onClick = { onOpenDetail(tea) },
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    todayCount: Int,
    todayCalories: Int,
    todaySpend: Double,
    todayStyle: DrinkCategory,
    modifier: Modifier = Modifier,
) {
    val liquid = when {
        todayCount == 0 -> Color(0xFFD9C3AE)
        todayCount == 1 -> Caramel
        todayCount == 2 -> Taro
        else -> Strawberry
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 18.dp,
                shape = MaterialTheme.shapes.extraLarge,
                ambientColor = Caramel.copy(alpha = 0.55f),
                spotColor = Caramel.copy(alpha = 0.55f),
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFEFBE93),
                        Color(0xFFD89B69),
                        Color(0xFFC0824F),
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                )
            ),
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                    ),
                    CircleShape,
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 6.dp, top = 20.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (todayCount == 0) "今天还没喝呢" else "今天已经喝了",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.92f),
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    AnimatedNumber(
                        value = todayCount,
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 62.sp),
                        color = Color.White,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "杯",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    MiniStat(
                        label = "热量",
                        value = "${todayCalories}kcal",
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    MiniStat(
                        label = "花费",
                        value = "¥${formatMoney(todaySpend)}",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            DrinkCup(
                modifier = Modifier
                    .width(104.dp)
                    .height(132.dp),
                liquidColor = liquid,
                strawColor = if (todayCount == 0) Color(0xFFB9A896) else Strawberry,
                toppingCount = (3 + todayCount * 2).coerceAtMost(10),
                style = cupStyleFor(todayStyle),
            )
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.20f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.78f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun QuickActions(
    onRecord: () -> Unit,
    onAiRecord: () -> Unit,
    onOpenWall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        AiBanner(onClick = onAiRecord)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ActionTile(
                title = "手动记",
                subtitle = "自己填更准",
                emoji = "✍️",
                colors = listOf(Caramel, Color(0xFFE2A776)),
                onClick = onRecord,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            ActionTile(
                title = "饮品墙",
                subtitle = "看看攒了多少",
                emoji = "🧱",
                colors = listOf(Taro, Color(0xFFCBBAF5)),
                onClick = onOpenWall,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AiBanner(onClick: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "ai")
    val shift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            tween(2600, easing = LinearEasing),
            RepeatMode.Restart,
        ),
        label = "aiShift",
    )

    BouncyBox(onClick = onClick) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = MaterialTheme.shapes.large,
                    ambientColor = Taro.copy(alpha = 0.55f),
                    spotColor = Taro.copy(alpha = 0.55f),
                )
                .clip(MaterialTheme.shapes.large)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF6C5CE7), Color(0xFF8E7CF0), Color(0xFFB79CED))
                    )
                ),
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.22f),
                                Color.Transparent,
                            ),
                            start = Offset(shift * 600f - 300f, 0f),
                            end = Offset(shift * 600f, 400f),
                        )
                    )
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "🤖", fontSize = 23.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI 识图 · 拍一下就好",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "小票 / 杯身 / 订单截图，自动填好",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.86f),
                    )
                }
                Text(
                    text = "→",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    emoji: String,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BouncyBox(onClick = onClick, modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = MaterialTheme.shapes.large,
                    ambientColor = colors.first().copy(alpha = 0.45f),
                    spotColor = colors.first().copy(alpha = 0.45f),
                )
                .clip(MaterialTheme.shapes.large)
                .background(Brush.linearGradient(colors))
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Column {
                Text(text = emoji, fontSize = 22.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun TeaRow(
    tea: Drink,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brand = Catalog.brandOf(tea.brand)
    SoftCard(
        modifier = modifier,
        elevation = 4.dp,
        contentPadding = PaddingValues(0.dp),
    ) {
        BouncyBox(onClick = onClick) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrandBadge(
                            badge = brand.badge,
                            color = Color(brand.color),
                            logo = brand.logo,
                            squircle = Catalog.isSquircle(brand.name),
                        )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tea.brand.ifBlank { "未命名" },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = TimeUtil.dayLabel(tea.timestamp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = buildString {
                            if (tea.name.isNotBlank()) append(tea.name)
                            if (tea.cupSize.isNotBlank()) {
                                if (isNotEmpty()) append(" · ")
                                append(tea.cupSize)
                            }
                            if (tea.sugar.isNotBlank()) append(" · ${tea.sugar}")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = PearlSoft,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingStars(rating = tea.rating, starSize = 13.dp)
                        Spacer(Modifier.weight(1f))
                        if (tea.price > 0) {
                            Text(
                                text = "¥${formatMoney(tea.price)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Caramel,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHint(onRecord: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DrinkCup(
            modifier = Modifier
                .width(120.dp)
                .height(150.dp),
            liquidColor = Color(0xFFDCC7B4),
            strawColor = Color(0xFFC9B5A3),
            toppingCount = 3,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "还没有记录哦",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "喝下的每一杯，都值得被记住",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

fun formatMoney(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.CHINA, "%.1f", value)
