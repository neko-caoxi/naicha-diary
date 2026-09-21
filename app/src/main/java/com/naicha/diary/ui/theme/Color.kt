package com.naicha.diary.ui.theme

import androidx.compose.ui.graphics.Color

val Cream = Color(0xFFFFF9F2)
val CreamDeep = Color(0xFFF7EADC)
val Caramel = Color(0xFFC98A5B)
val CaramelDeep = Color(0xFFA96C3F)
val CaramelLight = Color(0xFFFFE7CE)
val Pearl = Color(0xFF3B2B2B)
val PearlSoft = Color(0xFF6B5548)
val Muted = Color(0xFF9C8478)
val Taro = Color(0xFFB79CED)
val TaroLight = Color(0xFFEDE3FF)
val Matcha = Color(0xFF9CC49B)
val MatchaLight = Color(0xFFE4F2E3)
val Strawberry = Color(0xFFFF9EB5)
val StrawberryLight = Color(0xFFFFE4EB)
val Sky = Color(0xFF8FC7DE)
val SkyLight = Color(0xFFE2F2F9)
val Outline = Color(0xFFEADCCB)
val Success = Color(0xFF7CB342)

val BrandPalette = listOf(
    Caramel, Taro, Matcha, Strawberry, Sky,
    Color(0xFFF2B04A), Color(0xFFE07A5F), Color(0xFF81B29A),
)

fun accentFor(seed: Long): Color = when (seed % 8) {
    0L -> Caramel
    1L -> Taro
    2L -> Matcha
    3L -> Strawberry
    4L -> Sky
    5L -> Color(0xFFF2B04A)
    6L -> Color(0xFFE07A5F)
    else -> Color(0xFF81B29A)
}
