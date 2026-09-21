package com.naicha.diary.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naicha.diary.data.Catalog
import com.naicha.diary.data.Drink
import com.naicha.diary.ui.components.BrandBadge
import com.naicha.diary.ui.components.GradientButton
import com.naicha.diary.ui.components.LocalPhoto
import com.naicha.diary.ui.components.DrinkCup
import com.naicha.diary.ui.components.cupStyleFor
import com.naicha.diary.ui.components.RatingStars
import com.naicha.diary.ui.components.TagChip
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.PearlSoft
import com.naicha.diary.ui.theme.Strawberry
import com.naicha.diary.util.PhotoStore
import com.naicha.diary.util.TimeUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkDetailSheet(
    tea: Drink,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val brand = remember(tea.brand) { Catalog.brandOf(tea.brand) }
    val accent = Color(brand.color)
    var confirmDelete by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            DetailHeader(tea = tea, accent = accent)

            Spacer(Modifier.height(18.dp))

            InfoGrid(tea = tea, accent = accent)

            if (tea.toppings.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "小料",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tea.toppings.forEach { topping ->
                        TagChip(
                            text = topping,
                            selected = true,
                            accent = accent,
                            onClick = { },
                        )
                    }
                }
            }

            if (tea.note.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "当时想说",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(9.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(accent.copy(alpha = 0.09f))
                        .padding(14.dp),
                ) {
                    Text(
                        text = tea.note,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GradientButton(
                    text = "编辑",
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = listOf(accent, accent.copy(alpha = 0.75f)),
                )
                GradientButton(
                    text = if (confirmDelete) "确认删除？" else "删除",
                    onClick = { if (confirmDelete) onDelete() else confirmDelete = true },
                    modifier = Modifier.weight(1f),
                    colors = if (confirmDelete) {
                        listOf(Color(0xFFE53935), Color(0xFFEF5350))
                    } else {
                        listOf(Strawberry, Color(0xFFFFB3C6))
                    },
                )
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun DetailHeader(tea: Drink, accent: Color) {
    val brand = remember(tea.brand) { Catalog.brandOf(tea.brand) }
    val transition = rememberInfiniteTransition(label = "detail3d")
    val angle by transition.animateFloat(
        initialValue = -13f,
        targetValue = 13f,
        animationSpec = infiniteRepeatable(
            tween(2800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "detailAngle",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.18f), accent.copy(alpha = 0.05f))
                )
            )
            .padding(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DrinkCup(
                modifier = Modifier
                    .width(96.dp)
                    .height(124.dp)
                    .graphicsLayer {
                        rotationY = angle
                        cameraDistance = 16f * density
                    },
                liquidColor = accent,
                strawColor = accent.copy(alpha = 0.8f),
                toppingCount = (tea.toppings.size + 4).coerceAtMost(10),
                style = cupStyleFor(tea.categoryType),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tea.name.ifBlank { "一杯${tea.brand}" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandBadge(badge = brand.badge, color = accent, size = 28.dp, logo = brand.logo)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = tea.brand.ifBlank { "自制 / 其他" },
                        style = MaterialTheme.typography.titleMedium,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(9.dp))
                RatingStars(rating = tea.rating, starSize = 16.dp)
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = tea.mood, fontSize = 17.sp)
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = TimeUtil.fullLabel(tea.timestamp),
                        style = MaterialTheme.typography.labelMedium,
                        color = PearlSoft,
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoGrid(tea: Drink, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCell("杯型", tea.cupSize, accent, Modifier.weight(1f))
            InfoCell("甜度", tea.sugar, accent, Modifier.weight(1f))
            InfoCell("冰量", tea.ice, accent, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            InfoCell(
                "价格",
                if (tea.price > 0) "¥${formatMoney(tea.price)}" else "—",
                accent,
                Modifier.weight(1f),
            )
            InfoCell("热量", "${tea.calories} kcal", accent, Modifier.weight(1f))
            InfoCell("小料数", "${tea.toppings.size} 种", accent, Modifier.weight(1f))
        }
    }

    val photo = tea.photoPath
    if (photo != null) {
        Spacer(Modifier.height(14.dp))
        Text(
            text = "照片",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(9.dp))
        LocalPhoto(
            file = PhotoStore.fileOf(photo),
            contentScale = ContentScale.Crop,
            maxSize = 1080,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(MaterialTheme.shapes.large),
        )
    }
}

@Composable
private fun InfoCell(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = accent,
            fontWeight = FontWeight.Bold,
        )
    }
}

