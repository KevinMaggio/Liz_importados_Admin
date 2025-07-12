package com.refactoringlife.lizimportados.core.utils

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize

@SuppressLint("ModifierFactoryUnreferencedReceiver")
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }

    val transition = rememberInfiniteTransition(
        label = EMPTY
    )
    val startOffSetX by transition.animateFloat(
        initialValue = (SHIMMER_INITIAL_CONSTANT * size.width).toFloat(),
        targetValue = SHIMMER_TARGET_CONSTANT * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = SHIMMER_DURATION,
                delayMillis = ZERO,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = EMPTY
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color(CUSTOM_GRAY_SHIMMER),
                Color.Transparent
            ),
            start = Offset(startOffSetX, ZERO_FLOAT),
            end = Offset(startOffSetX + size.width.toFloat(), ZERO_FLOAT)
        )
    ).onGloballyPositioned {
        size = it.size
    }
}