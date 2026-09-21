package com.naicha.diary.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.naicha.diary.data.Catalog
import com.naicha.diary.data.Drink
import com.naicha.diary.ui.components.BrandLogo
import com.naicha.diary.ui.components.GradientButton
import com.naicha.diary.ui.components.LocalPhoto
import com.naicha.diary.ui.theme.Caramel
import com.naicha.diary.ui.theme.PearlSoft
import com.naicha.diary.util.PhotoStore
import com.naicha.diary.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SharePosterDialog(
    items: List<Drink>,
    totalSpend: Double,
    topBrand: String?,
    photoCount: Int,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    var hint by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.width(320.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.66f)
                    .clip(RoundedCornerShape(26.dp))
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    },
            ) {
                PosterContent(
                    items = items,
                    totalSpend = totalSpend,
                    topBrand = topBrand,
                    photoCount = photoCount,
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GradientButton(
                    text = "分享",
                    onClick = {
                        scope.launch {
                            val bitmap = withContext(Dispatchers.Default) {
                                graphicsLayer.toImageBitmap().asAndroidBitmap()
                            }
                            shareBitmap(context, bitmap)
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
                GradientButton(
                    text = "存到相册",
                    onClick = {
                        scope.launch {
                            val bitmap = withContext(Dispatchers.Default) {
                                graphicsLayer.toImageBitmap().asAndroidBitmap()
                            }
                            val ok = saveToGallery(context, bitmap)
                            hint = if (ok) "已保存到相册" else "保存失败，试试分享吧"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = listOf(Color(0xFFB79CED), Color(0xFFCBBAF5)),
                )
            }

            if (hint != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = hint!!,
                    style = MaterialTheme.typography.labelMedium,
                    color = PearlSoft,
                )
            }
        }
    }
}

@Composable
private fun PosterContent(
    items: List<Drink>,
    totalSpend: Double,
    topBrand: String?,
    photoCount: Int,
) {
    val dateRange = remember(items) {
        if (items.isEmpty()) {
            SimpleDateFormat("yyyy.MM.dd", Locale.CHINA).format(Date())
        } else {
            val format = SimpleDateFormat("yyyy.MM.dd", Locale.CHINA)
            val first = format.format(Date(items.minOf { it.timestamp }))
            val last = format.format(Date(items.maxOf { it.timestamp }))
            if (first == last) first else "$first - $last"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFFFF7EC), Color(0xFFFBE6CF), Color(0xFFF6DCC0))
                )
            ),
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0x55FFFFFF), Color.Transparent)
                    ),
                    CircleShape,
                )
        )

        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text(
                text = "我的饮品日记",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF4A3428),
                fontWeight = FontWeight.Black,
            )
            Text(
                text = dateRange,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF9C8478),
            )

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = items.size.toString(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    color = Caramel,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "杯",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF9C8478),
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "共花费",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF9C8478),
                    )
                    Text(
                        text = "¥${formatMoney(totalSpend)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF4A3428),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            PosterGrid(items = items, modifier = Modifier.weight(1f))

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.66f))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = topBrand?.let { "最爱喝 $it" } ?: "还没开始记录",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF4A3428),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (photoCount > 0) {
                    Text(
                        text = "$photoCount 张照片",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF9C8478),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "🧋 饮品日记 · 记录每一杯的小确幸",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF9C8478),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PosterGrid(items: List<Drink>, modifier: Modifier = Modifier) {
    val display = remember(items) {
        items.sortedByDescending { it.timestamp }.take(12)
    }
    val rows = display.chunked(3)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                row.forEach { tea ->
                    PosterCell(tea = tea, modifier = Modifier.weight(1f))
                }
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PosterCell(tea: Drink, modifier: Modifier = Modifier) {
    val brand = Catalog.brandOf(tea.brand)
    val color = Color(brand.color)

    Box(
        modifier = modifier
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(color.copy(alpha = 0.92f), color.copy(alpha = 0.58f))
                )
            ),
    ) {
        if (tea.photoPath != null) {
            LocalPhoto(
                file = PhotoStore.fileOf(tea.photoPath),
                contentScale = ContentScale.Crop,
                maxSize = 320,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            BrandLogo(
                logo = brand.logo,
                color = color,
                badge = brand.badge,
                size = 46.dp,
                squircle = Catalog.isSquircle(brand.name),
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(5.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(horizontal = 5.dp, vertical = 2.dp),
        ) {
            Text(
                text = tea.brand.take(4),
                fontSize = 8.sp,
                color = color,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

private fun shareBitmap(context: Context, bitmap: Bitmap) {
    runCatching {
        val dir = File(context.filesDir, "share").apply { mkdirs() }
        val file = File(dir, "naicha_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享我的饮品墙"))
    }
}

private fun saveToGallery(context: Context, bitmap: Bitmap): Boolean = runCatching {
    val name = "naicha_${System.currentTimeMillis()}.png"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/饮品日记",
            )
        }
        val uri = context.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return false
        context.contentResolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    } else {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "饮品日记",
        ).apply { mkdirs() }
        FileOutputStream(File(dir, name)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
    true
}.getOrDefault(false)
