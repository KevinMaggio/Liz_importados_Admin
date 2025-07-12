package com.refactoringlife.lizimportados.core.composablesLipsy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refactoringlife.lizimportados.R
import com.refactoringlife.lizimportados.core.utils.capitalizeWords
import com.refactoringlife.lizimportados.core.utils.onValid
import com.refactoringlife.lizimportados.features.home.data.model.ProductModel
import com.refactoringlife.lizimportados.ui.theme.TextBlue
import com.refactoringlife.lizimportados.ui.theme.TextPrimary
import com.refactoringlife.lizimportados.ui.theme.TextSecondary

typealias id = String
@Composable
fun LipsyProduct(
    product: ProductModel,
    isAvailable: Boolean = false,
    addCartProduct: (String) -> Unit,
    action: (id) -> Unit
) {
    Column(modifier = Modifier.padding(20.dp)
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            action.invoke(product.id)
        }) {
        LipsyCardImage(product.images[0], modifier = Modifier.width(100.dp).height(150.dp))

        Spacer(modifier = Modifier.height(10.dp))

        product.name?.onValid {
            Text(
                text = it.capitalizeWords(),
                fontFamily = FontFamily(Font(R.font.montserrat_bold)),
                fontSize = 12.sp,
                lineHeight = 1.sp,
                color = TextSecondary
            )
        }

        product.brand?.onValid {
            Text(
                text = it.capitalizeWords(),
                fontFamily = FontFamily(Font(R.font.montserrat_regular)),
                fontSize = 10.sp,
                lineHeight = 6.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        product.oldPrice?.onValid {
            Text(
                text = it.capitalizeWords(),
                fontFamily = FontFamily(Font(R.font.montserrat_regular)),
                fontSize = 10.sp,
                lineHeight = 1.sp,
                textDecoration = TextDecoration.LineThrough,
                color = TextBlue
            )
        }

        product.price?.onValid {
            Text(
                text = it.capitalizeWords(),
                fontFamily = FontFamily(Font(R.font.montserrat_bold)),
                fontSize = 14.sp,
                lineHeight = 1.sp,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isAvailable) {
            Text(
                text = stringResource(R.string.add_cart),
                fontFamily = FontFamily(Font(R.font.montserrat_regular)),
                fontSize = 10.sp,
                lineHeight = 1.sp,
                color = TextBlue,
                modifier = Modifier.clickable {
                    addCartProduct(product.id)
                }
            )
        }
    }
}