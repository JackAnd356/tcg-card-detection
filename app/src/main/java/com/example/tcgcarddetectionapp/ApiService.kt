package com.example.tcgcarddetectionapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("authenticateUser")
    fun loginUser(@Body loginRequest: LoginRequestModel): Call<LoginResponseModel>

    @POST("saveUserStorefront")
    fun saveUserStorefront(@Body saveStorefrontRequestModel: SaveStorefrontRequestModel): Call<GenericSuccessErrorResponseModel>

    @POST("getUserCollection")
    fun getUserCollection(@Body userCollectionRequest: UserCollectionRequestModel): Call<Array<Card>>

    @POST("getUserSubcollectionInfo")
    fun getUserSubcollectionInfo(@Body userSubcollectionInfoRequest: UserSubcollectionInfoRequestModel): Call<UserSubcollectionInfoResponseModel>

    @POST("saveUsername")
    fun saveUsername(@Body saveUsernameRequest: SaveUsernameRequestModel): Call<GenericSuccessErrorResponseModel>

    @POST("saveUserPass")
    fun saveUserPass(@Body savePasswordRequest: SavePasswordRequestModel): Call<GenericSuccessErrorResponseModel>

    @POST("saveUserEmail")
    fun saveUserEmail(@Body saveEmailRequest: SaveEmailRequestModel): Call<GenericSuccessErrorResponseModel>
}