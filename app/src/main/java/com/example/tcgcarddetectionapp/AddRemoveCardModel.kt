package com.example.tcgcarddetectionapp

data class AddRemoveCardModel (
    val userid: String,
    val game: String,
    val cardid: String,
    val setcode : String,
    val cardname: String,
    val quantity: Int,
    val rarity: String? = null,
    val price: Double,
    val attribute: String? = null,
    val level: String? = null,
    val type: String? = null,
    val description: String? = null,
    val atk: String? = null,
    val def: String? = null,
    val cost: String? = null,
    val hp: String? = null,
    val retreat: Array<String>? = null,
    val weaknesses: Array<Weakness>? = null,
    val attacks: Array<Attack>? = null,
    val subcollection: String? = null
)