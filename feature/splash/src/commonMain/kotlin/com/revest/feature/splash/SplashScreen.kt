package com.revest.feature.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 2_200L

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {

    val iconScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "icon_scale"
    )

    val alpha    = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))
        delay(200)
        textAlpha.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        delay(SPLASH_DURATION_MS - 800)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B6E),
                        Color(0xFF1A237E),
                        Color(0xFF283593)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(96.dp)
                    .scale(iconScale)
                    .alpha(alpha.value),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.15f),
                tonalElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Store,
                        contentDescription = "Revest Logo",
                        modifier = Modifier.size(56.dp),
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.alpha(textAlpha.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "REVEST",
                    style = MaterialTheme.typography.displaySmall.copy(
                        letterSpacing = 8.sp,
                        fontWeight    = FontWeight.ExtraBold
                    ),
                    color = Color.White
                )
                Text(
                    text = "Product Catalog",
                    style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 2.sp),
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        Text(
            text     = "v1.0.0",
            style    = MaterialTheme.typography.labelSmall,
            color    = Color.White.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(textAlpha.value)
        )
    }
}
