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
    val amt = card.subcollections!!.count { it == subcolInfo.subcollectionid}
    if (adding) {
        if (subcolInfo.totalValue != null) subcolInfo.totalValue = subcolInfo.totalValue!! + (card.price * amt)
        else subcolInfo.totalValue = (card.price * amt)

        if (subcolInfo.cardCount != null) subcolInfo.cardCount = subcolInfo.cardCount!! + amt
        else subcolInfo.cardCount = amt
    } else {
        if (subcolInfo.totalValue != null) subcolInfo.totalValue = subcolInfo.totalValue!! - (card.price * amt)
        else println("Removing Card From Subcollection with no Price??")

        if (subcolInfo.cardCount != null) subcolInfo.cardCount = subcolInfo.cardCount!! - amt
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

fun mapMTGColorToIcon(color: Char): Int {
    return when (color) {
        'W' -> R.drawable.white_icon
        'U' -> R.drawable.blue_icon
        'B' -> R.drawable.black_icon
        'R' -> R.drawable.red_icon
        'G' -> R.drawable.green_icon
        else -> R.drawable.colorless_icon
    }
}

fun stripColorString(colors: String): CharArray {
    val filtered = colors.filterNot {
        it.isWhitespace() || it == '{' || it == '}' || it == ','
    }

    return filtered.toCharArray()
}

fun mapPokemonTypeToIcon(type: String): Int {
    return when (type) {
        "Normal" -> R.drawable.normal_energy_icon
        "Dark" -> R.drawable.dark_energy_icon
        "Electric" -> R.drawable.electric_energy_icon
        "Fairy" -> R.drawable.fairy_energy_icon
        "Fighting" -> R.drawable.fighting_energy_icon
        "Fire" -> R.drawable.fire_energy_icon
        "Grass" -> R.drawable.grass_energy_icon
        "Psychic" -> R.drawable.psychic_energy_icon
        "Metal" -> R.drawable.steel_energy_icon
        "Water" -> R.drawable.water_energy_icon
        else -> R.drawable.normal_energy_icon
    }
}

fun arrToPrintableString(arr: Array<String>): String {
    var str = ""
    arr.forEach {
            itm ->
        str += "$itm,"
    }
    return str
}


