package com.example.tcgcarddetectionapp

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("authenticateUser")
    fun loginUser(@Body loginRequest: LoginRequestModel): Call<LoginResponseModel>

    @POST("saveUserStorefront")
    fun saveUserStorefront(@Body saveStorefrontRequestModel: SaveStorefrontRequestModel): Call<GenericSuccessErrorResponseModel>

    @POST("getUserCollection")
    fun getUserCollection(@Body userCollectionRequest: UserCollectionRequestModel): Call<Array<CardData>>

    @POST("getUserSubcollectionInfo")
    fun getUserSubcollectionInfo(@Body userSubcollectionInfoRequest: UserSubcollectionInfoRequestModel): Call<UserSubcollectionInfoResponseModel>

    @POST("saveUsername")
    fun saveUsername(@Body saveUsernameRequest: SaveUsernameRequestModel): Call<GenericSuccessErrorResponseModel>

    @POST("saveUserPass")
    fun saveUserPass(@Body savePasswordRequest: SavePasswordRequestModel): Call<GenericSuccessErrorResponseModel>

    @POST("saveUserEmail")
    fun saveUserEmail(@Body saveEmailRequest: SaveEmailRequestModel): Call<GenericSuccessErrorResponseModel>

    @POST("createNewUser")
    fun saveNewUser(@Body saveNewUserRequest: SaveNewUserRequestModel): Call<SaveNewUserResponseModel>

    @Multipart
    @POST("getCardInfo")
    fun getCardInfo(@Part image: MultipartBody.Part): Call<Array<CardData>>

    @POST("createUserSubcollection")
    fun createUserSubcollection(@Body createSubcollectionRequest: CreateSubcollectionModel): Call<GenericSuccessErrorResponseModel>

    @POST("deleteUser")
    fun deleteUser(@Body deleteUserRequest: UserCollectionRequestModel): Call<GenericSuccessErrorResponseModel>

    @Headers("Connection: close")
    @POST("getCardImage")
    fun getCardImage(@Body getCardImageRequest: GetCardImageRequestModel): Call<GetCardImageResponseModel>

    @POST("addToUserCollection")
    fun addToCollection(@Body addToCollectionRequest: AddRemoveCardModel): Call<GenericSuccessErrorResponseModel>

    @POST("addToUserSubcollection")
    fun addToUserSubcollection(@Body addToUserSubcollection: SaveToSubcollectionRequestModel): Call<SaveToSubcollectionResponseModel>

    @POST("removeFromUserCollection")
    fun removeFromCollection(@Body removeFromCollectionRequest: AddRemoveCardModel): Call <GenericSuccessErrorResponseModel>

    @POST("removeFromUserSubcollection")
    fun removeFromSubcollection(@Body removeFromSubcollectionRequest: AddRemoveCardModel): Call<GenericSuccessErrorResponseModel>
}