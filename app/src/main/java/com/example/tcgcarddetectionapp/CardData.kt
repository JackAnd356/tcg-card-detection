package com.example.tcgcarddetectionapp

data class CardData(
    val userid : String,
    val cardid: String,
    val setcode: String,
    val quantity: Int,
    val rarity: String,
    val subcollections: Array<String>?,
    val game: String,
    val price: Double,
)
