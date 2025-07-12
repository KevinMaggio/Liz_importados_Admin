package com.refactoringlife.lizimportados.core.composablesLipsy

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.refactoringlife.lizimportados.ui.theme.ColorGrayLipsy

@Composable
fun LipsyDivider(){
    Spacer(Modifier.height(10.dp))

    HorizontalDivider(
        color = ColorGrayLipsy,
        modifier = Modifier.padding(end = 20.dp)
    )

    Spacer(Modifier.height(10.dp))
}