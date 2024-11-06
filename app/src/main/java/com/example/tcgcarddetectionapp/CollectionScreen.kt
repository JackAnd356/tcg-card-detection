package com.example.tcgcarddetectionapp

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import kotlin.math.round

@Composable
fun CollectionScreen(gameName: String,
                     subcollections: Array<SubcollectionInfo>,
                     gameFilter: String,
                     totalCardCount: Int,
                     totalCardValue: Double,
                     navController: NavController,
                     modifier: Modifier = Modifier,
                     userid: String) {
    var searchTerm by remember { mutableStateOf("") }
    var popUp by remember { mutableStateOf(false)}
    val scrollstate = rememberScrollState()
    Box(
        modifier
            .background(color = Color.LightGray)
            .fillMaxWidth().fillMaxHeight(.9f)) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally).verticalScroll(state = scrollstate)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = modifier
                    .fillMaxWidth(.8f)
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
            Card(colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = modifier
                    .fillMaxWidth(.8f)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 20.dp),
                onClick = {
                    navController.navigate(CardDetectionScreens.Subcollection.name + "/all/" + gameFilter)
                }
            ) {
                Text(
                    text = String.format(
                        stringResource(R.string.all_cards_label),
                        totalCardCount
                    ),
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
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center
                )
            }
            TextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                label = { Text(stringResource(R.string.search_label)) }
            )
            Button(onClick = {
                popUp = true
            },
                colors = ButtonColors(
                    containerColor = colorResource(R.color.lightGreen),
                    contentColor = Color.Black,
                    disabledContainerColor = colorResource(R.color.lightGreen),
                    disabledContentColor = Color.Black),
            ) {
                Text(stringResource(R.string.create_new_collection_label))
            }

            if (popUp) {
                DialogTest(onDismissRequest = {
                    popUp = false
                },
                onSubmitRequest = {
                  popUp = false
                },
                userid = userid,
                gameName = gameFilter)
            }
            
            subcollections.forEach { subcollection ->
                if (subcollection.game == gameFilter && searchTerm in subcollection.name) {
                    CollectionSummary(
                        name = subcollection.name,
                        cardCount = subcollection.cardCount ?: 0,
                        location = subcollection.physLoc,
                        totalValue = subcollection.totalValue ?: 0.0,
                        subColId = subcollection.subcollectionid,
                        game = gameFilter,
                        navController = navController,
                        modifier = modifier
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionSummary(name: String,
                      cardCount: Int,
                      location: String,
                      totalValue: Double,
                      subColId: String,
                      game: String,
                      navController: NavController,
                      modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .fillMaxWidth(.8f)
            .padding(vertical = 20.dp),
        onClick = {
            navController.navigate(CardDetectionScreens.Subcollection.name + "/" + subColId + "/" + game)
        }
    ) {
        Text(
            text = name,
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
        Row {
            Text(
                text = String.format(stringResource(R.string.location_label), location),
                fontSize = 20.sp,
                lineHeight = 25.sp,
                textAlign = TextAlign.Left
            )
            Text(
                text = String.format(stringResource(R.string.total_value_label), totalValue, "$"),
                fontSize = 20.sp,
                lineHeight = 25.sp,
                textAlign = TextAlign.Left
            )
        }
    }
}

@Composable
fun DialogTest(
    onDismissRequest: () -> Unit,
    onSubmitRequest: () -> Unit,
    userid: String,
    gameName: String
) {
    var subColName by remember { mutableStateOf("") }
    var subColLocation by remember { mutableStateOf("")}
    var isDeck by remember { mutableStateOf(false)}
    var context = LocalContext.current
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
                    label = {Text("Sub-Collection Name:")}
                )

                TextField(
                    value = subColLocation,
                    onValueChange = {subColLocation = it},
                    label = {Text("Sub-Collection Physical Location:")}
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = isDeck,
                        onCheckedChange = { isDeck = it }
                    )
                    Text(
                        "Is this collection a Deck?"
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
                                createNewSubcollectionPost(gameName = gameName, userid = userid, isDeck = isDeck, subcolName = subColName, physLoc = subColLocation)
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

fun createNewSubcollectionPost(gameName: String, userid: String, isDeck: Boolean, subcolName: String, physLoc: String) {
    val url = "http://10.0.2.2:5000/"
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
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
                }
            }
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
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
            userid = "1"
        )
    }
}