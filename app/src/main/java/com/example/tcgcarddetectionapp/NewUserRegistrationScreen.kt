package com.example.tcgcarddetectionapp

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun NewUserRegistrationScreen(username: String,
                              email: String,
                              storefront: Int,
                              onUsernameChange: (String) -> Unit,
                              onUserEmailChange: (String) -> Unit,
                              onUserStorefrontChange: (Int) -> Unit,
                              onUseridChange: (String) -> Unit,
                              onLoginNavigate: () -> Unit,
                              modifier: Modifier = Modifier) {
    var enteredPassword by remember { mutableStateOf("")}
    var errorText by remember { mutableStateOf("")}

    Column(verticalArrangement = Arrangement.Top,
        modifier = modifier) {
        Text(
            text = stringResource(R.string.new_user_registration_screen_heading),
            fontSize = 50.sp,
            lineHeight = 100.sp,
            textAlign = TextAlign.Center
        )
        NewUserDataComponent(label = stringResource(R.string.username_label),
            data = username,
            modifier = modifier,
            onChange = onUsernameChange)
        NewUserDataComponent(
            label = stringResource(R.string.password_label),
            data = enteredPassword,
            modifier = modifier,
            onChange = {enteredPassword = it},
        )
        NewUserDropdownSelector(
            label = stringResource(R.string.storefront_label),
            data = storefront,
            options = listOf("TCGPlayer", "Card Market"),
            onChange = onUserStorefrontChange,
        )
        NewUserDataComponent(
            label = stringResource(R.string.email_label),
            data = email,
            modifier = modifier,
            onChange = onUserEmailChange,
        )
        Button(
            onClick = {
                SaveNewUserPost(
                    username = username,
                    password = enteredPassword,
                    storefront = storefront,
                    email = email,
                    onUseridChange = onUseridChange,
                    setErrorText = {errorText = it}
                )
            },
            enabled = username != "" && enteredPassword != ""
        ) {
            Text(stringResource(R.string.register_button_label))
        }
        if (errorText != "") {
            Text(errorText,
                color = Color.Red)
        }
    }
}

@Composable
fun NewUserDataComponent(label: String,
                         data: String,
                         modifier: Modifier = Modifier,
                         onChange: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(corner = CornerSize(0.dp)),
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .requiredHeight(70.dp)) {
        Row {
            TextField(
                value = data,
                onValueChange = onChange,
                label = { Text(label) }
            )
        }
    }
}

@Composable
fun NewUserDropdownSelector(label: String, data: Int, options: List<String>, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    var mExpanded by remember { mutableStateOf(false) }
    var mSelectedText by remember { mutableStateOf(options[data - 1]) }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(corner = CornerSize(0.dp)),
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .requiredHeight(70.dp)) {
        Row {
            Text(
                text = label,
                fontSize = 20.sp,
                lineHeight = 50.sp,
                textAlign = TextAlign.Left
            )
            Spacer(Modifier.weight(1f))
            Column {
                OutlinedTextField(
                    value = mSelectedText,
                    onValueChange = { mSelectedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            // This value is used to assign to
                            // the DropDown the same width
                            mTextFieldSize = coordinates.size.toSize()
                        },
                    trailingIcon = {
                        Icon(icon,"contentDescription",
                            Modifier.clickable { mExpanded = !mExpanded })
                    }
                )
                DropdownMenu(
                    expanded = mExpanded,
                    onDismissRequest = {mExpanded = false}
                ) {
                    options.forEachIndexed{ index, optionLabel ->
                        DropdownMenuItem(
                            onClick = {
                                mSelectedText = optionLabel
                                onChange(index + 1)
                                mExpanded = false
                            },
                            text = {Text(optionLabel)}
                        )
                    }
                }
            }
        }
    }
}

fun SaveNewUserPost(
    username: String,
    password: String,
    storefront: Int,
    email: String,
    onUseridChange: (String) -> Unit,
    setErrorText: (String) -> Unit,
) {
    var url = "http://10.0.2.2:5000/"
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = SaveNewUserRequestModel(username = username, authenticationToken = password, storefront = storefront, email = email)
    retrofitAPI.saveNewUser(requestData).enqueue(object: Callback<SaveNewUserResponseModel> {
        override fun onResponse(
            call: Call<SaveNewUserResponseModel>,
            response: Response<SaveNewUserResponseModel>
        ) {
            val respData = response.body()
            if (respData != null) {
                if (respData.success == 0) {
                    Log.d("ERROR", respData.error!!)
                    setErrorText(respData.error)
                }
                else {
                    onUseridChange(respData.userid!!)
                }
            }
        }

        override fun onFailure(call: Call<SaveNewUserResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}





@Preview(showBackground = true)
@Composable
fun NewUserRegistrationScreenPreview(modifier: Modifier = Modifier) {
    TCGCardDetectionAppTheme {
        NewUserRegistrationScreen(
            modifier = modifier,
            onUsernameChange = { },
            onUserEmailChange = { },
            onUserStorefrontChange = { },
            onUseridChange = { },
            username = "",
            email = "",
            storefront = 1,
            onLoginNavigate = {  },
        )
    }
}