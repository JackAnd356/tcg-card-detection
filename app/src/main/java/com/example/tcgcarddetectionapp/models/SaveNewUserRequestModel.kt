package com.example.tcgcarddetectionapp.models

data class SaveNewUserRequestModel(
    val username: String,
    val authenticationToken: String,
    val email: String,
    val storefront: Int
)
