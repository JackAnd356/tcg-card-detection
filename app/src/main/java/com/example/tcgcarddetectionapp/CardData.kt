package com.example.tcgcarddetectionapp


data class CardData(
    val userid : String,
    val cardid: String,
    val setcode: String,
    val quantity: Int,
    val rarity: String,
    var subcollections: Array<String>?,
    val game: String,
    val price: Double,
    var image: String?,
    val cardname: String,
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
)

data class Weakness(
    val type: String,
    val value: String,
)

data class Attack(
    val name: String,
    val cost: Array<String>,
    val damage: String,
    val text: String?,
)