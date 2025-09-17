package com.example.tcgcarddetectionapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = White,
    secondary = DarkGreen, //Accept Button
    tertiary = DarkBlue, //Hyperlink Text
    background = Dark,
    primaryContainer = DarkGrey,
    error = DarkRed,
    secondaryContainer = DarkGrey, //Text field container
    onSecondaryContainer = White, //Text field text
    tertiaryContainer = DarkBlue, //Login Button
    onTertiaryContainer = White,
    inverseSurface = DarkGrey, //Disabled Buttons
    outline = White,
    errorContainer = DarkRed,
    onErrorContainer = White,
    outlineVariant = White,
    surfaceContainer = DarkBlue,
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    secondary = LightGreen, //Accept Button
    tertiary = LightBlue, //Hyperlink Text
    background = White,
    primaryContainer = Gray,
    error = LightRed,
    secondaryContainer = Gray, //Text field container
    onSecondaryContainer = Black, //Text field text
    tertiaryContainer = LightBlue, //Login Button
    onTertiaryContainer = White,
    inverseSurface = DarkGrey, //Disabled Buttons
    outline = Black,
    errorContainer = LightRed,
    onErrorContainer = White,
    outlineVariant = Gray,
    surfaceContainer = LightBlue,
)

@Composable
fun TCGCardDetectionAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}