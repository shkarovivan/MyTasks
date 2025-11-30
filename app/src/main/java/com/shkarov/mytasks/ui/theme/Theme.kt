package com.shkarov.mytasks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColorScheme(
    onBackground = Color.White,
    background = Color.Black,
    primary = Purple200,
    secondary = Teal200
)

private val LightColorPalette = lightColorScheme(
    onBackground = Color.DarkGray,
    background = Color.White,
    primary = Purple500,
    secondary = Teal200
)

@Composable
fun MyTasksTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}