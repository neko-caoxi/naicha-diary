package com.naicha.diary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naicha.diary.data.Catalog
import com.naicha.diary.data.Drink
import com.naicha.diary.ui.components.BouncyBox
import com.naicha.diary.ui.components.BrandBadge
import com.naicha.diary.ui.components.BrandLogo
import com.naicha.diary.ui.components.LocalPhoto
import com.naicha.diary.ui.components.PearlField
import com.naicha.diary.ui.components.RatingStars
import com.naicha.diary.ui.components.SoftCard
import com.naicha.diary.ui.components.TagChip
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.Matcha
import com.naicha.diary.ui.theme.PearlSoft
import com.naicha.diary.ui.theme.Strawberry
import com.naicha.diary.ui.theme.Taro
import com.naicha.diary.util.PhotoStore
import com.naicha.diary.util.TimeUtil

private enum class WallMode(val label: String, val emoji: String) {
    Wall("饮品墙", "🧱"),
    Brand("品牌榜", "🏆"),
    Timeline("时间轴", "🕰"),
}

@Composable
fun WallScreen(
    items: List<Drink>,
    onOpenDetail: (Drink) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mode by remember { mutableStateOf(WallMode.Wall) }
    var showPoster by remember { mutableStateOf(false) }

    val sorted = remember(items) { items.sortedByDescending { it.timestamp } }
    val totalSpend = remember(items) { items.sumOf { it.price } }
    val topBrand = remember(items) {
        items.groupingBy { it.brand }.eachCount().maxByOrNull { it.value }?.key
    }
    val photoCount = remember(items) { items.count { it.photoPath != null } }

    Box(modifier.fillMaxSize()) {
        PearlField(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            count = 16,
            seed = 11,
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "我的饮品墙 🧱",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = if (items.isEmpty()) "还没有故事" else
                                "共 ${items.size} 杯 · ¥${formatMoney(totalSpend)}" +
                                    (topBrand?.let { " · 最爱 $it" } ?: ""),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    BouncyBox(
                        onClick = { if (items.isNotEmpty()) showPoster = true },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = CircleShape,
                                    ambientColor = Caramel.copy(alpha = 0.5f),
                                    spotColor = Caramel.copy(alpha = 0.5f),
                                )
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(Caramel, Color(0xFFE2A776)))
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "生成分享图",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WallMode.entries.forEach { entry ->
                        TagChip(
                            text = "${entry.emoji} ${entry.label}",
                            selected = mode == entry,
                            accent = Caramel,
                            onClick = { mode = entry },
                        )
                    }
                }
            }

            when (mode) {
                WallMode.Wall -> WallGrid(
                    items = sorted,
                    onOpenDetail = onOpenDetail,
                )
                WallMode.Brand -> BrandRank(items = sorted)
                WallMode.Timeline -> TimelineView(
                    items = sorted,
                    onOpenDetail = onOpenDetail,
                )
            }
        }

        if (showPoster) {
            SharePosterDialog(
                items = sorted,
                totalSpend = totalSpend,
                topBrand = topBrand,
                photoCount = photoCount,
                onDismiss = { showPoster = false },
            )
        }
    }
}

@Composable
private fun WallGrid(
    items: List<Drink>,
    onOpenDetail: (Drink) -> Unit,
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "墙上还空空的哦",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 130.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp,
    ) {
        items(items, key = { it.id }) { tea ->
            WallCard(tea = tea, onClick = { onOpenDetail(tea) })
        }
    }
}

