package com.refactoringlife.lizimportados.features.home.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refactoringlife.lizimportados.R
import com.refactoringlife.lizimportados.core.composablesLipsy.LipsyAsyncImage
import com.refactoringlife.lizimportados.core.composablesLipsy.LipsyDivider
import com.refactoringlife.lizimportados.features.home.data.model.CombosModel
import com.refactoringlife.lizimportados.ui.theme.CardBackGround
import com.refactoringlife.lizimportados.ui.theme.TextBlue
import com.refactoringlife.lizimportados.ui.theme.TextPrimary
import com.refactoringlife.lizimportados.ui.theme.TextSecondary

@Composable
fun ComboItem(
    combo: CombosModel.ComboModel,
    lastItem: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackGround),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LipsyAsyncImage(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                combo.firstProduct.image
            )

            Image(
                painter = painterResource(R.drawable.icon_plus),
                contentDescription = "No description",
                modifier = Modifier.size(15.dp)
            )

            LipsyAsyncImage(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                combo.secondProduct.image
            )

            Spacer(
                modifier = Modifier
                    .weight(0.5f)
            )

            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$" + combo.oldPrice.toString(),
                    fontSize = 10.sp,
                    fontFamily = FontFamily(Font(R.font.montserrat_regular)),
                    textDecoration = TextDecoration.LineThrough,
                    color = TextBlue
                )
                Text(
                    text = "$" + combo.price.toString(),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.montserrat_bold)),
                    color = TextPrimary
                )
            }
        }
        Row {
            Text(
                text = "${combo.firstProduct.brand} + ${combo.secondProduct.brand}",
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.montserrat_bold)),
                color = TextPrimary
            )
        }
        Row {
            Text(
                text = "${combo.firstProduct.description} + ${combo.secondProduct.description}",
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.montserrat_regular)),
                color = TextSecondary
            )
        }
    }

    if (!lastItem) {
        LipsyDivider()
    } else {
        Spacer(Modifier.height(10.dp))
    }
}
