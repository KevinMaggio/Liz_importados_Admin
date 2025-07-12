package com.refactoringlife.lizimportados.features.home.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.refactoringlife.lizimportados.R
import com.refactoringlife.lizimportados.core.composablesLipsy.LipsyProduct
import com.refactoringlife.lizimportados.features.home.data.model.ProductModel
import com.refactoringlife.lizimportados.core.utils.capitalizeWords
import com.refactoringlife.lizimportados.core.utils.onValid
import com.refactoringlife.lizimportados.ui.theme.TextBlue

typealias id = String

@Composable
fun WeeklyOffersSection (
    title : String?,
    products: List<ProductModel>,
    action: (id) -> Unit
){
    title?.onValid {
        Text(
            text = it.capitalizeWords(),
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            fontSize = 14.sp,
            color = TextBlue
        )
    }

    LazyRow(modifier = Modifier.fillMaxWidth()){
        items(products){product ->
            LipsyProduct(
                product=product,
                addCartProduct = {},
                action = { action.invoke(product.id) }
            )
        }
    }
}
