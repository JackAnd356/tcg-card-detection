package com.example.tcgcarddetectionapp

import com.example.tcgcarddetectionapp.models.AddRemoveCardModel
import com.example.tcgcarddetectionapp.models.CardData
import com.example.tcgcarddetectionapp.models.CreateSubcollectionModel
import com.example.tcgcarddetectionapp.models.DeleteUserSubcollectionRequestModel
import com.example.tcgcarddetectionapp.models.GenericSuccessErrorResponseModel
import com.example.tcgcarddetectionapp.models.GetCardImageRequestModel
import com.example.tcgcarddetectionapp.models.GetCardImageResponseModel
import com.example.tcgcarddetectionapp.models.LoginFBRequestModel
import com.example.tcgcarddetectionapp.models.LoginGoogleRequestModel
import com.example.tcgcarddetectionapp.models.LoginRequestModel
import com.example.tcgcarddetectionapp.models.LoginResponseModel
import com.example.tcgcarddetectionapp.models.SaveEmailRequestModel
import com.example.tcgcarddetectionapp.models.SaveNewUserRequestModel
import com.example.tcgcarddetectionapp.models.SaveNewUserResponseModel
import com.example.tcgcarddetectionapp.models.SavePasswordRequestModel
import com.example.tcgcarddetectionapp.models.SaveStorefrontRequestModel
import com.example.tcgcarddetectionapp.models.SaveToSubcollectionRequestModel
import com.example.tcgcarddetectionapp.models.SaveToSubcollectionResponseModel
import com.example.tcgcarddetectionapp.models.SaveUsernameRequestModel
import com.example.tcgcarddetectionapp.models.UpdateUserSubcollectionRequestModel
import com.example.tcgcarddetectionapp.models.UserCollectionRequestModel
import com.example.tcgcarddetectionapp.models.UserSubcollectionInfoRequestModel
import com.example.tcgcarddetectionapp.models.UserSubcollectionInfoResponseModel
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("authenticateUser")
    fun loginUser(@Body loginRequest: LoginRequestModel): Call<LoginResponseModel>

    @POST("authenticateGoogleUser")
    fun loginGoogleUser(@Body loginRequest: LoginGoogleRequestModel): Call<LoginResponseModel>

    @POST("authenticateFacebookUser")
    fun loginFacebookUser(@Body loginRequest: LoginFBRequestModel): Call<LoginResponseModel>

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

    @POST("updateUserSubcollection")
    fun updateUserSubcollection(@Body updateUserSubcollectionRequestModel: UpdateUserSubcollectionRequestModel): Call<GenericSuccessErrorResponseModel>

    @POST("deleteUserSubcollection")
    fun deleteUserSubcollection(@Body deleteUserSubcollectionRequestModel: DeleteUserSubcollectionRequestModel): Call<GenericSuccessErrorResponseModel>
}