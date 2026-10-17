package ie.suric.suricapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Oat = Color(0xFFF7F3EB)
private val Paper = Color(0xFFFFFEFC)
private val Ink = Color(0xFF292B25)
private val Sage = Color(0xFF49664E)
private val Olive = Color(0xFF75804B)
private val Terracotta = Color(0xFFAD6248)
private val Night = Color(0xFF171B19)
private val NightSurface = Color(0xFF232925)
private val LavenderSage = Color(0xFFB6C9B7)

@Composable
fun SuricTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) darkColorScheme(
        primary = LavenderSage,
        onPrimary = Color(0xFF203127),
        secondary = Color(0xFFD0BA96),
        tertiary = Color(0xFFE0A28A),
        background = Night,
        onBackground = Color(0xFFF2F0E9),
        surface = NightSurface,
        onSurface = Color(0xFFF2F0E9),
        surfaceVariant = Color(0xFF343D36),
        onSurfaceVariant = Color(0xFFD3D7CF),
        primaryContainer = Color(0xFF35483B),
        onPrimaryContainer = Color(0xFFE3EEE1),
        error = Color(0xFFFFB4AB)
    ) else lightColorScheme(
        primary = Sage,
        onPrimary = Color.White,
        secondary = Olive,
        tertiary = Terracotta,
        background = Oat,
        onBackground = Ink,
        surface = Paper,
        onSurface = Ink,
        surfaceVariant = Color(0xFFEAE7DE),
        onSurfaceVariant = Color(0xFF53574E),
        primaryContainer = Color(0xFFDCE8D8),
        onPrimaryContainer = Color(0xFF223A2A),
        error = Color(0xFFBA1A1A)
    )
    MaterialTheme(colorScheme = colors, content = content)
}
