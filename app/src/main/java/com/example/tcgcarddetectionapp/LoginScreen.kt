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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

@Composable
fun LoginScreen(onLoginNavigate: () -> Unit, modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.Top,
        modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally)) {
        Text(
            text = "Login",
            fontSize = 100.sp,
            lineHeight = 116.sp,
            textAlign = TextAlign.Center
        )
        Logo(modifier = modifier)
        UsernameField(username = username, modifier = modifier, onChange = {username = it})
        Text(
            text = loginError,
            fontSize = 20.sp,
            lineHeight = 30.sp,
            textAlign = TextAlign.Left
        )
        PasswordField(password = password, modifier = modifier, onChange = {password = it})
        LoginButton{
            loginPost(username, password, setErrorMessage = {loginError = it}, onLoginNavigate)
        }
    }
}

@Composable
fun Logo(modifier: Modifier = Modifier) {
    val image = painterResource(R.drawable.templogo)
    Image(
        painter = image,
        contentDescription = "Logo"
    )
}

@Composable
fun UsernameField(username: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    TextField(
        value = username,
        onValueChange = onChange,
        label = { Text("Username") }
    )
}

@Composable
fun PasswordField(password: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    TextField(
        value = password,
        onValueChange = onChange,
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

@Composable
fun LoginButton(onClick: () -> Unit) {
    Button(onClick = { onClick()}) {
        Text("Login")
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TCGCardDetectionAppTheme {
        LoginScreen({})
    }
}

fun loginPost(username: String, password: String, setErrorMessage: (String) -> Unit, onLoginNavigate: () -> Unit): Array<Any> {
    var url = "http://10.0.2.2:5000/"
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
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
                onLoginNavigate()
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