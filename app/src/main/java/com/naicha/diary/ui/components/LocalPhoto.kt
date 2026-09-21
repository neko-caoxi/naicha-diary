package com.naicha.diary.ui.components

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val memoryCache = object : LruCache<String, ImageBitmap>(20 * 1024 * 1024) {
    override fun sizeOf(key: String, value: ImageBitmap): Int =
        value.width * value.height * 4
}

@Composable
fun LocalPhoto(
    file: File?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    maxSize: Int = 640,
) {
    val key = file?.let { "${it.path}@$maxSize" }
    val bitmap by produceState<ImageBitmap?>(
        initialValue = key?.let { memoryCache.get(it) },
        key1 = key,
    ) {
        if (file == null || !file.exists() || key == null) {
            value = null
            return@produceState
        }
        memoryCache.get(key)?.let {
            value = it
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            decode(file, maxSize)?.also { decoded -> memoryCache.put(key, decoded) }
        }
    }

    val current = bitmap
    if (current != null) {
        Image(
            bitmap = current,
            contentDescription = null,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        Box(modifier.background(Color(0xFFF1E6DA)))
    }
}

private fun decode(file: File, maxSize: Int): ImageBitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSize) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
    }
    BitmapFactory.decodeFile(file.path, options)?.asImageBitmap()
}.getOrNull()
