package com.example.tcgcarddetectionapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("authenticateUser")
    fun loginUser(@Body loginRequest: LoginRequestModel): Call<LoginResponseModel>
}