package com.example.tcgcarddetectionapp

import androidx.compose.ui.window.Dialog
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.tcgcarddetectionapp.models.GenericSuccessErrorResponseModel
import com.example.tcgcarddetectionapp.models.SaveEmailRequestModel
import com.example.tcgcarddetectionapp.models.SavePasswordRequestModel
import com.example.tcgcarddetectionapp.models.SaveUsernameRequestModel
import com.example.tcgcarddetectionapp.models.UserCollectionRequestModel
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun ProfileScreen(username: String,
                  email: String,
                  userid: String,
                  onUsernameChange: (String) -> Unit,
                  onUserEmailChange: (String) -> Unit,
                  onLogout: () -> Unit,
                  lockdownEmail: Boolean,
                  modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var usernameEditFlag by remember { mutableStateOf(false) }
    var passwordEditFlag by remember { mutableStateOf(false) }
    var emailEditFlag by remember { mutableStateOf(false) }
    var oldUsername by remember { mutableStateOf(username) }
    var oldEmail by remember { mutableStateOf(email) }
    var showDeletePopup by remember { mutableStateOf(false) }
    var enteredPassword by remember { mutableStateOf(context.getString(R.string.password_placeholder))}

    Column(verticalArrangement = Arrangement.Top,
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.profile_screen_heading),
            style = appTypography.displayLarge,
        )
        if(showDeletePopup) {
            DeleteUserWarningPopup(
                userid = userid,
                onDismiss = { showDeletePopup = !showDeletePopup},
                navLogin = onLogout
            )
        }
        UserDataComponent(label = stringResource(R.string.username_label),
            data = username,
            modifier = Modifier,
            onChange = onUsernameChange,
            flag = usernameEditFlag,
            onClickEdit = {usernameEditFlag = !usernameEditFlag},
            onClickSave = {
                SaveUsernamePost(userid = userid, username = username)
                oldUsername = username
                usernameEditFlag = !usernameEditFlag
            },
            oldData = oldUsername)
        UserDataComponent(
            label = stringResource(R.string.password_label),
            data = enteredPassword,
            modifier = Modifier,
            flag = passwordEditFlag,
            onChange = {enteredPassword = it},
            onClickEdit = {
                passwordEditFlag = !passwordEditFlag
            },
            onClickSave = {
                SavePasswordPost(userid = userid, password = enteredPassword)
                enteredPassword = context.getString(R.string.password_placeholder)
                passwordEditFlag = !passwordEditFlag
            },
            oldData = context.getString(R.string.password_placeholder)
        )
        UserDataComponent(
            label = stringResource(R.string.email_label),
            data = email,
            modifier = Modifier,
            flag = emailEditFlag,
            onChange = onUserEmailChange,
            onClickEdit = { emailEditFlag = !emailEditFlag },
            onClickSave = {
                SaveEmailPost(userid = userid, email = email)
                oldEmail = email
                emailEditFlag = !emailEditFlag
            },
            oldData = oldEmail,
            mutable = !lockdownEmail,
        )
        Button(
            onClick = { showDeletePopup = !showDeletePopup},
            colors = ButtonColors(
                containerColor = Color(0xFFFF7A7A),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFFF7A7A),
                disabledContentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(.9f).padding(top = 30.dp),
        ) {
            Text(
                text = stringResource(R.string.delete_account_button_label),
                style = appTypography.labelLarge
            )
        }
        Button(
            onClick = { onLogout() },
            colors = ButtonColors(
                containerColor = Color(0xFF77E6FF),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF77E6FF),
                disabledContentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(.9f).padding(top = 30.dp),
        ) {
            Text(
                text = stringResource(R.string.logout_button_label),
                style = appTypography.labelLarge
            )
        }

    }
}

