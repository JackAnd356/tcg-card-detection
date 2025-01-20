package com.example.tcgcarddetectionapp

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import android.os.Bundle
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.common.SignInButton
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun LoginScreen(onLoginNavigate: () -> Unit,
                onNewUserNavigate: () -> Unit,
                username: String,
                userid: String,
                credentialManager: CredentialManager,
                onUsernameChange: (String) -> Unit,
                onUserIdChange: (String) -> Unit,
                onUserEmailChange: (String) -> Unit,
                onUserCollectionChange: (Array<CardData>) -> Unit,
                onUserSubColInfoChange: (Array<SubcollectionInfo>) -> Unit,
                onLockdownEmailChange: (Boolean) -> Unit,
                modifier: Modifier = Modifier) {

    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    val WEB_CLIENT_ID = "595031517908-mo8jed9cc8pdshfdhhb7plmoj1vqf5o5.apps.googleusercontent.com"
    val context = LocalContext.current
    val callbackManager = CallbackManager.Factory.create()
    val loginManager = LoginManager.getInstance()

    val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
        .build()

    loginManager.registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
        override fun onCancel() {
            //Do Nothing
        }

        override fun onError(error: FacebookException) {
            error.printStackTrace()
        }

        override fun onSuccess(result: LoginResult) {
            val accessToken = result.accessToken
            val graphReq = GraphRequest.newMeRequest(
                accessToken,
                object: GraphRequest.GraphJSONObjectCallback {
                    override fun onCompleted(obj: JSONObject?, response: GraphResponse?) {
                        if (obj != null) {
                            val fbid = obj.get("id") as String
                            val fbEmail = obj.get("email") as String
                            loginFacebookPost(fbid,
                                fbEmail,
                                onLoginNavigate,
                                onUserIdChange,
                                onUserEmailChange,
                                onUserCollectionChange,
                                onUserSubColInfoChange,
                                onLockdownEmailChange,)
                        }
                    }
                }
            )
            val params = Bundle()
            params.putString("fields", "id,name,email")
            graphReq.parameters = params
            graphReq.executeAsync()
        }

    })


    Column(verticalArrangement = Arrangement.Top,
        modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally)) {
        Text(
            text = stringResource(R.string.login_label),
            fontSize = 100.sp,
            lineHeight = 116.sp,
            textAlign = TextAlign.Center
        )
        Logo(modifier = modifier)
        UsernameField(username = username, modifier = modifier, onChange = {onUsernameChange(it)})
        Text(
            text = loginError,
            fontSize = 20.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Left
        )
        PasswordField(password = password, modifier = modifier, onChange = {password = it})
        LoginButton{
            loginPost(username,
                password,
                setErrorMessage = {loginError = it},
                onLoginNavigate,
                onUserIdChange,
                onUserEmailChange,
                onUserCollectionChange,
                onUserSubColInfoChange)
        }
        GoogleSignInButtonStyled(context = context) {
            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            runBlocking {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = context,
                    )
                    handleGoogleSignIn(result) {
                        googleid, email ->
                        loginGooglePost(googleid,
                            email,
                            onLoginNavigate,
                            onUserIdChange,
                            onUserEmailChange,
                            onUserCollectionChange,
                            onUserSubColInfoChange,
                            onLockdownEmailChange,)
                    }
                } catch (e: GetCredentialException) {
                    e.printStackTrace()
                }
            }
        }
        Button(
            onClick = {
                loginManager.logIn(context as ActivityResultRegistryOwner, callbackManager, listOf("email"))
            }
        ) {
            Text(stringResource( R.string.facebook_login_placeholder))
        }
        Button(
            onClick = {
                onNewUserNavigate()
            }
        ) {
            Text(
                stringResource(R.string.new_user_button_label)
            )
        }
    }
}

@Composable
fun Logo(modifier: Modifier = Modifier) {
    val image = painterResource(R.drawable.templogo)
    Image(
        painter = image,
        contentDescription = stringResource(R.string.logo_content_description)
    )
}

@Composable
fun UsernameField(username: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    TextField(
        value = username,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.username_label)) }
    )
}

