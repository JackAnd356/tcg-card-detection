package com.example.tcgcarddetectionapp

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
fun SubcollectionScreen(subcolInfo: SubcollectionInfo,
                        storefront: Int,
                        cardData: Array<CardData>,
                        navBack: () -> Unit,
                        allCardsFlag: Boolean,
                        fullCardPool: Array<CardData>,
                        game: String,
                        userid: String,
                        subcollections: Array<SubcollectionInfo>,
                        modifier: Modifier = Modifier,
                        onCollectionChange: (Array<CardData>) -> Unit) {
    var searchTerm by remember { mutableStateOf("") }
    var showCardPopup by remember { mutableStateOf(false) }
    var currentFocusedCard by remember { mutableStateOf<CardData>(
        value = CardData(
            userid = "",
            cardid = "",
            setcode = "",
            quantity = 0,
            rarity = "",
            subcollections = arrayOf(),
            game = "",
            price = 0.0,
            image = "",
            cardname = ""
        ),
    ) }
    val scrollstate = rememberScrollState()
    val refreshFlag by remember { mutableStateOf(false) }

    Box(
        modifier
            .background(color = Color.LightGray)
            .fillMaxWidth()
            .fillMaxHeight(.9f)) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = modifier
                .wrapContentWidth(Alignment.CenterHorizontally)
                .verticalScroll(state = scrollstate)
        ) {
            Button(
                onClick = { navBack() }
            ) {
                Text(stringResource(R.string.back_to_main_collections_button_label))
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = modifier
                    .fillMaxWidth(.8f)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 20.dp)
            ) {
                Row {
                    Column {
                        Text(
                            text = subcolInfo.name,
                            fontSize = 40.sp,
                            lineHeight = 50.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = String.format(
                                stringResource(R.string.total_value_label),
                                subcolInfo.totalValue,
                                "$"
                            ),
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Row {
                    if (subcolInfo.physLoc != "") {
                        Text(
                            text = String.format(
                                stringResource(R.string.location_label),
                                subcolInfo.physLoc
                            ),
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.weight(1f))
                    }
                    Text(
                        text = String.format(
                            stringResource(R.string.total_cards_label),
                            subcolInfo.cardCount
                        ),
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }

            }
            TextField(
                value = searchTerm,
                onValueChange = { searchTerm = it },
                label = { Text(stringResource(R.string.search_label)) }
            )
            Button(
                onClick = {/* Add Filtering popup here*/}
            ) {
                Text(stringResource(R.string.filter_button_label))
            }
            if (showCardPopup) {
                Dialog(
                    onDismissRequest = { showCardPopup = !showCardPopup}
                ) {
                    CardPopup(
                        cardData = currentFocusedCard,
                        storefront = storefront,
                        subcollections = subcollections,
                        game = game,
                        userid = userid,
                        allCardsFlag = allCardsFlag,
                        collection = cardData,
                        onCollectionChange = onCollectionChange,
                        subcolInfo = subcolInfo
                    )
                }
            }
            var filteredCardData = mutableListOf<CardData>()
            cardData.forEach { card ->
                if (searchTerm in card.cardname) {
                    filteredCardData.add(card)
                }
            }
            if (searchTerm == "") {
                filteredCardData = cardData.toMutableList()
            }
            for (i in 0..(filteredCardData.size - 1) step 2) {
                Row{
                    for (j in i..(i + 1).coerceAtMost(filteredCardData.size - 1)) {
                        val cardInfo = filteredCardData[j]
                        CardImage(
                            cardData = cardInfo,
                            setFocusedCard = { currentFocusedCard = it },
                            showCardPopup = { showCardPopup = !showCardPopup },
                            modifier = modifier
                                .padding(vertical = 20.dp, horizontal = 15.dp)
                        )
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun CardImage(cardData: CardData,
              setFocusedCard: (CardData) -> Unit,
              showCardPopup: () -> Unit,
              modifier: Modifier) {
    val decodedString = Base64.decode(cardData.image!!, 0)
    val img = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    Image(
        bitmap = img.asImageBitmap(),
        contentDescription = "Card",
        modifier
            .size(120.dp, 200.dp)
            .clickable {
                setFocusedCard(cardData)
                showCardPopup()
            }
    )
}


@OptIn(ExperimentalEncodingApi::class)
@Composable
fun CardPopup(cardData: CardData,
              storefront: Int,
              subcollections: Array<SubcollectionInfo>,
              game: String,
              userid: String,
              allCardsFlag: Boolean,
              modifier: Modifier = Modifier,
              collection: Array<CardData>,
              onCollectionChange: (Array<CardData>) -> Unit,
              subcolInfo: SubcollectionInfo) {
    val optionInfo = subcollections.filter( predicate = {
        it.game == game
    })
    val optionList = subcollections.map { it.name }
    var selectedOption by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableIntStateOf(1) }
    var responseText by remember { mutableStateOf("")}
    val context = LocalContext.current
    val staticResponseText = stringResource(R.string.card_added_to_subcollection_message)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        if (cardData.game == "yugioh") {
            Row {
                Column {
                    val decodedString = Base64.decode(cardData.image!!, 0)
                    val img = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    Image(
                        bitmap = img.asImageBitmap(),
                        contentDescription = "Card",
                        modifier
                            .size(120.dp, 200.dp)
                    )
                    Text(cardData.cardname)
                    Text(
                        text = String.format(stringResource(R.string.card_id_label), cardData.cardid),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                    Text(
                        text = String.format(stringResource(R.string.card_setcode_label), cardData.setcode),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                }
                Column {
                    Text(stringResource(R.string.price_header))
                    Text(
                        String.format(
                            stringResource(R.string.card_price_label),
                            cardData.price,
                            "$"
                        )
                    )
                    Button(
                        onClick = {}
                    ) {
                        if (storefront == 1) {
                            Text(stringResource(R.string.tcgplayer_label))
                        } else if (storefront == 2) {
                            Text(stringResource(R.string.card_market_label))
                        }
                    }
                    if (cardData.level != null) {
                        Row {
                            Text(String.format(stringResource(R.string.level_label), cardData.level))
                            Text(String.format(stringResource(R.string.attribute_label), cardData.attribute))
                        }
                    }
                    Text(String.format(stringResource(R.string.type_label), cardData.type))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Gray)
                    ) {
                        Text(cardData.description!!.replace("\\n", "\n"))
                    }
                    if (cardData.atk != null) {
                        Row {
                            Text(String.format(stringResource(R.string.atk_label), cardData.atk))
                            Text(String.format(stringResource(R.string.def_label), cardData.def))
                        }
                    }
                }
            }
        }
        else if (cardData.game == "mtg") {
            Row {
                Column {
                    val decodedString = Base64.decode(cardData.image!!, 0)
                    val img = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    Image(
                        bitmap = img.asImageBitmap(),
                        contentDescription = "Card",
                        modifier
                            .size(120.dp, 200.dp)
                    )
                    Text(cardData.cardname)
                    Text(
                        text = String.format(stringResource(R.string.card_setcode_label), cardData.setcode),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                }
                Column {
                    Text(stringResource(R.string.price_header))
                    Text(
                        String.format(
                            stringResource(R.string.card_price_label),
                            cardData.price,
                            "$"
                        )
                    )
                    Button(
                        onClick = {}
                    ) {
                        if (storefront == 1) {
                            Text(stringResource(R.string.tcgplayer_label))
                        } else if (storefront == 2) {
                            Text(stringResource(R.string.card_market_label))
                        }
                    }
                    Row {
                        Text(String.format(stringResource(R.string.cost_label), cardData.cost))
                        Text(String.format(stringResource(R.string.color_label), cardData.attribute))
                    }
                    Text(String.format(stringResource(R.string.type_label), cardData.type))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Gray)
                    ) {
                        Text(cardData.description!!.replace("\\n", "\n"))
                    }
                    if(cardData.atk != null) {
                        Row {
                            Text(String.format(stringResource(R.string.power_label), cardData.atk))
                            Text(String.format(stringResource(R.string.toughness_label), cardData.def))
                        }
                    }
                }
            }
        }
        else { //Pokemon
            Row {
                Column {
                    val decodedString = Base64.decode(cardData.image!!, 0)
                    val img = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    Image(
                        bitmap = img.asImageBitmap(),
                        contentDescription = "Card",
                        modifier
                            .size(120.dp, 200.dp)
                    )
                    Text(cardData.cardname)
                    Text(
                        text = String.format(stringResource(R.string.card_id_label), cardData.cardid),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                    Text(
                        text = String.format(stringResource(R.string.card_setcode_label), cardData.setcode),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                    Text(
                        text = String.format(stringResource(R.string.hp), cardData.hp),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                    cardData.weaknesses!!.forEach {
                        weakness ->
                        Text(
                            text = String.format(stringResource(R.string.weakness_label), weakness.type, weakness.value),
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                    }
                    Text(
                        text = String.format(stringResource(R.string.retreat), arrToPrintableString(cardData.retreat!!)),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )

                }
                Column {
                    Text(stringResource(R.string.price_header))
                    Text(
                        String.format(
                            stringResource(R.string.card_price_label),
                            cardData.price,
                            "$"
                        )
                    )
                    Button(
                        onClick = {}
                    ) {
                        if (storefront == 1) {
                            Text(stringResource(R.string.tcgplayer_label))
                        } else if (storefront == 2) {
                            Text(stringResource(R.string.card_market_label))
                        }
                    }

                    Text(String.format(stringResource(R.string.attribute_label), cardData.attribute))

                    Text(String.format(stringResource(R.string.type_label), cardData.type))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Gray)
                    ) {
                        cardData.attacks!!.forEach {
                            attack ->
                            Text(String.format(stringResource(R.string.cost_label), arrToPrintableString(attack.cost)))
                            Text(String.format(stringResource(R.string.card_name_label), attack.name))
                            Text(String.format(stringResource(R.string.damage_label), attack.damage))
                            if (attack.text != null) {
                                Text(String.format(stringResource(R.string.effect_label), attack.text))
                            }
                        }
                    }
                }
            }
        }
        if(allCardsFlag) {
            UserDropdownSelector(
                label = stringResource(R.string.subcollection_page),
                data = 1,
                onUserStorefrontChange = {
                    selectedOption = optionInfo.get(it - 1).subcollectionid
                    selectedIndex = it - 1
                },
                options = optionList,
            )
            Button(
                onClick = {
                    saveToSubcollectionPost(
                        card = cardData,
                        userid = userid,
                        subcollection = selectedOption,
                        subcolInfo = optionInfo.get(selectedIndex)
                    )
                    responseText = staticResponseText
                },
                enabled = selectedOption != ""
            ) {
                Text(stringResource(R.string.add_to_subcollection_button_label))
            }

            Column {
                Button(onClick = {
                    val successful = removeFromCollectionPost(card = cardData, userid = userid, game = game, quantity = 1, collection = collection, onCollectionChange = onCollectionChange)
                    if (successful) Toast.makeText(context, "Card Successfully Removed", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.align(Alignment.End)) {
                    Text(text="Delete")
                }
            }
        } else {
            Column {
                Button(onClick = {
                    removeFromSubcollectionPost(card = cardData, userid = userid, game = game, subcollection = subcolInfo.name)
                }) {
                    Text(text = "Remove")
                }
            }
        }
    }
}

fun arrToPrintableString(arr: Array<String>): String {
    var str = ""
    arr.forEach {
            itm ->
        str += itm + ","
    }
    return str
}

fun removeFromCollectionPost(card: CardData, userid: String, game: String, quantity: Int, collection: Array<CardData>, onCollectionChange: (Array<CardData>) -> Unit): Boolean {
    val url = "http://10.0.2.2:5000/"
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = AddRemoveCardModel(userid = userid, game = game, cardid = card.cardid, setcode = card.setcode, cardname = card.cardname, price = card.price, quantity = quantity, rarity = card.rarity)
    var successful = false
    retrofitAPI.removeFromCollection(requestData).enqueue(object : Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            if (card.quantity <= 1) {
                val cardsWithoutRemoved = ArrayList<CardData>()
                for (cardData in collection) {
                    if (card != cardData) cardsWithoutRemoved.add(cardData)
                }
                onCollectionChange(cardsWithoutRemoved.toTypedArray())
            } else {
                card.quantity--
            }
            successful = true
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }
    })
    return successful
}

fun removeFromSubcollectionPost(card: CardData, userid: String, subcollection: String, game: String): Boolean {
    val url = "http://10.0.2.2:5000/"
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = AddRemoveCardModel(userid = userid, game = game, cardid = card.cardid, setcode = card.setcode, cardname = card.cardname, price = card.price, quantity = card.quantity, rarity = card.rarity, subcollection = subcollection)
    var successful = false

    retrofitAPI.removeFromSubcollection(requestData).enqueue(object : Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            card.subcollections
            successful = true
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }
    })
    return successful
}

fun saveToSubcollectionPost(card: CardData, userid: String, subcollection: String, subcolInfo: SubcollectionInfo) {
    val url = "http://10.0.2.2:5000/"
    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = SaveToSubcollectionRequestModel(
        userid = userid,
        cardid = card.cardid,
        setcode = card.setcode,
        game = card.game,
        rarity = card.rarity,
        subcollection = subcollection,
    )
    retrofitAPI.addToUserSubcollection(requestData).enqueue(object : Callback<SaveToSubcollectionResponseModel> {
        override fun onResponse(
            call: Call<SaveToSubcollectionResponseModel>,
            response: Response<SaveToSubcollectionResponseModel>
        ) {
            if (card.subcollections == null) {
                card.subcollections = arrayOf(subcollection)
            }
            else {
                card.subcollections = card.subcollections!!.plus(subcollection)
            }
            if (subcolInfo.cardCount == null) {
                subcolInfo.cardCount = card.quantity
            }
            else {
                subcolInfo.cardCount = subcolInfo.cardCount!! + card.quantity
            }
            if (subcolInfo.totalValue == null) {
                subcolInfo.totalValue = card.quantity * card.price
            }
            else {
                subcolInfo.totalValue = subcolInfo.totalValue!! + (card.quantity * card.price)
            }
        }

        override fun onFailure(call: Call<SaveToSubcollectionResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}


@Preview(showBackground = true)
@Composable
fun SubollectionScreenPreview() {
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
    val card1 = CardData(
        userid = "1",
        cardid = "123",
        setcode = "LOB-EN005",
        quantity = 1,
        rarity = "",
        subcollections = arrayOf("1"),
        game = "yugioh",
        price = 10.91,
        image = "",
        cardname = "Dark Magician"
    )

    val card2 = CardData(
        userid = "1",
        cardid = "321",
        setcode = "LOB-EN001",
        quantity = 2,
        rarity = "",
        subcollections = arrayOf("1"),
        game = "yugioh",
        price = 15.10,
        image = "",
        cardname = "Blue-Eyes White Dragon"
    )

    val card3 = CardData(
        userid = "1",
        cardid = "321",
        setcode = "LOB-EN003",
        quantity = 2,
        rarity = "",
        subcollections = arrayOf("1"),
        game = "yugioh",
        price = 14.10,
        image = "",
        "Test Card"
    )

    val cards = arrayOf(card1, card2, card3)
    TCGCardDetectionAppTheme {
        SubcollectionScreen(
            subcolInfo = subCol1,
            storefront = 1,
            cardData = cards,
            navBack = { },
            allCardsFlag = false,
            fullCardPool = cards,
            subcollections = arrayOf(subCol1),
            userid = "1",
            game = "yugioh",
            onCollectionChange = {},
        )
    }
}

