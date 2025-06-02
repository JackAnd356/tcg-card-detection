package com.example.tcgcarddetectionapp.models

data class ExportModel(
    val cards: Array<CardData>,
    val subColID: String,
    val subColName: String,
    val email: String,
)