@Composable
private fun WallCard(tea: Drink, onClick: () -> Unit) {
    val brand = Catalog.brandOf(tea.brand)
    val base = Color(brand.color)
    val tall = tea.photoPath != null

    BouncyBox(onClick = onClick) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(22.dp),
                    ambientColor = base.copy(alpha = 0.35f),
                    spotColor = base.copy(alpha = 0.35f),
                )
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (tall) 0.86f else 1.25f),
            ) {
                if (tea.photoPath != null) {
                    LocalPhoto(
                        file = PhotoStore.fileOf(tea.photoPath),
                        contentScale = ContentScale.Crop,
                        maxSize = 640,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f))
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        base.copy(alpha = 0.92f),
                                        base.copy(alpha = 0.55f),
                                    )
                                )
                            ),
                    )
                    BrandLogo(
                        logo = brand.logo,
                        color = base,
                        badge = brand.badge,
                        size = 84.dp,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    Text(
                        text = brand.badge,
                        fontSize = 96.sp,
                        color = Color.White.copy(alpha = 0.20f),
                        fontWeight = FontWeight.Black,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 2.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.88f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = tea.brand.ifBlank { "其他" },
                        style = MaterialTheme.typography.labelMedium,
                        color = base,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Column(modifier = Modifier.padding(11.dp)) {
                Text(
                    text = tea.name.ifBlank { tea.cupSize + tea.categoryType.label },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tea.mood,
                        fontSize = 12.sp,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = TimeUtil.dayLabel(tea.timestamp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(5.dp))
                RatingStars(rating = tea.rating, starSize = 11.dp)
            }
        }
    }
}

@Composable
private fun BrandRank(items: List<Drink>) {
    val ranking = remember(items) {
        items.groupBy { it.brand }
            .map { (brand, list) ->
                Triple(brand, list.size, list.sumOf { it.price })
            }
            .sortedByDescending { it.second }
    }
    val max = ranking.firstOrNull()?.second ?: 1

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(1),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 130.dp),
        verticalItemSpacing = 12.dp,
    ) {
        items(ranking, key = { it.first }) { (brandName, count, spend) ->
            val brand = Catalog.brandOf(brandName)
            val color = Color(brand.color)
            SoftCard(elevation = 4.dp, shadowColor = color) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandBadge(badge = brand.badge, color = color, size = 46.dp, logo = brand.logo)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = brandName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "$count 杯",
                                style = MaterialTheme.typography.labelLarge,
                                color = color,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(7.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(9.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.14f)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((count.toFloat() / max).coerceIn(0.06f, 1f))
                                    .height(9.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(color, color.copy(alpha = 0.6f))
                                        )
                                    ),
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "累计 ¥${formatMoney(spend)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineView(
    items: List<Drink>,
    onOpenDetail: (Drink) -> Unit,
) {
    val grouped = remember(items) {
        items.groupBy { TimeUtil.monthKey(it.timestamp) }.toSortedMap(compareByDescending { it })
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(1),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 130.dp),
        verticalItemSpacing = 14.dp,
    ) {
        grouped.forEach { (month, teas) ->
            item(key = "header_$month") {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = month.replace("-", " 年 ") + " 月",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Caramel.copy(alpha = 0.15f))
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "${teas.size} 杯",
                            style = MaterialTheme.typography.labelMedium,
                            color = Caramel,
                        )
                    }
                }
            }
            items(teas, key = { it.id }) { tea ->
                TimelineRow(tea = tea, onClick = { onOpenDetail(tea) })
            }
        }
    }
}

@Composable
private fun TimelineRow(tea: Drink, onClick: () -> Unit) {
    val brand = Catalog.brandOf(tea.brand)
    val color = Color(brand.color)
    BouncyBox(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.35f)),
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.35f)),
                )
            }
            Spacer(Modifier.width(12.dp))
            if (tea.photoPath != null) {
                LocalPhoto(
                    file = PhotoStore.fileOf(tea.photoPath),
                    modifier = Modifier
                        .size(54.dp)
                        .clip(MaterialTheme.shapes.medium),
                    maxSize = 320,
                )
                Spacer(Modifier.width(12.dp))
            } else {
                BrandBadge(badge = brand.badge, color = color, size = 40.dp, logo = brand.logo)
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tea.name.ifBlank { "一杯${tea.brand}" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${tea.brand} · ${tea.cupSize} · ${tea.sugar}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PearlSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(rating = tea.rating, starSize = 11.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = TimeUtil.fullLabel(tea.timestamp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (tea.price > 0) {
                Text(
                    text = "¥${formatMoney(tea.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Caramel,
                )
            }
        }
    }
}
