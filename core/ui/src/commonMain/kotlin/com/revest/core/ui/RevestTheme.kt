package com.revest.core.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Revest Brand Colors ───────────────────────────────────────────────────────
private val Primary         = Color(0xFF1A237E)  // Deep indigo
private val OnPrimary       = Color.White
private val PrimaryContainer    = Color(0xFFDDE1FF)
private val OnPrimaryContainer  = Color(0xFF00105C)

private val Secondary       = Color(0xFF283593)
private val OnSecondary     = Color.White
private val SecondaryContainer  = Color(0xFFDFE0FF)
private val OnSecondaryContainer = Color(0xFF000F5C)

private val Tertiary        = Color(0xFF3F51B5)
private val TertiaryContainer   = Color(0xFFE3E5FF)

private val Error           = Color(0xFFBA1A1A)
private val ErrorContainer  = Color(0xFFFFDAD6)
private val OnError         = Color.White
private val OnErrorContainer = Color(0xFF410002)

private val SurfaceLight    = Color(0xFFFBF8FF)
private val OnSurfaceLight  = Color(0xFF1B1B1F)
private val SurfaceVariantLight = Color(0xFFE3E1EC)
private val OutlineLight    = Color(0xFF75737D)

// ── Dark palette ──────────────────────────────────────────────────────────────
private val PrimaryDark         = Color(0xFFBAC3FF)
private val OnPrimaryDark       = Color(0xFF00218A)
private val SurfaceDark         = Color(0xFF131318)
private val OnSurfaceDark       = Color(0xFFE5E1E9)
private val SurfaceVariantDark  = Color(0xFF47464F)

private val LightScheme = lightColorScheme(
    primary             = Primary,
    onPrimary           = OnPrimary,
    primaryContainer    = PrimaryContainer,
    onPrimaryContainer  = OnPrimaryContainer,
    secondary           = Secondary,
    onSecondary         = OnSecondary,
    secondaryContainer  = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary            = Tertiary,
    tertiaryContainer   = TertiaryContainer,
    error               = Error,
    errorContainer      = ErrorContainer,
    onError             = OnError,
    onErrorContainer    = OnErrorContainer,
    surface             = SurfaceLight,
    onSurface           = OnSurfaceLight,
    surfaceVariant      = SurfaceVariantLight,
    outline             = OutlineLight
)

private val DarkScheme = darkColorScheme(
    primary             = PrimaryDark,
    onPrimary           = OnPrimaryDark,
    primaryContainer    = Color(0xFF003399),
    onPrimaryContainer  = Color(0xFFDDE1FF),
    secondary           = Color(0xFFBBC4FF),
    onSecondary         = Color(0xFF00219A),
    tertiary            = Color(0xFFBFC6FF),
    surface             = SurfaceDark,
    onSurface           = OnSurfaceDark,
    surfaceVariant      = SurfaceVariantDark,
    outline             = Color(0xFF908E98),
    error               = Color(0xFFFFB4AB),
    onError             = Color(0xFF690005),
    errorContainer      = Color(0xFF93000A),
    onErrorContainer    = Color(0xFFFFDAD6)
)

@Composable
fun RevestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content
    )
}
