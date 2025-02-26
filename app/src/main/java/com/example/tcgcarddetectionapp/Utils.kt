package com.example.tcgcarddetectionapp

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
        "Pokemon" -> "Yu-Gi-Oh!"
        "mtg" -> "Magic the Gathering"
        "pokemon" -> "Pokemon"
        else -> "New Game"
    }
}


