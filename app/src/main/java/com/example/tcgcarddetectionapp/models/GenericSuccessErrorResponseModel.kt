package com.example.tcgcarddetectionapp.models

data class GenericSuccessErrorResponseModel(
    var error: String? = null,
    var success: Int,
)
