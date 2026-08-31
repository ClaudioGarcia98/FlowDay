package dev.flowday.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Blue400,
    onPrimary = Neutral950,
    primaryContainer = Blue700,
    onPrimaryContainer = Blue200,
    background = Neutral950,
    onBackground = Neutral100,
    surface = Neutral900,
    onSurface = Neutral100,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral300,
    outline = Neutral700,
    error = Error,
)

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Neutral50,
    primaryContainer = Blue200,
    onPrimaryContainer = Blue700,
    background = Neutral50,
    onBackground = Neutral950,
    surface = Neutral50,
    onSurface = Neutral950,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral700,
    outline = Neutral200,
    error = Error,
)

@Composable
fun FlowDayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FlowDayTypography,
        content = content
    )
}