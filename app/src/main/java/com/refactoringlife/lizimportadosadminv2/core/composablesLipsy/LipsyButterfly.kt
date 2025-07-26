package com.refactoringlife.lizimportados.core.composablesLipsy

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun LottieAnimationButterfly(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lootie_butterfly.json"))
    val progress by animateLottieCompositionAsState(composition,
        iterations = LottieConstants.IterateForever)

    LottieAnimation(
        composition,
        progress,
        modifier = modifier
            .size(60.dp)
    )
}