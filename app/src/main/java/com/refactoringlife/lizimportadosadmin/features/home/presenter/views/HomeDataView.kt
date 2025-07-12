package com.refactoringlife.lizimportadosadmin.features.home.presenter.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.refactoringlife.lizimportadosadmin.ui.theme.ColorWhiteLipsy


@Composable
fun HomeDataView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWhiteLipsy)
            .padding(start = 20.dp, top = 40.dp, bottom = 110.dp)
    ) {
        Text("Este es el Home")
    }
}