package com.naicha.diary.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naicha.diary.data.AppSettings
import com.naicha.diary.data.Catalog
import com.naicha.diary.data.Drink
import com.naicha.diary.net.DeepSeekClient
import com.naicha.diary.ui.components.AnimatedNumber
import com.naicha.diary.ui.components.GradientButton
import com.naicha.diary.ui.components.PearlField
import com.naicha.diary.ui.components.SectionHeader
import com.naicha.diary.ui.components.SoftCard
import com.naicha.diary.ui.components.StatPill
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.Matcha
import com.naicha.diary.ui.theme.PearlSoft
import com.naicha.diary.ui.theme.Sky
import com.naicha.diary.ui.theme.Strawberry
import com.naicha.diary.ui.theme.Taro
import com.naicha.diary.util.TimeUtil
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun StatsScreen(
    items: List<Drink>,
    modifier: Modifier = Modifier,
) {
    val total = items.size
    val totalSpend = remember(items) { items.sumOf { it.price } }
    val totalCalories = remember(items) { items.sumOf { it.calories } }
    val avgPrice = if (total == 0) 0.0 else totalSpend / total
    val streak = remember(items) { currentStreak(items) }
    val avgRating = if (total == 0) 0f else items.sumOf { it.rating }.toFloat() / total

    Box(modifier.fillMaxSize()) {
        PearlField(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            count = 12,
            seed = 23,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(modifier = Modifier.statusBarsPadding().padding(top = 12.dp)) {
                    Text(
                        text = "饮品数据 📊",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = if (total == 0) "还没有数据" else "看看你都喝了些什么",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill(
                        label = "总杯数",
                        value = total.toString(),
                        accent = Caramel,
                        modifier = Modifier.weight(1f),
                    )
                    StatPill(
                        label = "总花费",
                        value = "¥${formatMoney(totalSpend)}",
                        accent = Taro,
                        modifier = Modifier.weight(1f),
                    )
                    StatPill(
                        label = "连续天数",
                        value = "$streak 天",
                        accent = Matcha,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatPill(
                        label = "平均单价",
                        value = "¥${formatMoney(avgPrice)}",
                        accent = Sky,
                        modifier = Modifier.weight(1f),
                    )
                    StatPill(
                        label = "平均评分",
                        value = String.format("%.1f", avgRating),
                        accent = Strawberry,
                        modifier = Modifier.weight(1f),
                    )
                    StatPill(
                        label = "总热量",
                        value = "${totalCalories}",
                        accent = Color(0xFFE07A5F),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (total > 0) {
                item { AiInsightCard(items = items) }

                item {
                    SoftCard {
                        SectionHeader(title = "品牌分布", subtitle = "喝得最多的那几家")
                        Spacer(Modifier.height(14.dp))
                        BrandDonut(items = items)
                    }
                }

                item {
                    SoftCard {
                        SectionHeader(title = "甜度偏好", subtitle = "你的甜度人格")
                        Spacer(Modifier.height(14.dp))
                        PreferenceBars(
                            entries = Catalog.sugars.map { sugar ->
                                sugar to items.count { it.sugar == sugar }
                            },
                            colors = listOf(Strawberry, Color(0xFFF2B04A), Caramel, Matcha, Sky),
                        )
                    }
                }

                item {
                    SoftCard {
                        SectionHeader(title = "本月日历", subtitle = "每天喝了几杯")
                        Spacer(Modifier.height(14.dp))
                        MonthHeatmap(items = items)
                    }
                }

                item {
                    SoftCard {
                        SectionHeader(title = "成就勋章", subtitle = "解锁你的饮品成就")
                        Spacer(Modifier.height(14.dp))
                        Achievements(items = items, streak = streak)
                    }
                }

                item {
                    SoftCard {
                        SectionHeader(title = "最近 7 天", subtitle = "每日杯数走势")
                        Spacer(Modifier.height(14.dp))
                        WeekTrend(items = items)
                    }
                }
            }
        }
    }
}

@Composable
private fun AiInsightCard(items: List<Drink>) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    SoftCard(
        color = Color(0xFFF3F0FF),
        shadowColor = Taro,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF6C5CE7), Color(0xFFB79CED)))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🤖", fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI 洞察",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "让 AI 说说你的饮品习惯",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Taro,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "AI 正在翻阅你的记录…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PearlSoft,
                )
            }

            result != null -> Text(
                text = result!!,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4B3B8F),
            )

            else -> {
                Text(
                    text = error ?: "点一下，AI 会读你所有的记录，给你一句总结",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (error != null) Strawberry else PearlSoft,
                )
                Spacer(Modifier.height(12.dp))
                GradientButton(
                    text = "让 AI 分析一下",
                    onClick = {
                        val key = AppSettings.apiKey
                        if (key.isBlank()) {
                            error = "先去「我的」填 DeepSeek API Key"
                            return@GradientButton
                        }
                        loading = true
                        error = null
                        scope.launch {
                            DeepSeekClient.insight(key, buildSummary(items))
                                .onSuccess { result = it }
                                .onFailure { error = it.message ?: "分析失败" }
                            loading = false
                        }
                    },
                    colors = listOf(Color(0xFF6C5CE7), Color(0xFFB79CED)),
                )
            }
        }
    }
}

