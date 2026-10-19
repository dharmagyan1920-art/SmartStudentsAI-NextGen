package com.smartstudents.nextgen.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F51B5),
    primaryContainer = Color(0xFFDDE1FF),
    secondaryContainer = Color(0xFFFFE9A8),
    tertiaryContainer = Color(0xFFFFDDB8),
)

@Composable
fun SmartStudentsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else LightColors,
        content = content,
    )
}
