package com.example.tcgcarddetectionapp

data class SubcollectionInfo(
    val subcollectionid: String,
    val name: String,
    val totalValue: Double?,
    val physLoc: String,
    val cardCount: Int?,
    val game: String,
    val isDeck: Boolean,
    val userid: String,
)
