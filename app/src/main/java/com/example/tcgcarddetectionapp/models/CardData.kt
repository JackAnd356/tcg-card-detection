package com.example.tcgcarddetectionapp.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf


data class CardData(
    val userid : String,
    val cardid: String,
    val setcode: String,
    var quantity: Int,
    var rarity: String? = null,
    var possRarities: Array<String>? = null,
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
    var added: MutableState<Boolean> = mutableStateOf(true),
    var purchaseurl: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is CardData) return false
        return this.cardname == other.cardname && this.cardid == other.cardid && this.setcode == other.setcode
    }

    override fun hashCode(): Int {
        return (this.cardname + this.cardid + this.setcode).hashCode()
    }
}

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