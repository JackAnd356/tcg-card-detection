package com.example.tcgcarddetectionapp.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap


data class CardData(
    val userid : String? = null,
    val cardid: String,
    val setcode: String,
    var quantity: Int,
    var rarity: String? = null,
    var possRarities: Array<String>? = null,
    var subcollections: Array<String>?,
    val game: String,
    val price: Double,
    var image: String?,
    var imageBitmap: ImageBitmap? = null,
    val cardname: String,
    val attribute: String? = null,
    val level: String? = null,
    val type: String? = null,
    val description: String? = null,
    val atk: String? = null,
    val def: String? = null,
    val cost: String? = null,
    val hp: String? = null,
    val abilities: Array<Ability>? = null,
    val evolvesFrom: String? = null,
    val retreat: Array<String>? = null,
    val weaknesses: Array<Weakness>? = null,
    val attacks: Array<Attack>? = null,
    var added: MutableState<Boolean> = mutableStateOf(true),
    var purchaseurl: String? = null,
    val frameType: String? = null,
    val color: Array<String>? = null,
    val legalities: Legalities? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is CardData) return false
        return this.cardname == other.cardname && this.cardid == other.cardid && this.setcode == other.setcode && this.rarity == other.rarity
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

data class Ability(
    val name: String,
    val text: String,
    val type: String
)

data class Legalities(
    val standard: String,
    val future: String,
    val historic: String,
    val timeless: String,
    val gladiator: String,
    val pioneer: String,
    val explorer: String,
    val modern: String,
    val legacy: String,
    val pauper: String,
    val vintage: String,
    val penny: String,
    val commander: String,
    val oathbreaker: String,
    val standardbrawl: String,
    val brawl: String,
    val alchemy: String,
    val paupercommander: String,
    val duel: String,
    val oldschool: String,
    val premodern: String,
    val predh: String,
)