package com.example.tcgcarddetectionapp

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tcgcarddetectionapp.models.CreateSubcollectionModel
import com.example.tcgcarddetectionapp.models.DeleteUserSubcollectionRequestModel
import com.example.tcgcarddetectionapp.models.GenericSuccessErrorResponseModel
import com.example.tcgcarddetectionapp.models.SubcollectionInfo
import com.example.tcgcarddetectionapp.models.UpdateUserSubcollectionRequestModel
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun CollectionScreen(gameName: String,
                     subcollections: Array<SubcollectionInfo>,
                     gameFilter: String,
                     totalCardCount: Int,
                     totalCardValue: Double,
                     navController: NavController,
                     modifier: Modifier = Modifier,
                     userid: String,
                     removeSubcollection: (SubcollectionInfo) -> Unit,
                     onUserSubColInfoChange: (Array<SubcollectionInfo>) -> Unit) {
    var searchTerm by remember { mutableStateOf("") }
    var popUp by remember { mutableStateOf(false)}
    val scrollstate = rememberScrollState()
    var selectedSubcollectionInfo by remember { mutableStateOf<SubcollectionInfo>(
        value = SubcollectionInfo(
            subcollectionid = "none",
            name = "none",
            physLoc = "none",
            game = gameFilter,
            isDeck = false,
            userid = userid,
            totalValue = 0.0,
            cardCount = 0,
        ),
    ) }
    val context = LocalContext.current
    var showEditPopup by remember { mutableStateOf(false) }
    var showDeletePopup by remember { mutableStateOf(false) }
    var refreshFlag by remember { mutableStateOf(false) }
    val leftOffset = LocalConfiguration.current.screenWidthDp * 0.05
    val cardWidth = 0.9f

    Box(
        modifier
            .background(color = Color.White)
            .fillMaxWidth()
            .fillMaxHeight(.9f)) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .wrapContentWidth(Alignment.CenterHorizontally)
                .verticalScroll(state = scrollstate)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(ContextCompat.getColor(context, R.color.gray))),
                modifier = Modifier
                    .fillMaxWidth(cardWidth)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 20.dp)
            ) {
                Text(
                    text = String.format(
                        stringResource(R.string.collection_screen_heading),
                        gameName
                    ),
                    fontSize = 50.sp,
                    lineHeight = 60.sp,
                    textAlign = TextAlign.Center
                )
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(ContextCompat.getColor(context, R.color.gray))),
                modifier = Modifier
                    .fillMaxWidth(cardWidth)
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 20.dp),
                onClick = {
                    navController.navigate(CardDetectionScreens.Subcollection.name + "/all/" + gameFilter)
                }
            ) {
                Column(modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(
                            stringResource(R.string.all_cards_label),
                            totalCardCount
                        ),
                        modifier = Modifier.fillMaxWidth(1f),
                        fontSize = 20.sp,
                        lineHeight = 25.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = String.format(
                            stringResource(R.string.total_value_label),
                            totalCardValue,
                            "$"
                        ),
                        modifier = Modifier.fillMaxWidth(1f),
                        fontSize = 20.sp,
                        lineHeight = 25.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = String.format(
                            stringResource(R.string.view_all_collection),
                            totalCardValue,
                            "$"
                        ),
                        modifier = Modifier.fillMaxWidth(1f),
                        fontSize = 20.sp,
                        lineHeight = 25.sp,
                        textAlign = TextAlign.Center
                    )
                }

            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth(cardWidth)
                    .align(Alignment.CenterHorizontally),
                value = searchTerm,
                onValueChange = { searchTerm = it },
                label = { Text(stringResource(R.string.search_label)) }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = leftOffset.dp, top = 5.dp)
            ) {
                Button(
                    onClick = {
                        popUp = true
                    },
                    shape = RoundedCornerShape(10),
                    colors = ButtonColors(
                        containerColor = colorResource(R.color.lightGreen),
                        contentColor = Color.Black,
                        disabledContainerColor = colorResource(R.color.lightGreen),
                        disabledContentColor = Color.Black
                    )
                ) {
                    Text(stringResource(R.string.create_new_collection_label))
                }
            }

            if (popUp) {
                DialogTest(onDismissRequest = {
                    popUp = false
                },
                onSubmitRequest = {
                  popUp = false
                },
                userid = userid,
                gameName = gameFilter,
                onUserSubColInfoChange = onUserSubColInfoChange)
            }

            if (showEditPopup) {
                Dialog(
                    onDismissRequest = {showEditPopup = !showEditPopup}
                ) {
                    EditSubcollectionPopup(
                        subcollection = selectedSubcollectionInfo,
                        onCancel = { showEditPopup = !showEditPopup },
                    )
                }
            }

            if (showDeletePopup) {
                Dialog(
                    onDismissRequest = {showDeletePopup = !showDeletePopup}
                ) {
                    DeleteSubcollectionPopup(
                        subcollection = selectedSubcollectionInfo,
                        onCancel = { showDeletePopup = !showDeletePopup },
                        refresh = { refreshFlag = !refreshFlag },
                        onDeleteSubcol = removeSubcollection,
                    )
                }
            }
            
            subcollections.forEach { subcollection ->
                if (subcollection.game == gameFilter && searchTerm in subcollection.name) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = leftOffset.dp, end = leftOffset.dp)
                    ) {
                        CollectionSummary(
                            subcollection = subcollection,
                            game = gameFilter,
                            navController = navController,
                            onEditSubcollectionInfo = {
                                selectedSubcollectionInfo = it
                                showEditPopup = !showEditPopup
                            },
                            onDeleteSubcollection = {
                                selectedSubcollectionInfo = it
                                showDeletePopup = !showDeletePopup
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionSummary(subcollection: SubcollectionInfo,
                      game: String,
                      navController: NavController,
                      onEditSubcollectionInfo: (SubcollectionInfo) -> Unit,
                      onDeleteSubcollection: (SubcollectionInfo) -> Unit,
                      modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val name = subcollection.name
    val cardCount = subcollection.cardCount ?: 0
    val location = subcollection.physLoc
    val totalValue = subcollection.totalValue ?: 0.0
    val subColId = subcollection.subcollectionid
    Card(colors = CardDefaults.cardColors(containerColor = colorResource(R.color.gray)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        onClick = {
            navController.navigate(CardDetectionScreens.Subcollection.name + "/" + subColId + "/" + game)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f) // Allow the column to take up remaining space
            ) {
                Text(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 30.sp,
                    lineHeight = 35.sp,
                    textAlign = TextAlign.Left
                )
                Text(
                    text = String.format(stringResource(R.string.cards_label), cardCount),
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Left
                )
                Text(
                    text = String.format(stringResource(R.string.location_label), location),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Left
                )
                Text(
                    text = String.format(
                        stringResource(R.string.total_value_label),
                        totalValue,
                        "$"
                    ),
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Left
                )
            }

            Box(modifier = Modifier,
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = {
                        expanded = !expanded
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit_option_label)) },
                        onClick = {
                            onEditSubcollectionInfo(subcollection)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete_option_label)) },
                        onClick = {
                            onDeleteSubcollection(subcollection)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DialogTest(
    onDismissRequest: () -> Unit,
    onSubmitRequest: () -> Unit,
    userid: String,
    gameName: String,
    onUserSubColInfoChange: (Array<SubcollectionInfo>) -> Unit
) {
    var subColName by remember { mutableStateOf("") }
    var subColLocation by remember { mutableStateOf("")}
    var isDeck by remember { mutableStateOf(false)}
    val context = LocalContext.current
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Create A New Subcollection",
                    modifier = Modifier.padding(16.dp),
                )

                TextField(
                    value = subColName,
                    onValueChange = {subColName = it},
                    label = {Text(stringResource(R.string.subcol_name_label))}
                )

                TextField(
                    value = subColLocation,
                    onValueChange = {subColLocation = it},
                    label = {Text(stringResource(R.string.subcol_physloc_label))}
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isDeck,
                        onCheckedChange = { isDeck = it }
                    )
                    Text(
                        stringResource(R.string.subcol_isdeck_label)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                            if (subColName != "") {
                                createNewSubcollectionPost(gameName = gameName, userid = userid, isDeck = isDeck, subcolName = subColName, physLoc = subColLocation, onUserSubColInfoChange = onUserSubColInfoChange)
                                onSubmitRequest()
                            } else {
                                Toast.makeText(context, "Fill out all required fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun EditSubcollectionPopup(
    subcollection: SubcollectionInfo,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(subcollection.name)}
    var physloc by remember { mutableStateOf(subcollection.physLoc) }
    var isDeck by remember { mutableStateOf(subcollection.isDeck) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.subcol_name_label))}
        )
        TextField(
            value = physloc,
            onValueChange = { physloc = it },
            label = { Text(stringResource(R.string.subcol_physloc_label))}
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isDeck,
                onCheckedChange = { isDeck = it }
            )
            Text(
                stringResource(R.string.subcol_isdeck_label)
            )
        }
        Row {
            Button(
                onClick = {
                    subcollection.name = name
                    subcollection.physLoc = physloc
                    subcollection.isDeck = isDeck
                    updateSubcollectionPost(subcollection)
                    onCancel()
                }
            ) {
                Text(stringResource(R.string.save_button_label))
            }
            Button(
                onClick = {
                    onCancel()
                }
            ) {
                Text(stringResource(R.string.cancel_button_label))
            }
        }
    }
}

