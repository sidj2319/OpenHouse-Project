package com.example.catalogue.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// All look-and-feel lives here. When the creative team's designs land, change the
// palette/typography in this file; screens only reference MaterialTheme tokens.
private val LightColors = lightColorScheme(primary = Color(0xFF1F5FBF))
private val DarkColors = darkColorScheme(primary = Color(0xFF9CC2FF))

@Composable
fun CatalogueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
