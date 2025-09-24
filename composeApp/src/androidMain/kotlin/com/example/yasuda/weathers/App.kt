package com.example.yasuda.weathers

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    WeathersTheme {
        WeatherScreen()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeathersTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (!darkTheme) { lightColorScheme() } else { darkColorScheme() }
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        content = content
    )
}