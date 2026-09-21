package com.naicha.diary.net

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.naicha.diary.data.Catalog
import com.naicha.diary.data.Drink
import com.naicha.diary.data.DrinkCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object DeepSeekClient {

    private const val ENDPOINT = "https://api.deepseek.com/chat/completions"
    private const val MODEL = "deepseek-flash"
    private const val MAX_EDGE = 1280

    private val json = Json { ignoreUnknownKeys = true }

    private val systemPrompt = """
        你是一个饮品记录助手。用户会给你一张图片，可能是饮品小票、外卖订单截图、杯身标签或饮品照片。
        请识别图中的饮品信息，并且只输出一个 json 对象，不要输出任何解释、markdown 代码块或多余文字。

        json 字段要求：
        {
          "brand": "品牌名，例如 喜茶、奈雪的茶、瑞幸咖啡、星巴克；识别不到填 \"\"",
          "name": "饮品名称，例如 多肉葡萄、生椰拿铁、精酿IPA",
          "category": "品类，只能是 茶饮 / 咖啡 / 酒 / 其他 之一",
          "cupSize": "规格。茶饮或咖啡填 小杯/中杯/大杯/超大杯；酒类填 单份/小杯/中杯/大杯/整瓶",
          "sugar": "甜度，全糖/七分糖/半糖/三分糖/无糖；如果是酒类则填 无酒精/低度/微醺/中度/高度",
          "ice": "冰量，正常冰/少冰/去冰/常温/热饮；如果是酒类则填 冰镇/常温/温热",
          "toppings": ["小料或配料，数组，没有就给空数组"],
          "price": 价格数字，识别不到填 0,
          "note": "简短备注，可留空"
        }

        判断规则：
        - 含咖啡、拿铁、美式、浓缩等关键词归为 咖啡
        - 含啤酒、红酒、威士忌、清酒、鸡尾酒、白酒等归为 酒
        - 含可乐、雪碧、果汁、气泡水、酸奶、矿泉水等归为 其他
        - 其余茶饮、奶茶、果茶归为 茶饮
        - 无法确定的字段请给合理默认值，不要编造价格。
    """.trimIndent()

    suspend fun recognize(context: Context, uri: Uri, apiKey: String): Result<Drink> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dataUrl = encodeImage(context, uri)
                    ?: error("图片读取失败，换一张试试")
                val payload = buildRequestBody(dataUrl)
                val response = post(payload, apiKey)
                parseDrink(response)
            }
        }

    suspend fun testConnection(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = buildJsonObject {
                put("model", MODEL)
                put("max_tokens", 20)
                put("messages", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "user")
                        put("content", "只回复两个字：可用")
                    })
                })
            }
            val text = post(payload.toString(), apiKey)
            val content = json.parseToJsonElement(text)
                .jsonObject["choices"]?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content
                .orEmpty()
            content.ifBlank { "连接成功" }
        }
    }

    suspend fun insight(apiKey: String, summary: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val payload = buildJsonObject {
                    put("model", MODEL)
                    put("max_tokens", 200)
                    put("temperature", 1.1)
                    put("messages", buildJsonArray {
                        add(buildJsonObject {
                            put("role", "system")
                            put(
                                "content",
                                "你是饮品记录助手。用户会给你他最近的饮品统计，" +
                                    "请用一句轻松、有趣、像朋友聊天的话总结他的饮品习惯。" +
                                    "要求：30 字以内，不要 markdown，不要引号，直接输出这句话。",
                            )
                        })
                        add(buildJsonObject {
                            put("role", "user")
                            put("content", summary)
                        })
                    })
                }
                val text = post(payload.toString(), apiKey)
                json.parseToJsonElement(text).jsonObject["choices"]?.jsonArray
                    ?.firstOrNull()?.jsonObject
                    ?.get("message")?.jsonObject
                    ?.get("content")?.jsonPrimitive?.content
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: error("这次没想出什么，再试一次吧")
            }
        }

    private fun buildRequestBody(dataUrl: String): String = buildJsonObject {
        put("model", MODEL)
        put("max_tokens", 1000)
        put("temperature", 0.1)
        put("response_format", buildJsonObject { put("type", "json_object") })
        put("messages", buildJsonArray {
            add(buildJsonObject {
                put("role", "system")
                put("content", systemPrompt)
            })
            add(buildJsonObject {
                put("role", "user")
                put("content", buildJsonArray {
                    add(buildJsonObject {
                        put("type", "text")
                        put("text", "请识别这张图片里的饮品，按要求的 json 格式输出。")
                    })
                    add(buildJsonObject {
                        put("type", "image_url")
                        put("image_url", buildJsonObject {
                            put("url", dataUrl)
                            put("detail", "high")
                        })
                    })
                })
            })
        })
    }.toString()

    private fun post(body: String, apiKey: String): String {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 90_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Accept", "application/json")
        }

        try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { it.write(body) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                val message = runCatching {
                    json.parseToJsonElement(text).jsonObject["error"]
                        ?.jsonObject?.get("message")?.jsonPrimitive?.content
                }.getOrNull()
                error(
                    when (code) {
                        401 -> "API Key 无效，请检查后重试"
                        402 -> "账户余额不足"
                        429 -> "请求太频繁，稍后再试"
                        else -> message ?: "请求失败（HTTP $code）"
                    }
                )
            }
            return text
        } finally {
            connection.disconnect()
        }
    }

    private fun parseDrink(responseText: String): Drink {
        val root = json.parseToJsonElement(responseText).jsonObject
        val content = root["choices"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("message")?.jsonObject
            ?.get("content")?.jsonPrimitive?.content
            ?: error("模型没有返回内容，换张图再试试")

        val cleaned = content.trim()
            .removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val obj = json.parseToJsonElement(cleaned).jsonObject

        fun str(key: String): String =
            (obj[key] as? JsonPrimitive)?.content?.takeIf { it != "null" }?.trim().orEmpty()

        fun num(key: String): Double =
            (obj[key] as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0

        val category = DrinkCategory.entries
            .firstOrNull { it.label == str("category") } ?: DrinkCategory.Tea

        val toppings = (obj["toppings"] as? JsonArray)
            ?.mapNotNull { (it as? JsonPrimitive)?.content?.trim() }
            ?.filter { it.isNotBlank() }
            .orEmpty()

        val size = str("cupSize").takeIf { it in Catalog.sizesFor(category) }
            ?: Catalog.sizesFor(category).getOrElse(1) { "中杯" }
        val sugar = str("sugar").takeIf { it in Catalog.sugarOptions(category) }
            ?: Catalog.sugarOptions(category).first()
        val ice = str("ice").takeIf { it in Catalog.iceOptions(category) }
            ?: Catalog.iceOptions(category).first()

        return Drink(
            category = category.label,
            brand = str("brand"),
            name = str("name"),
            cupSize = size,
            sugar = sugar,
            ice = ice,
            toppings = toppings,
            price = num("price"),
            note = str("note"),
        )
    }

    private fun encodeImage(context: Context, uri: Uri): String? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0) return null

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_EDGE) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        val scaled = scaleDown(bitmap)
        val bytes = ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.toByteArray()
        }
        if (scaled != bitmap) scaled.recycle()
        bitmap.recycle()

        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }.getOrNull()

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
