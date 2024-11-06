package com.example.tcgcarddetectionapp

data class CreateSubcollectionModel(
    var userid: String,
    var name: String,
    var isDeck: Boolean,
    var game: String,
    var physLoc: String
)