private fun buildSummary(items: List<Drink>): String {
    val total = items.size
    val topBrand = items.groupingBy { it.brand }.eachCount().maxByOrNull { it.value }
    val topSugar = items.groupingBy { it.sugar }.eachCount().maxByOrNull { it.value }
    val topCategory = items.groupingBy { it.category }.eachCount().maxByOrNull { it.value }
    val avgRating = if (total == 0) 0f else items.sumOf { it.rating }.toFloat() / total
    val spend = items.sumOf { it.price }
    val today = TimeUtil.startOfDay(System.currentTimeMillis())
    val last7 = items.count { it.timestamp >= today - 6 * 86_400_000L }
    val last30 = items.count { it.timestamp >= today - 29 * 86_400_000L }
    val calories = items.sumOf { it.calories }

    return buildString {
        append("用户最近的饮品记录统计：")
        append("总计 $total 次；")
        topCategory?.let { append("最常喝品类是 ${it.key}（${it.value} 次）；") }
        topBrand?.let { append("最常喝品牌是 ${it.key}（${it.value} 次）；") }
        topSugar?.let { append("最常选 ${it.key}；") }
        append("平均评分 ${String.format("%.1f", avgRating)}；")
        append("累计花费约 ${spend.toInt()} 元；")
        append("累计热量约 ${calories} 千卡；")
        append("最近 7 天 $last7 次，最近 30 天 $last30 次。")
        val brands = items.map { it.brand }.distinct().size
        append("一共尝试过 $brands 个品牌。")
    }
}

@Composable
private fun BrandDonut(items: List<Drink>) {
    val data = remember(items) {
        items.groupingBy { it.brand }.eachCount()
            .entries.sortedByDescending { it.value }
            .take(6)
    }
    val total = data.sumOf { it.value }.coerceAtLeast(1)
    val palette = listOf(Caramel, Taro, Matcha, Strawberry, Sky, Color(0xFFF2B04A))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(128.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = size.minDimension * 0.24f
                val inset = stroke / 2f
                var startAngle = -90f
                data.forEachIndexed { index, entry ->
                    val sweep = entry.value.toFloat() / total * 360f
                    drawArc(
                        color = palette[index % palette.size],
                        startAngle = startAngle + 1.6f,
                        sweepAngle = (sweep - 3.2f).coerceAtLeast(0.6f),
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                    startAngle += sweep
                }
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedNumber(
                    value = total,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Caramel,
                )
                Text(
                    text = "总杯数",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            data.forEachIndexed { index, entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(palette[index % palette.size])
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = entry.key,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                    Text(
                        text = "${entry.value} 杯",
                        style = MaterialTheme.typography.labelMedium,
                        color = PearlSoft,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceBars(
    entries: List<Pair<String, Int>>,
    colors: List<Color>,
) {
    val max = entries.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        entries.forEachIndexed { index, (label, count) ->
            val color = colors[index % colors.size]
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(52.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.13f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((count.toFloat() / max).coerceIn(0.04f, 1f))
                            .height(14.dp)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f)))),
                    )
                }
                Spacer(Modifier.width(9.dp))
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelMedium,
                    color = PearlSoft,
                    modifier = Modifier.width(24.dp),
                )
            }
        }
    }
}

