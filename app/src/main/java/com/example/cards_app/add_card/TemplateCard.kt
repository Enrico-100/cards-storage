package com.example.cards_app.add_card

import com.example.cards_app.R

data class TemplateCard(
    val nameOfCard: String,
    val color: String,
    val logoResId: Int
)
object Templates {
     val list = listOf(
        TemplateCard("spar", "#FFFFFF", R.drawable.logo_spar),
        TemplateCard("ikea", "#0057AD", R.drawable.logo_ikea),
        TemplateCard("lidl", "#0050AA", R.drawable.logo_lidl),
        TemplateCard("dm", "#FFFFFF", R.drawable.logo_dm),
        TemplateCard("muller", "#FFFFFF", R.drawable.logo_muller),
        TemplateCard("agraria koper", "#FFFFFF", R.drawable.logo_agrariakoper),
        TemplateCard("bauhaus", "#FFFFFF", R.drawable.logo_bauhaus),
        TemplateCard("hervis", "#FFFFFF", R.drawable.logo_hervis),
        TemplateCard("merkur", "#FFDF01", R.drawable.logo_merkur),
        TemplateCard("obi", "#FFFFFF", R.drawable.logo_obi),
        TemplateCard("tus", "#00803B", R.drawable.logo_tus)
    )
}
