package com.example.tcgcarddetectionapp

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.sp
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

fun mapGameToMaxCopiesInDeck(game: String): Int {
    if (game == "yugioh") {
        return 3
    }
    else {
        return 4
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

val appTypography = Typography(
    displayLarge = TextStyle(
        color = Color.Black,
        fontSize = 50.sp,
        lineHeight = 60.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        textDirection = TextDirection.Ltr,
        lineBreak = LineBreak.Heading
    ),
    displayMedium = TextStyle(
        color = Color.DarkGray,
        fontSize = 40.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    ),
    displaySmall = TextStyle(
        color = Color.DarkGray,
        fontSize = 34.sp,
        fontWeight = FontWeight.Medium
    ),
    headlineLarge = TextStyle(
        color = Color.Black,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        color = Color.DarkGray,
        fontSize = 26.sp,
        fontWeight = FontWeight.Medium
    ),
    headlineSmall = TextStyle(
        color = Color.DarkGray,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyLarge = TextStyle(
        color = Color.Black,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        color = Color.Gray,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        color = Color.LightGray,
        fontSize = 14.sp,
        fontWeight = FontWeight.Light
    ),
    labelLarge = TextStyle(
        color = Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        color = Color.DarkGray,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    ),
    labelSmall = TextStyle(
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )
)