@Composable
fun UserDataComponent(label: String,
                      data: String,
                      modifier: Modifier = Modifier,
                      flag: Boolean,
                      onChange: (String) -> Unit,
                      onClickEdit: () -> Unit,
                      onClickSave: () -> Unit,
                      oldData: String? = null,
                      mutable: Boolean? = true) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(corner = CornerSize(0.dp)),
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .requiredHeight(70.dp)) {
        Row(modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            if (flag) {
                TextField(modifier = Modifier.padding(start = 5.dp),
                    value = data,
                    onValueChange = onChange,
                    label = { Text(
                        text = label,
                        style = appTypography.labelLarge
                    ) }
                )
            }
            else {
                Text(modifier = Modifier.padding(start = 5.dp),
                    text = "$label: $data",
                    style = appTypography.labelLarge,
                    textAlign = TextAlign.Left
                )
            }

            //Spacer(modifier = Modifier.weight(1f))
            if (mutable == true) {
                Button(
                    onClick = {
                        if (flag && data != oldData) {
                            onClickSave()
                        } else {
                            onClickEdit()
                        }
                    }, modifier = Modifier
                        .height(40.dp)
                        .align(Alignment.CenterVertically)
                        .padding(end = 5.dp),
                    colors = ButtonColors(
                        containerColor = colorResource(R.color.gray),
                        contentColor = Color.Black,
                        disabledContainerColor = colorResource(R.color.gray),
                        disabledContentColor = Color.Black
                    )
                ) {
                    if (flag && data != oldData) {
                        Text(
                            text = stringResource(R.string.save_button_label),
                            style = appTypography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    } else if (flag) {
                        Text(
                            text = stringResource(R.string.cancel_button_label),
                            style = appTypography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.change_button_label),
                            style = appTypography.labelLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserDropdownSelector(label: String, data: Int, onUserStorefrontChange: (Int) -> Unit, options: List<String>, modifier: Modifier = Modifier) {
    var mExpanded by remember { mutableStateOf(false) }
    var mSelectedText by remember { mutableStateOf(options[data - 1]) }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(corner = CornerSize(0.dp)),
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .requiredHeight(70.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = appTypography.labelLarge,
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
                        }
                        .clickable(interactionSource = interactionSource, indication = null) {
                            mExpanded = !mExpanded
                        },
                    trailingIcon = {
                        Icon(icon,context.getString(R.string.password_placeholder),
                            Modifier.clickable { mExpanded = !mExpanded })
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White)
                )
                DropdownMenu(
                    expanded = mExpanded,
                    onDismissRequest = {mExpanded = false}
                ) {
                    options.forEachIndexed{ index, optionLabel ->
                        DropdownMenuItem(
                            onClick = {
                                mSelectedText = optionLabel
                                onUserStorefrontChange(index + 1)
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

@Composable
fun DeleteUserWarningPopup(
    userid: String,
    onDismiss: () -> Unit,
    navLogin: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(modifier = Modifier) {
            Text(modifier = Modifier.padding(5.dp),
                text = stringResource(R.string.delete_account_confirmation_message),
                style = appTypography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)) {
                Button(
                    modifier = Modifier.padding(end = 5.dp),
                    onClick = {
                        deleteUserPost(userid = userid, navLogin = navLogin)
                    },
                    colors = ButtonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Red,
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.yes_label),
                        style = appTypography.labelLarge
                    )
                }
                Button(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    onClick = { onDismiss() },
                    colors = ButtonColors(
                        containerColor = colorResource(R.color.textLightGrey),
                        contentColor = Color.White,
                        disabledContainerColor = colorResource(R.color.textLightGrey),
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.no_label),
                        style = appTypography.labelLarge
                    )
                }
            }
        }
    }
}

fun SaveUsernamePost(userid: String, username: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = SaveUsernameRequestModel(userid = userid, username = username)
    retrofitAPI.saveUsername(requestData).enqueue(object: Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            val respData = response.body()
            if (respData != null) {
                if (respData.success == 0) {
                    Log.d("ERROR", respData.error!!)
                }
            }
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

fun SavePasswordPost(userid: String, password: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = SavePasswordRequestModel(userid = userid, authenticationToken = password)
    retrofitAPI.saveUserPass(requestData).enqueue(object: Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            val respData = response.body()
            if (respData != null) {
                if (respData.success == 0) {
                    Log.d("ERROR", respData.error!!)
                }
            }
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

fun SaveEmailPost(userid: String, email: String) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = SaveEmailRequestModel(userid = userid, email = email)
    retrofitAPI.saveUserEmail(requestData).enqueue(object: Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            val respData = response.body()
            if (respData != null) {
                if (respData.success == 0) {
                    Log.d("ERROR", respData.error!!)
                }
            }
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

fun deleteUserPost(userid: String, navLogin: () -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = UserCollectionRequestModel(userid = userid)
    retrofitAPI.deleteUser(requestData).enqueue(object: Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            val respData = response.body()
            if (respData != null) {
                if (respData.success == 0) {
                    Log.d("ERROR", respData.error!!)
                }
                else {
                    navLogin()
                }
            }
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    } )
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview(modifier: Modifier = Modifier) {
    TCGCardDetectionAppTheme {
        ProfileScreen(
            username = "TestUser", email = "TestUser@void.com", modifier = modifier,
            onUsernameChange = { },
            onUserEmailChange = { },
            userid = "1",
            onLogout = { },
            lockdownEmail = false
        )
    }
}