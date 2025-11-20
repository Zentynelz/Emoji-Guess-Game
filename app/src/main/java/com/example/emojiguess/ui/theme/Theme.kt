package com.example.emojiguess.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Tema Neón Violeta - Siempre oscuro con colores vibrantes
private val NeonColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color.White,
    primaryContainer = DarkPurple,
    onPrimaryContainer = LightPurple,
    
    secondary = NeonViolet,
    onSecondary = Color.White,
    secondaryContainer = DeepPurple,
    onSecondaryContainer = MediumPurple,
    
    tertiary = NeonPink,
    onTertiary = Color.White,
    
    background = DarkBackground,
    onBackground = LightPurple,
    
    surface = DarkSurface,
    onSurface = LightPurple,
    surfaceVariant = DeepPurple,
    onSurfaceVariant = MediumPurple,
    
    error = NeonPink,
    onError = Color.White,
    
    outline = NeonViolet,
    outlineVariant = DarkPurple
)

@Composable
fun EmojiGuessTheme(
    darkTheme: Boolean = true, // Siempre oscuro para el efecto neón
    dynamicColor: Boolean = false, // Deshabilitado para mantener colores neón
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NeonColorScheme,
        typography = Typography,
        content = content
    )
}
