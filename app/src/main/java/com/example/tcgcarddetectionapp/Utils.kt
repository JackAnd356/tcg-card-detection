package com.example.tcgcarddetectionapp

import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.example.tcgcarddetectionapp.models.CardData
import com.example.tcgcarddetectionapp.models.SubcollectionInfo

const val api_url = "http://10.0.2.2:5000"

fun <E> List<E>.remove(elem: E): List<E> {
    val lst = ArrayList<E>()
    var found = false
    for (e in this) {
        if (found || e != elem) lst.add(e)
        else found = true
    }
    return lst
}

fun updateSubcollectionInfo(subcolInfo: SubcollectionInfo, card: CardData, quantity: Int, adding: Boolean) {
    if (adding) {
        if (subcolInfo.totalValue != null) subcolInfo.totalValue = subcolInfo.totalValue!! + (card.price * quantity)
        else subcolInfo.totalValue = (card.price * quantity)

        if (subcolInfo.cardCount != null) subcolInfo.cardCount = subcolInfo.cardCount!! + quantity
        else subcolInfo.cardCount = quantity
    } else {
        if (subcolInfo.totalValue != null) subcolInfo.totalValue = subcolInfo.totalValue!! - (card.price * quantity)
        else println("Removing Card From Subcollection with no Price??")

        if (subcolInfo.cardCount != null) subcolInfo.cardCount = subcolInfo.cardCount!! - quantity
        else println("Removing Card from Empty Subcollection??")
    }
}

fun mapGameToFullName(game : String): String {
    return when (game) {
        "yugioh" -> "Yu-Gi-Oh!"
        "mtg" -> "Magic the Gathering"
        "pokemon" -> "Pokemon"
        else -> "New Game"
    }
}

fun mapRarityToPlaceholder(rarity: String): Pair<Int, Int> {
    return when (rarity) {
        "Common" -> Pair(R.drawable.common, R.string.yugioh_common_description)
        "Rare" -> Pair(R.drawable.common, R.string.yugioh_rare_description)
        "Super Rare" -> Pair(R.drawable.common, R.string.yugioh_super_rare_description)
        "Ultra Rare" -> Pair(R.drawable.common, R.string.yugioh_ultra_rare_description)
        "Ultimate Rare" -> Pair(R.drawable.common, R.string.yugioh_ultimate_rare_description)
        "Secret Rare" -> Pair(R.drawable.common, R.string.yugioh_secret_rare_description)
        "Ghost Rare" -> Pair(R.drawable.common, R.string.yugioh_ghost_rare_description)
        "Starlight Rare" -> Pair(R.drawable.common, R.string.yugioh_starlight_rare_description)
        else -> Pair(R.drawable.nocardimage, R.string.no_card_error)
    }
}

fun mapRarityToRibbon(rarity: String?): Int {
    return when (rarity) {
        "Secret Rare" -> R.drawable.secret_rare_ribbon
        "Ultra Rare" -> R.drawable.ultra_rare_ribbon
        else -> R.drawable.common_ribbon
    }
}


