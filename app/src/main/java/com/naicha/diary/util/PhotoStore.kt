package com.naicha.diary.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

object PhotoStore {

    private const val MAX_EDGE = 1080
    private const val QUALITY = 82

    private lateinit var dir: File

    fun init(context: Context) {
        dir = File(context.filesDir, "photos").apply { mkdirs() }
    }

    fun fileOf(name: String): File = File(dir, name)

    fun save(context: Context, uri: Uri): String? = runCatching {
        val raw = context.contentResolver.openInputStream(uri) ?: return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        raw.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = calculateSample(bounds.outWidth, bounds.outHeight)
        }
        val source = context.contentResolver.openInputStream(uri) ?: return null
        val decoded = source.use { BitmapFactory.decodeStream(it, null, options) } ?: return null

        val rotation = context.contentResolver.openInputStream(uri)?.use {
            when (ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        val oriented = if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                .also { if (it != decoded) decoded.recycle() }
        } else decoded

        val scaled = scaleDown(oriented)
        val name = "tea_${System.currentTimeMillis()}.jpg"
        FileOutputStream(File(dir, name)).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
        }
        if (scaled != oriented) oriented.recycle() else scaled.recycle()
        name
    }.getOrNull()

    fun delete(name: String?) {
        if (name.isNullOrBlank()) return
        runCatching { fileOf(name).delete() }
    }

    private fun calculateSample(width: Int, height: Int): Int {
        var sample = 1
        var longest = maxOf(width, height)
        while (longest / 2 >= MAX_EDGE) {
            longest /= 2
            sample *= 2
        }
        return sample
    }

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= MAX_EDGE) return bitmap
        val ratio = MAX_EDGE.toFloat() / longest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt().coerceAtLeast(1),
            (bitmap.height * ratio).toInt().coerceAtLeast(1),
            true,
        )
    }
}