@Composable
private fun MonthHeatmap(items: List<Drink>) {
    val calendar = remember { Calendar.getInstance() }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val today = calendar.get(Calendar.DAY_OF_MONTH)

    val perDay = remember(items, year, month) {
        items.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            c.get(Calendar.YEAR) == year && c.get(Calendar.MONTH) == month
        }.groupingBy {
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.DAY_OF_MONTH)
        }.eachCount()
    }

    val first = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val leading = (first.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
    val maxCount = perDay.values.maxOrNull()?.coerceAtLeast(1) ?: 1

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        val cells = leading + daysInMonth
        val rows = (cells + 6) / 7
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val index = row * 7 + col
                    val day = index - leading + 1
                    Box(modifier = Modifier.weight(1f).padding(2.dp)) {
                        if (day in 1..daysInMonth) {
                            val count = perDay[day] ?: 0
                            val intensity = if (count == 0) 0f else {
                                (0.28f + count.toFloat() / maxCount * 0.72f)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (count == 0) MaterialTheme.colorScheme.surfaceContainerLow
                                        else Caramel.copy(alpha = intensity)
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (count == 0) PearlSoft else Color.White,
                                    fontWeight = if (day == today) FontWeight.Black else FontWeight.Normal,
                                )
                            }
                        } else {
                            Spacer(Modifier.fillMaxWidth().aspectRatio(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Achievements(items: List<Drink>, streak: Int) {
    val brandCount = items.map { it.brand }.distinct().size
    val sugarCount = items.map { it.sugar }.distinct().size
    val photoCount = items.count { it.photoPath != null }
    val total = items.size

    val badges = listOf(
        Triple("🌱", "第一杯", total >= 1),
        Triple("🧋", "十杯达成", total >= 10),
        Triple("🏅", "五十杯", total >= 50),
        Triple("👑", "百杯王者", total >= 100),
        Triple("🔥", "连续三天", streak >= 3),
        Triple("⚡", "连续七天", streak >= 7),
        Triple("🧭", "尝鲜达人", brandCount >= 5),
        Triple("🍬", "甜度全通", sugarCount >= 5),
        Triple("📷", "拍照记录", photoCount >= 5),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        badges.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (emoji, label, unlocked) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (unlocked) Caramel.copy(alpha = 0.13f)
                                else MaterialTheme.colorScheme.surfaceContainerLow
                            )
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (unlocked) emoji else "🔒",
                            fontSize = 22.sp,
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (unlocked) Caramel else PearlSoft,
                            fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun WeekTrend(items: List<Drink>) {
    val days = remember(items) {
        val today = TimeUtil.startOfDay(System.currentTimeMillis())
        (6 downTo 0).map { offset ->
            val start = today - offset * 86_400_000L
            val end = start + 86_400_000L
            val count = items.count { it.timestamp in start until end }
            val label = android.text.format.DateFormat.format("E", start).toString()
            Triple(label, count, offset == 0)
        }
    }
    val max = days.maxOf { it.second }.coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        days.forEach { (label, count, isToday) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = if (count > 0) count.toString() else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = Caramel,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((14 + count.toFloat() / max * 62).dp)
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (count == 0) {
                                Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
                            } else {
                                Modifier.background(
                                    Brush.verticalGradient(
                                        listOf(
                                            if (isToday) Taro else Caramel,
                                            (if (isToday) Taro else Caramel).copy(alpha = 0.55f),
                                        )
                                    )
                                )
                            }
                        ),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isToday) Caramel else PearlSoft,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

private fun currentStreak(items: List<Drink>): Int {
    if (items.isEmpty()) return 0
    val days = items.map { TimeUtil.startOfDay(it.timestamp) }.toHashSet()
    var cursor = TimeUtil.startOfDay(System.currentTimeMillis())
    if (!days.contains(cursor)) cursor -= 86_400_000L
    var streak = 0
    while (days.contains(cursor)) {
        streak++
        cursor -= 86_400_000L
    }
    return streak
}
