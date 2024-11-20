package com.example.tcgcarddetectionapp

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