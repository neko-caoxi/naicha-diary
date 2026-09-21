package com.naicha.diary.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DrinkColors = lightColorScheme(
    primary = Caramel,
    onPrimary = Color.White,
    primaryContainer = CaramelLight,
    onPrimaryContainer = Color(0xFF5C3A1E),
    secondary = Taro,
    onSecondary = Color.White,
    secondaryContainer = TaroLight,
    onSecondaryContainer = Color(0xFF3B2A63),
    tertiary = Matcha,
    onTertiary = Color.White,
    tertiaryContainer = MatchaLight,
    onTertiaryContainer = Color(0xFF26401F),
    background = Cream,
    onBackground = Pearl,
    surface = Color.White,
    onSurface = Pearl,
    surfaceVariant = CreamDeep,
    onSurfaceVariant = PearlSoft,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFCF8),
    surfaceContainer = Color(0xFFFDF5EC),
    surfaceContainerHigh = Color(0xFFF9EFE3),
    surfaceContainerHighest = Color(0xFFF4E8DA),
    outline = Outline,
    outlineVariant = Color(0xFFF2E6D8),
    error = Color(0xFFE06C75),
    onError = Color.White,
    scrim = Color(0x66000000),
)

@Composable
fun DrinkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DrinkColors,
        typography = DrinkTypography,
        shapes = DrinkShapes,
        content = content,
    )
}
