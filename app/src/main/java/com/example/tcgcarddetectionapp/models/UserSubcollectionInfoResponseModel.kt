package com.example.tcgcarddetectionapp.models

data class UserSubcollectionInfoResponseModel(
    val success: Int,
    val subcollections: Array<SubcollectionInfo>,
)
