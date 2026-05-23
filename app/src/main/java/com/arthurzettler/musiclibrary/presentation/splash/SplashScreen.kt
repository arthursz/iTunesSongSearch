package com.arthurzettler.musiclibrary.presentation.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.arthurzettler.musiclibrary.R
import com.arthurzettler.musiclibrary.ui.theme.MusicLibraryTheme
import com.arthurzettler.musiclibrary.ui.theme.SplashBlack
import com.arthurzettler.musiclibrary.ui.theme.SplashGradientTeal
import kotlinx.coroutines.delay

internal const val SplashDurationMs = 1200L
internal const val IconEnterDurationMs = 220

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val splashIconDescription = stringResource(R.string.splash_icon_description)
    var showIcon by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showIcon = true
        delay(SplashDurationMs)
        onFinished()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBlack)
    ) {
        val density = LocalDensity.current
        val gradientCenter = with(density) {
            Offset(maxWidth.toPx(), 0f)
        }
        val gradientRadius = with(density) {
            maxOf(maxWidth.toPx(), maxHeight.toPx()) * 0.75f
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            SplashGradientTeal,
                            Color.Transparent
                        ),
                        center = gradientCenter,
                        radius = gradientRadius
                    )
                )
        )

        AnimatedVisibility(
            visible = showIcon,
            enter = scaleIn(
                initialScale = 0.88f,
                animationSpec = tween(
                    durationMillis = IconEnterDurationMs,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = IconEnterDurationMs,
                    easing = FastOutSlowInEasing
                )
            ),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_splash),
                contentDescription = splashIconDescription,
                modifier = Modifier
                    .fillMaxWidth(0.42f)
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    MusicLibraryTheme {
        SplashScreen(onFinished = {})
    }
}
