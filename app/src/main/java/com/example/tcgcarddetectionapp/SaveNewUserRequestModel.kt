package com.example.tcgcarddetectionapp

data class SaveNewUserRequestModel(
    val username: String,
    val authenticationToken: String,
    val email: String,
    val storefront: Int
)
