package com.refactoringlife.lizimportados.features.home.presenter.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.refactoringlife.lizimportados.R
import com.refactoringlife.lizimportados.core.composablesLipsy.LipsyDivider
import com.refactoringlife.lizimportados.core.composablesLipsy.id
import com.refactoringlife.lizimportados.core.dto.response.ConfigResponse
import com.refactoringlife.lizimportados.core.utils.getComboMock
import com.refactoringlife.lizimportados.features.home.composables.WeeklyOffersSection
import com.refactoringlife.lizimportados.core.utils.getProductsMock
import com.refactoringlife.lizimportados.features.home.composables.CircleOptionsSection
import com.refactoringlife.lizimportados.features.home.composables.ComboSection
import com.refactoringlife.lizimportados.ui.theme.ColorWhiteLipsy

typealias filter = String
typealias id = String

@Composable
fun HomeDataView(
    modifier: Modifier = Modifier,
    configData: ConfigResponse,
    action: (filter, id) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorWhiteLipsy)
            .padding(start = 20.dp, top = 40.dp, bottom = 110.dp)
    ) {

        item {
            Column {
                Image(
                    painter = painterResource(R.drawable.liz_importados),
                    contentDescription = "",
                    contentScale = ContentScale.Inside
                )

                Spacer(Modifier.height(40.dp))

            }
        }

        if (configData.weeklyOffers) {
            item {
                WeeklyOffersSection(
                    title = stringResource(R.string.weekly_offers),
                    products = getProductsMock(),
                    action =  {action.invoke("ofertas", it)}
                )
                LipsyDivider()
            }
        }

        if (configData.circleOptions.isNotEmpty()) {
            item {
                CircleOptionsSection(
                    options = configData.circleOptions,
                    action = { filter ->
                        action(filter, "01")
                    }
                )
                LipsyDivider()
            }
        }

        item {
            ComboSection(getComboMock())
        }
    }
}