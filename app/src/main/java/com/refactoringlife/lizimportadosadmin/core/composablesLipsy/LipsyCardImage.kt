package com.refactoringlife.lizimportados.core.composablesLipsy

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.refactoringlife.lizimportados.R
import com.refactoringlife.lizimportados.core.utils.shimmerEffect
import com.refactoringlife.lizimportados.ui.theme.CardBackGround

@Composable
fun LipsyCardImage(url: String?, modifier: Modifier= Modifier) {
    var isLoading by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier
            .height(180.dp)
            .width(150.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
            .background(CardBackGround)
    ) {
        if (isLoading) {
            Box(
                modifier = modifier
                    .height(180.dp)
                    .width(150.dp)
                    .shimmerEffect()
                    .clip(RoundedCornerShape(12.dp))
                    .padding(10.dp)
            )
        }

        if (url.isNullOrEmpty()) {
            isLoading = false
            Image(
                painter = painterResource(R.drawable.icon_default_clothes),
                contentDescription = "no image",
                contentScale = ContentScale.Inside,
                modifier = modifier
                    .height(180.dp)
                    .width(150.dp)
                    .background(CardBackGround)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(10.dp)
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = "generic image",
                contentScale = ContentScale.Inside,
                modifier = modifier
                    .height(180.dp)
                    .width(150.dp)
                    .background(CardBackGround)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(10.dp),
                onState = { state ->
                    isLoading = state is AsyncImagePainter.State.Loading
                }
            )
        }

    }
}