@Composable
fun PasswordField(password: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    TextField(
        value = password,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.password_label)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Composable
fun LoginButton(onClick: () -> Unit) {
    Button(onClick = { onClick()}) {
        Text(stringResource(R.string.login_label))
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TCGCardDetectionAppTheme {
        var username by remember { mutableStateOf("") }
        LoginScreen(
            username = username,
            userid = "1",
            onUsernameChange = { username = it },
            onLoginNavigate = {},
            onUserIdChange = { },
            onUserEmailChange = { },
            onUserCollectionChange = { },
            onUserSubColInfoChange = { },
            onNewUserNavigate = { },
            credentialManager = TODO(),
            modifier = TODO(),
            onLockdownEmailChange = TODO(),
        )
    }
}

@Composable
fun GoogleSignInButtonStyled(
    context: Context,
    onClick: () -> Unit
) {
    AndroidView(
        factory = {
            SignInButton(context).apply {
                setSize(SignInButton.SIZE_WIDE)
                setColorScheme(SignInButton.COLOR_LIGHT)
                setOnClickListener { onClick() }
            }
        },
    )
}

fun loginPost(username: String,
              password: String,
              setErrorMessage: (String) -> Unit,
              onLoginNavigate: () -> Unit,
              onUserIdChange: (String) -> Unit,
              onUserEmailChange: (String) -> Unit,
              onUserCollectionChange: (Array<CardData>) -> Unit,
              onUserSubColInfoChange: (Array<SubcollectionInfo>) -> Unit): Array<Any> {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = LoginRequestModel(username = username, authenticationToken = password)
    var loginSuccess = false
    var message = "Unexpected Error"
    retrofitAPI.loginUser(requestData).enqueue(object: Callback<LoginResponseModel>{
        override fun onResponse(
            call: Call<LoginResponseModel>,
            response: Response<LoginResponseModel>
        ) {
            val respData = response.body()
            if (respData?.success == 0) {
                message = respData.error!!
                setErrorMessage(message)
            }
            else {
                loginSuccess = true
                message = "Successful Login"
                setErrorMessage(message)
                if (respData != null) {
                    onUserIdChange(respData.userid!!)
                    onUserEmailChange(respData.email!!)
                    collectionPost(respData.userid!!, onUserCollectionChange, onUserSubColInfoChange, onLoginNavigate)
                }

            }
        }

        override fun onFailure(call: Call<LoginResponseModel>, t: Throwable) {
            t.printStackTrace()
            message = "Response returned error: " + t.message
            setErrorMessage(message)
        }
    })
    return arrayOf(loginSuccess, message)
}

fun loginGooglePost(googleid: String,
                    email: String,
                    onLoginNavigate: () -> Unit,
                    onUserIdChange: (String) -> Unit,
                    onUserEmailChange: (String) -> Unit,
                    onUserCollectionChange: (Array<CardData>) -> Unit,
                    onUserSubColInfoChange: (Array<SubcollectionInfo>) -> Unit,
                    onLockdownEmailChange: (Boolean) -> Unit,): Array<Any> {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = LoginGoogleRequestModel(googleid = googleid, email = email)
    var loginSuccess = false
    retrofitAPI.loginGoogleUser(requestData).enqueue(object: Callback<LoginResponseModel>{
        override fun onResponse(
            call: Call<LoginResponseModel>,
            response: Response<LoginResponseModel>
        ) {
            val respData = response.body()
            if (respData?.success == 1) {
                loginSuccess = true
                onUserIdChange(respData.userid!!)
                onUserEmailChange(respData.email!!)
                onLockdownEmailChange(true)
                collectionPost(respData.userid!!, onUserCollectionChange, onUserSubColInfoChange, onLoginNavigate)
            }
        }

        override fun onFailure(call: Call<LoginResponseModel>, t: Throwable) {
            t.printStackTrace()
        }
    })
    return arrayOf(loginSuccess)
}

fun loginFacebookPost(fbid: String,
                      email: String,
                      onLoginNavigate: () -> Unit,
                      onUserIdChange: (String) -> Unit,
                      onUserEmailChange: (String) -> Unit,
                      onUserCollectionChange: (Array<CardData>) -> Unit,
                      onUserSubColInfoChange: (Array<SubcollectionInfo>) -> Unit,
                      onLockdownEmailChange: (Boolean) -> Unit): Array<Any> {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = LoginFBRequestModel(fbid = fbid, email = email)
    var loginSuccess = false
    retrofitAPI.loginFacebookUser(requestData).enqueue(object: Callback<LoginResponseModel>{
        override fun onResponse(
            call: Call<LoginResponseModel>,
            response: Response<LoginResponseModel>
        ) {
            val respData = response.body()
            if (respData?.success == 1) {
                loginSuccess = true
                onUserIdChange(respData.userid!!)
                onUserEmailChange(respData.email!!)
                onLockdownEmailChange(true)
                collectionPost(respData.userid!!, onUserCollectionChange, onUserSubColInfoChange, onLoginNavigate)
            }
        }

        override fun onFailure(call: Call<LoginResponseModel>, t: Throwable) {
            t.printStackTrace()
        }
    })
    return arrayOf(loginSuccess)
}


fun collectionPost(userid: String,
                   onUserCollectionChange: (Array<CardData>) -> Unit,
                   onUserSubColInfoChange: (Array<SubcollectionInfo>) -> Unit,
                   onLoginNavigate: () -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = UserCollectionRequestModel(userid = userid)
    retrofitAPI.getUserCollection(requestData).enqueue(object: Callback<Array<CardData>>{
        override fun onResponse(
            call: Call<Array<CardData>>,
            response: Response<Array<CardData>>
        ) {
            val respData = response.body()
            if (respData != null) {
                onUserCollectionChange(respData)
                respData.forEach {
                    cardData ->
                    cardImagePost(cardData.cardid, cardData.game, {cardData.image = it})
                }
                subcollectionPost(userid, onUserSubColInfoChange, onLoginNavigate)
            }
        }

        override fun onFailure(call: Call<Array<CardData>>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

fun subcollectionPost(userid: String,
                      onUserSubColInfoChange: (Array<SubcollectionInfo>) -> Unit,
                      onLoginNavigate: () -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = UserSubcollectionInfoRequestModel(userid = userid)
    retrofitAPI.getUserSubcollectionInfo(requestData).enqueue(object: Callback<UserSubcollectionInfoResponseModel> {
        override fun onResponse(
            call: Call<UserSubcollectionInfoResponseModel>,
            response: Response<UserSubcollectionInfoResponseModel>
        ) {
            val respData = response.body()
            if (respData != null) {
                onUserSubColInfoChange(respData.subcollections)
                onLoginNavigate()
            }
        }

        override fun onFailure(call: Call<UserSubcollectionInfoResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

fun cardImagePost(cardid: String, game: String, setCardImage: (String) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = GetCardImageRequestModel(cardid = cardid, game = game)
    retrofitAPI.getCardImage(requestData).enqueue(object: Callback<GetCardImageResponseModel> {
        override fun onResponse(
            call: Call<GetCardImageResponseModel>,
            response: Response<GetCardImageResponseModel>
        ) {
            val respData = response.body()
            if (respData != null && respData.image != null) {
                setCardImage(respData.image)
            }
        }

        override fun onFailure(call: Call<GetCardImageResponseModel>, t: Throwable) {
            t.printStackTrace()
            Log.d("ERROR", "Card " + cardid + " not loaded in")
            setCardImage("nocardimage")
        }

    } )
}

fun handleGoogleSignIn(result: GetCredentialResponse,
                       loginGooglePost: (String, String) -> Unit) {
    // Handle the successfully returned credential.
    val credential = result.credential

    when (credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    // Use googleIdTokenCredential and extract id to validate and
                    // authenticate on your server.
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data)
                    val googleid = googleIdTokenCredential.idToken
                    val email = googleIdTokenCredential.id
                    loginGooglePost(googleid, email)
                } catch (e: GoogleIdTokenParsingException) {
                    e.printStackTrace()
                }
            }
            else {
                // Catch any unrecognized credential type here.
                Log.e("CREDENTIAL TYPE ERROR", "Unexpected type of credential")
            }
        }

        else -> {
            // Catch any unrecognized credential type here.
            Log.e("CREDENTIAL TYPE ERROR", "Unexpected type of credential")
        }
    }
}