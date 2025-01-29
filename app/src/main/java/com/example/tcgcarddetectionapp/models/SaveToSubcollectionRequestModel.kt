package com.example.tcgcarddetectionapp.models

data class SaveToSubcollectionRequestModel(
    val userid: String,
    val cardid: String,
    val setcode: String,
    val game: String,
    val rarity: String,
    val subcollection: String,
)
