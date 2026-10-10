package dz.irrigationdz.ui

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

// ------------------------------------------------------------------
// IrrigationDZ 2026 design tokens.
//
// These tokens are the single source of truth for the app theme and map
// 1:1 to the shared Penpot library "DZ-Apps 2026" (see docs/DESIGN.md):
//   seed/primary      -> Penpot color token  primary-40
//   seed/secondary    -> Penpot color token  secondary-40
//   seed/tertiary     -> Penpot color token  tertiary-40
//   corner/medium=12  corner/large=16  corner/xl=28 (cards, sheets, FAB)
//   spacing scale 4/8/12/16/24 dp
// Dark mode: system-follow; dynamic color on Android 12+.
// ------------------------------------------------------------------

private val LightScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8EAC9),
    onPrimaryContainer = Color(0xFF0B3D12),
    secondary = Color(0xFF0277BD),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC4E5FA),
    onSecondaryContainer = Color(0xFF063A57),
    tertiary = Color(0xFF795548),
    surface = Color(0xFFF7F9F4),
    surfaceContainerLowest = Color(0xFFFFFFFF)
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF8BD39A),
    onPrimary = Color(0xFF0B3D12),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFC8EAC9),
    secondary = Color(0xFF7CC4F2),
    onSecondary = Color(0xFF063A57),
    secondaryContainer = Color(0xFF014A7A),
    onSecondaryContainer = Color(0xFFC4E5FA),
    tertiary = Color(0xFFCFB4A4),
    surface = Color(0xFF10140F),
    surfaceContainerLowest = Color(0xFF0B0E0A)
)

@Composable
fun IrrigationDzTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkScheme
        else -> LightScheme
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
