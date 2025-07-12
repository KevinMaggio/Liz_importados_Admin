package com.refactoringlife.lizimportados.core.composablesLipsy

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.refactoringlife.lizimportados.R
import com.refactoringlife.lizimportados.core.utils.shimmerEffect
import com.refactoringlife.lizimportados.ui.theme.CardBackGround

@Composable
fun LipsyAsyncImage(modifier: Modifier,url: String?) {
    var isLoading by remember { mutableStateOf(true) }

    if (isLoading) { Box(modifier = modifier.shimmerEffect()) }

    if (url.isNullOrEmpty()) {
        isLoading = false
        Image(
            painter = painterResource(R.drawable.icon_default_clothes),
            contentDescription = "no image",
            contentScale = ContentScale.Inside,
            modifier = modifier
                .background(CardBackGround)
                .clip(RoundedCornerShape(12.dp))
                .padding(10.dp)
        )
    } else {
        AsyncImage(
            model = url,
            contentDescription = "generic image",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .background(CardBackGround)
                .clip(RoundedCornerShape(12.dp))
                .padding(10.dp),
            onState = { state ->
                isLoading = state is AsyncImagePainter.State.Loading
            }
        )
    }
}