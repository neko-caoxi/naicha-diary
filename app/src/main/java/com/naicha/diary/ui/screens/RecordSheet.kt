package com.naicha.diary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.core.content.FileProvider
import com.naicha.diary.data.AppSettings
import com.naicha.diary.data.Brand
import com.naicha.diary.data.Catalog
import com.naicha.diary.data.Drink
import com.naicha.diary.data.DrinkCategory
import com.naicha.diary.net.DeepSeekClient
import com.naicha.diary.ui.components.BrandBadge
import com.naicha.diary.ui.components.CupStyle
import com.naicha.diary.ui.components.cupStyleFor
import com.naicha.diary.ui.components.DrinkCup
import com.naicha.diary.ui.components.GradientButton
import com.naicha.diary.ui.components.LocalPhoto
import com.naicha.diary.ui.components.RatingStars
import com.naicha.diary.ui.components.TagChip
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.Matcha
import com.naicha.diary.ui.theme.Outline
import com.naicha.diary.ui.theme.PearlSoft
import com.naicha.diary.ui.theme.Strawberry
import com.naicha.diary.ui.theme.Taro
import com.naicha.diary.util.PhotoStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecordSheet(
    initial: Drink?,
    aiImage: Uri? = null,
    photoImage: Uri? = null,
    onDismiss: () -> Unit,
    onSave: (Drink) -> Unit,
    onDelete: ((Drink) -> Unit)? = null,
    onOpenSettings: () -> Unit = {},
    onPick: (forAi: Boolean, fromCamera: Boolean) -> Unit = { _, _ -> },
    onAiImageConsumed: () -> Unit = {},
    onPhotoImageConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var category by remember {
        mutableStateOf(DrinkCategory.of(initial?.category ?: DrinkCategory.Tea.label))
    }
    var brand by remember { mutableStateOf(initial?.brand ?: "") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var cupSize by remember { mutableStateOf(initial?.cupSize ?: "中杯") }
    var sugar by remember { mutableStateOf(initial?.sugar ?: "半糖") }
    var ice by remember { mutableStateOf(initial?.ice ?: "少冰") }
    var toppings by remember { mutableStateOf(initial?.toppings ?: emptyList()) }
    var priceText by remember {
        mutableStateOf(initial?.price?.takeIf { it > 0 }?.let { formatMoney(it) } ?: "")
    }
    var rating by remember { mutableIntStateOf(initial?.rating ?: 5) }
    var mood by remember { mutableStateOf(initial?.mood ?: "😋") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var photoName by remember { mutableStateOf(initial?.photoPath) }

    var aiLoading by remember { mutableStateOf(false) }
    var aiMessage by remember { mutableStateOf<String?>(null) }

    val brandColor = remember(brand, category) {
        if (brand.isNotBlank()) Color(Catalog.colorOf(brand)) else Color(category.accent)
    }
    val cupStyle = cupStyleFor(category)

    fun switchCategory(next: DrinkCategory) {
        category = next
        if (brand.isNotBlank() && Catalog.brandOf(brand).category != next) brand = ""
        val sizes = Catalog.sizesFor(next)
        if (cupSize !in sizes) cupSize = sizes.getOrElse(1) { sizes.first() }
        val sugars = Catalog.sugarOptions(next)
        if (sugar !in sugars) sugar = sugars.first()
        val ices = Catalog.iceOptions(next)
        if (ice !in ices) ice = ices.first()
        val extras = Catalog.extraOptions(next)
        toppings = toppings.filter { it in extras }
    }

    fun applyRecognized(drink: Drink) {
        category = drink.categoryType
        brand = drink.brand
        name = drink.name
        cupSize = drink.cupSize
        sugar = drink.sugar
        ice = drink.ice
        toppings = drink.toppings
        if (drink.price > 0) priceText = formatMoney(drink.price)
        if (drink.note.isNotBlank()) note = drink.note
    }

    fun runRecognize(uri: Uri) {
        val key = AppSettings.apiKey
        if (key.isBlank()) {
            aiMessage = "请先到「我的」里填写 DeepSeek API Key"
            return
        }
        aiLoading = true
        aiMessage = "正在识别…"
        scope.launch {
            val saved = withContext(Dispatchers.IO) { PhotoStore.save(context, uri) }
            if (saved != null) photoName = saved
            DeepSeekClient.recognize(context, uri, key)
                .onSuccess { drink ->
                    applyRecognized(drink)
                    aiMessage = "识别完成，已自动填好，记得核对一下"
                }
                .onFailure { error ->
                    aiMessage = error.message ?: "识别失败，请重试"
                }
            aiLoading = false
        }
    }

    LaunchedEffect(aiImage) {
        if (aiImage != null) {
            runRecognize(aiImage)
            onAiImageConsumed()
        }
    }

    LaunchedEffect(photoImage) {
        if (photoImage != null) {
            val saved = withContext(Dispatchers.IO) { PhotoStore.save(context, photoImage) }
            if (saved != null) photoName = saved
            onPhotoImageConsumed()
        }
    }

    val sheetBackground by animateColorAsState(
        targetValue = lerp(MaterialTheme.colorScheme.background, brandColor, 0.09f),
        animationSpec = tween(420),
        label = "sheetBackground",
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBackground,
        dragHandle = { SheetHandle() },
    ) {
        Column(modifier = Modifier.fillMaxHeight(0.94f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (initial == null) "记一杯" else "编辑记录",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "关闭",
                        tint = PearlSoft,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    PreviewRow(
                        liquid = softLiquid(brandColor),
                        straw = brandColor,
                        style = cupStyle,
                        toppingCount = (toppings.size + 3).coerceAtMost(10),
                        brand = brand,
                        name = name,
                        cupSize = cupSize,
                        sugar = sugar,
                        rating = rating,
                        mood = mood,
                        aiLoading = aiLoading,
                        onAiCamera = { onPick(true, true) },
                        onAiGallery = { onPick(true, false) },
                    )
                }

                if (aiMessage != null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(brandColor.copy(alpha = 0.10f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = aiMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }

                item {
                    CategorySwitcher(
                        current = category,
                        onSelect = { switchCategory(it) },
                    )
                }

                item {
                    FieldGroup(
                        title = "品牌",
                        subtitle = "${Catalog.brandsOf(category).size} 个",
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Catalog.brandsOf(category).forEach { item ->
                                BrandChip(
                                    brand = item,
                                    selected = brand == item.name,
                                    onClick = { brand = item.name },
                                )
                            }
                        }
                    }
                }

                item {
                    FieldGroup(title = "喝的是什么") {
                        Column {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("例如：多肉葡萄", color = Outline) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                colors = drinkFieldColors(),
                            )
                            Spacer(Modifier.height(10.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Catalog.commonNames[category].orEmpty().forEach { candidate ->
                                    TagChip(
                                        text = candidate,
                                        selected = name == candidate,
                                        accent = brandColor,
                                        onClick = { name = candidate },
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    FieldGroup(title = "规格") {
                        ChipRow(
                            options = Catalog.sizesFor(category),
                            selected = cupSize,
                            accent = brandColor,
                        ) { cupSize = it }
                    }
                }

                item {
                    FieldGroup(title = Catalog.sugarLabel(category)) {
                        ChipRow(
                            options = Catalog.sugarOptions(category),
                            selected = sugar,
                            accent = brandColor,
                        ) { sugar = it }
                    }
                }

                item {
                    FieldGroup(title = Catalog.iceLabel(category)) {
                        ChipRow(
                            options = Catalog.iceOptions(category),
                            selected = ice,
                            accent = brandColor,
                        ) { ice = it }
                    }
                }

                item {
                    FieldGroup(title = Catalog.extraLabel(category), subtitle = "可多选") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Catalog.extraOptions(category).forEach { item ->
                                TagChip(
                                    text = item,
                                    selected = toppings.contains(item),
                                    accent = brandColor,
                                    onClick = {
                                        toppings = if (toppings.contains(item)) {
                                            toppings - item
                                        } else {
                                            toppings + item
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        FieldGroup(title = "价格", modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { input ->
                                    priceText = input.filter { it.isDigit() || it == '.' }.take(7)
                                },
                                placeholder = { Text("0", color = Outline) },
                                prefix = { Text("¥ ", color = Caramel) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = MaterialTheme.shapes.medium,
                                colors = drinkFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        FieldGroup(title = "评分", modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .padding(horizontal = 12.dp, vertical = 15.dp)
                            ) {
                                RatingStars(
                                    rating = rating,
                                    starSize = 24.dp,
                                    onRate = { rating = it },
                                )
                            }
                        }
                    }
                }

                item {
                    FieldGroup(title = "心情") {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Catalog.moods.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (mood == emoji) brandColor.copy(alpha = 0.16f)
                                            else MaterialTheme.colorScheme.surfaceContainerLow
                                        )
                                        .border(
                                            width = if (mood == emoji) 2.dp else 0.dp,
                                            color = if (mood == emoji) brandColor else Color.Transparent,
                                            shape = CircleShape,
                                        )
                                        .clickable { mood = emoji },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    FieldGroup(title = "照片", subtitle = "可选") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val preview = photoName
                            if (preview != null) {
                                LocalPhoto(
                                    file = PhotoStore.fileOf(preview),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(84.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                )
                                Spacer(Modifier.width(12.dp))
                            }
                            PhotoButton(text = "相册", emoji = "🖼") { onPick(false, false) }
                            Spacer(Modifier.width(10.dp))
                            PhotoButton(text = "拍照", emoji = "📷") { onPick(false, true) }
                            if (preview != null) {
                                Spacer(Modifier.width(10.dp))
                                PhotoButton(text = "移除", emoji = "✕") {
                                    PhotoStore.delete(preview)
                                    photoName = null
                                }
                            }
                        }
                    }
                }

                item {
                    FieldGroup(title = "备注", subtitle = "可选") {
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it.take(120) },
                            placeholder = { Text("今天为什么想喝这杯…", color = Outline) },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = drinkFieldColors(),
                        )
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (initial != null && onDelete != null) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Strawberry.copy(alpha = 0.16f))
                            .clickable { onDelete(initial) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "删除",
                            tint = Strawberry,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                }
                GradientButton(
                    text = if (initial == null) "记下这一杯" else "保存修改",
                    onClick = {
                        val item = (initial ?: Drink()).copy(
                            category = category.label,
                            brand = brand.ifBlank { "自制 / 其他" },
                            name = name.trim(),
                            cupSize = cupSize,
                            sugar = sugar,
                            ice = ice,
                            toppings = toppings,
                            price = priceText.toDoubleOrNull() ?: 0.0,
                            rating = rating,
                            mood = mood,
                            note = note.trim(),
                            photoPath = photoName,
                        )
                        onSave(item)
                    },
                    modifier = Modifier.weight(1f),
                    colors = listOf(brandColor, brandColor.copy(alpha = 0.75f)),
                    leading = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun CategorySwitcher(
    current: DrinkCategory,
    onSelect: (DrinkCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color(current.accent).copy(alpha = 0.35f),
                spotColor = Color(current.accent).copy(alpha = 0.35f),
            )
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(30.dp),
            )
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        DrinkCategory.entries.forEach { entry ->
            val selected = current == entry
            val accent = Color(entry.accent)
            val bg by animateColorAsState(
                targetValue = if (selected) accent else Color.Transparent,
                animationSpec = tween(320),
                label = "catBg",
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(bg)
                    .clickable { onSelect(entry) }
                    .padding(vertical = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = entry.badge,
                    fontSize = 23.sp,
                    color = if (selected) Color.White else PearlSoft,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Color.White else PearlSoft,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}


@Composable
private fun BrandChip(
    brand: Brand,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = Color(brand.color)
    Column(
        modifier = modifier
            .width(74.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) color.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) color else Color.Transparent,
                shape = RoundedCornerShape(22.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandBadge(
            badge = brand.badge,
            color = color,
            size = 40.dp,
            logo = brand.logo,
            squircle = Catalog.isSquircle(brand.name),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = brand.name,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) color else PearlSoft,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
}

@Composable
private fun SheetHandle() {
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
                .background(Outline),
        )
    }
}

@Composable
private fun PreviewRow(
    liquid: Color,
    straw: Color,
    style: CupStyle,
    toppingCount: Int,
    brand: String,
    name: String,
    cupSize: String,
    sugar: String,
    rating: Int,
    mood: String,
    aiLoading: Boolean,
    onAiCamera: () -> Unit,
    onAiGallery: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        straw.copy(alpha = 0.14f),
                        MaterialTheme.colorScheme.surfaceContainerLow,
                    )
                )
            )
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DrinkCup(
                modifier = Modifier
                    .width(78.dp)
                    .height(100.dp),
                liquidColor = liquid,
                strawColor = straw,
                toppingCount = toppingCount,
                style = style,
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = brand.ifBlank { "选个品牌吧" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = listOf(name.ifBlank { "还没写品名" }, cupSize, sugar)
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PearlSoft,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(rating = rating, starSize = 14.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(text = mood, fontSize = 16.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (aiLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = straw,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "AI 识别中…",
                            style = MaterialTheme.typography.labelMedium,
                            color = PearlSoft,
                        )
                    } else {
                        AiChip(text = "📷 拍小票", onClick = onAiCamera)
                        Spacer(Modifier.width(8.dp))
                        AiChip(text = "🖼 选图", onClick = onAiGallery)
                    }
                }
            }
        }
    }
}

@Composable
private fun AiChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = PearlSoft,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FieldGroup(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (subtitle != null) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(
    options: List<String>,
    selected: String,
    accent: Color,
    onSelect: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            TagChip(
                text = option,
                selected = selected == option,
                accent = accent,
                onClick = { onSelect(option) },
            )
        }
    }
}

@Composable
private fun PhotoButton(text: String, emoji: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = PearlSoft,
        )
    }
}

@Composable
private fun drinkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Caramel,
    unfocusedBorderColor = Outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    cursorColor = Caramel,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
)

private fun softLiquid(color: Color): Color = Color(
    red = color.red * 0.62f + 0.38f,
    green = color.green * 0.62f + 0.38f,
    blue = color.blue * 0.62f + 0.38f,
    alpha = 1f,
)

