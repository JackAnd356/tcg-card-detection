package com.example.tcgcarddetectionapp

data class UpdateUserSubcollectionRequestModel(
    val name: String,
    val physLoc: String,
    val isDeck: Boolean,
    val subcollectionid: String,
)
