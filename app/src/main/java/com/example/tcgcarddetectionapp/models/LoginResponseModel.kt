package com.example.tcgcarddetectionapp.models

data class LoginResponseModel(
    var error: String? = null,
    var success: Int,
    var username: String? = null,
    var userid: String? = null,
    var email: String? = null,
)
