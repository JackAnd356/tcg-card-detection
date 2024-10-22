package com.example.tcgcarddetectionapp

data class SubcollectionInfo(
    val subcollectionid: String,
    val name: String,
    var totalValue: Double?,
    val physLoc: String,
    var cardCount: Int?,
    val game: String,
    val isDeck: Boolean,
    val userid: String,
)
