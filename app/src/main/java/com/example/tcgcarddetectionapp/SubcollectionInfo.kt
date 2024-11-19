package com.example.tcgcarddetectionapp

data class SubcollectionInfo(
    val subcollectionid: String,
    var name: String,
    var totalValue: Double?,
    var physLoc: String,
    var cardCount: Int?,
    val game: String,
    var isDeck: Boolean,
    val userid: String,
)