@Composable
fun DeleteSubcollectionPopup(
    subcollection: SubcollectionInfo,
    onCancel: () -> Unit,
    refresh: () -> Unit,
    onDeleteSubcol: (SubcollectionInfo) -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Text(stringResource(R.string.subcol_delete_warning))
        Row {
            Button(
                onClick = {
                    deleteSubcollectionPost(subcollection)
                    onDeleteSubcol(subcollection)
                    refresh()
                    onCancel()
                }
            ) {
                Text(stringResource(R.string.delete_option_label))
            }
            Button(
                onClick = {
                    onCancel()
                }
            ) {
                Text(stringResource(R.string.cancel_button_label))
            }
        }
    }
}

fun createNewSubcollectionPost(gameName: String, userid: String, isDeck: Boolean, subcolName: String, physLoc: String, onUserSubColInfoChange: (Array<SubcollectionInfo>) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = CreateSubcollectionModel(userid = userid, name = subcolName, isDeck = isDeck, game = gameName, physLoc = physLoc)

    retrofitAPI.createUserSubcollection(requestData).enqueue(object:
        Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            val respData = response.body()
            if (respData != null) {
                if (respData.success == 0) {
                    Log.d("ERROR", respData.error!!)
                } else {
                    subcollectionPost(userid = userid, onUserSubColInfoChange, {})
                }
            }
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

fun updateSubcollectionPost(subcollection: SubcollectionInfo) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = UpdateUserSubcollectionRequestModel(
        name = subcollection.name,
        physLoc = subcollection.physLoc,
        isDeck = subcollection.isDeck,
        subcollectionid = subcollection.subcollectionid
    )
    retrofitAPI.updateUserSubcollection(requestData).enqueue(object: Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            //Do Nothing
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

fun deleteSubcollectionPost(subcollection: SubcollectionInfo) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = DeleteUserSubcollectionRequestModel(subcollectionid = subcollection.subcollectionid)
    retrofitAPI.deleteUserSubcollection(requestData).enqueue(object: Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            //Do nothing
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

@Composable
fun FilterTextfield(modifier: Modifier, label: String, value: String, onValueChange: (String) -> Unit) {
    Column() {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            color = Color.Black
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionScreenPreview() {
    val subCol1 = SubcollectionInfo(
        subcollectionid = "1",
        name = "Subcollection 1",
        totalValue = 500.12,
        physLoc = "Mega Tin 2013",
        cardCount = 42,
        game = "yugioh",
        isDeck = false,
        userid = "1"
    )
    val subCol2 = SubcollectionInfo(
        subcollectionid = "1",
        name = "Subcollection 2",
        totalValue = 12.13,
        physLoc = "Mega Tin 2012",
        cardCount = 4,
        game = "pokemon",
        isDeck = false,
        userid = "1"
    )
    val subCol3 = SubcollectionInfo(
        subcollectionid = "1",
        name = "Subcollection 3",
        totalValue = 56.0,
        physLoc = "Mega Tin 2014",
        cardCount = 7,
        game = "yugioh",
        isDeck = false,
        userid = "1"
    )
    val colList = arrayOf(subCol1, subCol2, subCol3)
    TCGCardDetectionAppTheme {
        CollectionScreen(
            gameName = "Yu-Gi-Oh!",
            subcollections = colList,
            gameFilter = "yugioh",
            navController = rememberNavController(),
            totalCardCount = 200,
            totalCardValue = 1000000.00,
            userid = "1",
            onUserSubColInfoChange = {},
            removeSubcollection = {  }
        )
    }
}