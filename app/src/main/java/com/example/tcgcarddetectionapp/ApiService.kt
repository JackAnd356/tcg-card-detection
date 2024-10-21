package com.example.tcgcarddetectionapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("authenticateUser")
    fun loginUser(@Body loginRequest: LoginRequestModel): Call<LoginResponseModel>

    @POST("saveUserStorefront")
    fun saveUserStorefront(@Body saveStorefrontRequestModel: SaveStorefrontRequestModel): Call<SaveStorefrontResponseModel>

    @POST("getUserCollection")
    fun getUserCollection(@Body userCollectionRequest: UserCollectionRequestModel): Call<UserCollectionResponseModel>

    @POST("getUserSubcollectionInfo")
    fun getUserSubcollectionInfo(@Body userSubcollectionInfoRequest: UserSubcollectionInfoRequestModel): Call<UserSubcollectionInfoResponseModel>
}