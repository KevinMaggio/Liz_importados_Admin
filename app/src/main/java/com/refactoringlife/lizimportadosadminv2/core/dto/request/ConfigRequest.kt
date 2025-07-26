package com.refactoringlife.lizimportadosadminv2.core.dto.request

import kotlinx.serialization.SerialName

data class ConfigRequest(
    @SerialName("combo")
    val combos : List<Combo>?,
    @SerialName("options")
    val circleOptions: List<String>,
    @SerialName("weekly_offers")
    val weeklyOffers: Boolean
){
    data class Combo(
        @SerialName("show_combo")
        val showCombo: Boolean?,
        @SerialName("combo_id")
        val comboID: List<String>?
    )
}