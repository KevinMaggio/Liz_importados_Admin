package com.refactoringlife.lizimportados.features.home.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.refactoringlife.lizimportados.R
import com.refactoringlife.lizimportados.features.home.data.model.CombosModel
import com.refactoringlife.lizimportados.ui.theme.CardBackGround
import com.refactoringlife.lizimportados.ui.theme.TextBlue

@Composable
fun ComboSection(
    combo: CombosModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
    ) {
        Text(
            text = stringResource(R.string.unmissable_combo),
            fontFamily = FontFamily(Font(R.font.montserrat_bold)),
            fontSize = 16.sp,
            color = TextBlue
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 8.dp)
                .background(CardBackGround)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(corner = CornerSize(8.dp)))
                    .background(CardBackGround)
            ) {
                combo.combos.forEachIndexed { index, comboItem ->
                    ComboItem(
                        combo = comboItem,
                        lastItem = index == combo.combos.size - 1
                    )
                }
            }
        }
    }
}
