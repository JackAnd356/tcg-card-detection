package com.example.tcgcarddetectionapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint.Align
import android.net.Uri
import android.util.Log
import android.view.RoundedCorner
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import com.example.tcgcarddetectionapp.models.AddRemoveCardModel
import com.example.tcgcarddetectionapp.models.CardData
import com.example.tcgcarddetectionapp.models.GenericSuccessErrorResponseModel
import com.example.tcgcarddetectionapp.models.SaveToSubcollectionRequestModel
import com.example.tcgcarddetectionapp.models.SaveToSubcollectionResponseModel
import com.example.tcgcarddetectionapp.models.SubcollectionInfo
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Collections
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
fun SubcollectionScreen(subcolInfo: SubcollectionInfo,
                        navBack: () -> Unit,
                        allCardsFlag: Boolean,
                        fullCardPool: Array<CardData>,
                        game: String,
                        userid: String,
                        subcollections: Array<SubcollectionInfo>,
                        modifier: Modifier = Modifier,
                        onCollectionChange: (Array<CardData>) -> Unit,
                        removeSubcollection: (SubcollectionInfo) -> Unit,) {
    var searchTerm by remember { mutableStateOf("") }
    var showCardPopup by remember { mutableStateOf(false) }
    var showAllCardAddToSubcollection by remember { mutableStateOf(false) }
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
    var refreshFlag by remember { mutableStateOf(0) }
    var cardData = remember { mutableStateListOf<CardData>() }
    var navWebsite by remember { mutableStateOf("") }
    var optionsExpanded by remember { mutableStateOf(false) }
    var showEditPopup by remember { mutableStateOf(false) }
    var showDeletePopup by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var filterList = remember { mutableStateListOf<(CardData) -> Boolean>()}
    var listOfTypes = mutableListOf<String>()
    var selectedTypeFilter by remember { mutableStateOf("") }
    var selectedMinQuantity by remember { mutableStateOf("") }
    var minQuantErr by remember { mutableStateOf(false) }
    var selectedMaxQuantity by remember { mutableStateOf("") }
    var maxQuantErr by remember { mutableStateOf(false) }
    var selectedPriceRange by remember { mutableStateOf(0) }
    var selectedMinLevel by remember { mutableStateOf("") }
    var minLevelErr by remember { mutableStateOf(false) }
    var selectedMaxLevel by remember { mutableStateOf("") }
    var maxLevelErr by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedMinPrice by remember { mutableStateOf("") }
    var minPriceErr by remember { mutableStateOf(false) }
    var selectedMaxPrice by remember { mutableStateOf("") }
    var maxPriceErr by remember { mutableStateOf(false) }

    val leftOffset = LocalConfiguration.current.screenWidthDp * 0.05
    val forceRecomposeState = rememberUpdatedState(refreshFlag)


    if (navWebsite != "") {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(navWebsite))
        LocalContext.current.startActivity(browserIntent)
        navWebsite = ""
    }

    if (subcolInfo.subcollectionid == "all") {
        fullCardPool.forEach {
                card ->
            var filterFlag = true
            filterList.forEach {
                    func ->
                if (!func(card)) {
                    filterFlag = false
                }
            }
            if (!cardData.contains(card) && card.game == game && filterFlag) {
                cardData.add(card)
            }
            else if (cardData.contains(card) && !filterFlag) {
                cardData.removeAll(Collections.singleton(card))
            }
        }
    }
    else {
        if (game == "yugioh") {
            fullCardPool.sortedWith( compareBy<CardData> {
                yugiohSort(it.type!!)
            }.thenBy { it.cardname }).forEach {
                    card ->
                var filterFlag = true
                filterList.forEach {
                        func ->
                    if (!func(card)) {
                        filterFlag = false
                    }
                }
                if (card.subcollections != null &&
                    card.subcollections!!.count { it == subcolInfo.subcollectionid} > cardData.count {it == card} &&
                    card.subcollections?.contains(subcolInfo.subcollectionid) == true && filterFlag) {
                    val numCopiesToAdd = card.subcollections!!.count { it == subcolInfo.subcollectionid} - cardData.count {it == card}
                    for (i in 1..numCopiesToAdd) {
                        cardData.add(card)
                    }
                }
                else if (card.subcollections != null &&
                    card.subcollections!!.count { it == subcolInfo.subcollectionid} < cardData.count {it == card} &&
                    card.subcollections?.contains(subcolInfo.subcollectionid) == true && filterFlag) {
                    val numCopiesToRemove = cardData.count {it == card} - card.subcollections!!.count { it == subcolInfo.subcollectionid}
                    for (i in 1..numCopiesToRemove) {
                        cardData.remove(card)
                    }
                }
                else if (!filterFlag && cardData.contains(card)) {
                    cardData.removeAll(Collections.singleton(card))
                }
                else if ((card.subcollections == null || !card.subcollections!!.contains(subcolInfo.subcollectionid)) && cardData.contains(card)) {
                    cardData.removeAll(Collections.singleton(card))
                }

                if (card.subcollections?.contains(subcolInfo.subcollectionid) == true) {
                    if (card.game == "yugioh") {
                        listOfTypes.add(card.type!!)
                    }
                }
            }
        }
        else {
            fullCardPool.sortedWith( compareBy<CardData> {
                it.attribute
            }.thenBy { it.cardname }).forEach {
                    card ->
                var filterFlag = true
                filterList.forEach {
                        func ->
                    if (!func(card)) {
                        filterFlag = false
                    }
                }
                if (card.subcollections != null &&
                    card.subcollections!!.count { it == subcolInfo.subcollectionid} > cardData.count {it == card} &&
                    card.subcollections?.contains(subcolInfo.subcollectionid) == true && filterFlag) {

                    cardData.add(card)
                }
                else if (!filterFlag && cardData.contains(card)) {
                    cardData.removeAll(Collections.singleton(card))
                }
                else if ((card.subcollections == null || !card.subcollections!!.contains(subcolInfo.subcollectionid)) && cardData.contains(card)) {
                    cardData.removeAll(Collections.singleton(card))
                }

                if (card.subcollections?.contains(subcolInfo.subcollectionid) == true) {
                    if (card.game == "yugioh") {
                        listOfTypes.add(card.type!!)
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .background(color = Color.White)
            .fillMaxWidth()
            .fillMaxHeight(.9f),
        contentAlignment = Alignment.TopStart) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .wrapContentWidth(Alignment.CenterHorizontally)
        ) {
            Spacer(modifier = modifier.height(20.dp))
            Button(modifier = Modifier.padding(start = leftOffset.dp, end = 0.dp, top = 0.dp, bottom = 0.dp),
                onClick = { navBack() }
            ) {
                Text(stringResource(R.string.back_to_main_collections_button_label))
            }
            Card(colors = CardDefaults.cardColors(containerColor = colorResource(R.color.gray)),
                modifier = Modifier
                    .fillMaxWidth(.9f)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp, bottom = 20.dp)
                    .border(1.dp, colorResource(R.color.borderGray), shape = RoundedCornerShape(10.dp))
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(10.dp),
                        spotColor = Color.Black, ambientColor = Color.Black)
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center) {
                    Column(modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally) {
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
                    if (subcolInfo.subcollectionid != "all") {
                        Box {
                            IconButton(
                                onClick = {optionsExpanded = !optionsExpanded}
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More"
                                )
                            }
                            DropdownMenu(
                                expanded = optionsExpanded,
                                onDismissRequest = {
                                    optionsExpanded = false
                                }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.edit_option_label)) },
                                    onClick = {
                                        showEditPopup = !showEditPopup
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete_option_label)) },
                                    onClick = {
                                        showDeletePopup = !showDeletePopup
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)) {
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

            Row(modifier = Modifier.fillMaxWidth(.9f).align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    modifier = Modifier.weight(.75f).padding(end = 5.dp).height(26.dp),
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.search_label)) }
                )
                Button(
                    modifier = Modifier.weight(.25f).height(50.dp),
                    shape = if (!showFilters) RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp)
                            else RoundedCornerShape(10.dp, 10.dp, 0.dp, 0.dp),
                    onClick = {
                        showFilters = !showFilters
                    },
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.darkGray))
                ) {
                    Text(stringResource(R.string.filter_button_label))
                }
            }

            Row(modifier = modifier.fillMaxWidth(.9f).align(Alignment.CenterHorizontally)) {

                if(!allCardsFlag) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            showAllCardAddToSubcollection = !showAllCardAddToSubcollection
                        }
                    ) {
                        Text(stringResource(R.string.add_from_all_cards_button_label))
                    }
                }
            }

            if (showFilters) {
                Column(modifier = Modifier.fillMaxWidth(.9f)
                    .clip(RoundedCornerShape(10.dp, 0.dp, 10.dp, 10.dp))
                    .background(color = colorResource(R.color.darkGray))
                    .align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(modifier = Modifier.padding(vertical = 10.dp)) {
                        MinMaxIntComponent(
                            minVal = selectedMinQuantity,
                            maxVal = selectedMaxQuantity,
                            onMinValChange = { selectedMinQuantity = it },
                            onMaxValChange = { selectedMaxQuantity = it },
                            label = stringResource(R.string.quantity_filter_label),
                            minError = minQuantErr,
                            onMinErrChange = { minQuantErr = it },
                            maxError = maxQuantErr,
                            onMaxErrChange = { maxQuantErr = it },
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(5.dp)
                                .fillMaxWidth(.9f)
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                priceRange = selectedPriceRange,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                            ).forEach {
                                    func ->
                                filterList.add(func)
                            }
                        }
                    }
                    Row(modifier = Modifier.padding(vertical = 10.dp)) {
                        MinMaxDoubleComponent(
                            minVal = selectedMinPrice,
                            maxVal = selectedMaxPrice,
                            onMinValChange = { selectedMinPrice = it },
                            onMaxValChange = {  selectedMaxPrice = it },
                            label = stringResource(R.string.price_filter_label),
                            minError = minPriceErr,
                            onMinErrChange = { minPriceErr = it },
                            maxError = maxPriceErr,
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(5.dp)
                                .fillMaxWidth(.9f),
                            onMaxErrChange = { maxPriceErr = it }
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                priceRange = selectedPriceRange,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                            ).forEach {
                                    func ->
                                filterList.add(func)
                            }
                        }
                    }
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        if (game == "yugioh") {
                            MinMaxIntComponent(
                                minVal = selectedMinLevel,
                                maxVal = selectedMaxLevel,
                                onMinValChange = { selectedMinLevel = it },
                                onMaxValChange = { selectedMaxLevel = it },
                                label = stringResource(R.string.level_filter_label),
                                minError = minLevelErr,
                                onMinErrChange = { minLevelErr = it },
                                maxError = maxLevelErr,
                                onMaxErrChange = { maxLevelErr = it },
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .padding(5.dp)
                                    .fillMaxWidth(.9f)
                            ) {
                                filterList.clear()
                                recalculateFilterList(
                                    type = selectedTypeFilter,
                                    quantityMin = selectedMinQuantity,
                                    levelMin = selectedMinLevel,
                                    priceRange = selectedPriceRange,
                                    quantityMax = selectedMaxQuantity,
                                    levelMax = selectedMaxLevel,
                                    priceMin = selectedMinPrice,
                                    priceMax = selectedMaxPrice,
                                ).forEach {
                                        func ->
                                    filterList.add(func)
                                }
                            }
                            DropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = {
                                    typeExpanded = false
                                }
                            ) {
                                listOfTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            selectedTypeFilter = type
                                            filterList.clear()
                                            recalculateFilterList(
                                                type = selectedTypeFilter,
                                                quantityMin = selectedMinQuantity,
                                                levelMin = selectedMinLevel,
                                                priceRange = selectedPriceRange,
                                                quantityMax = selectedMaxQuantity,
                                                levelMax = selectedMaxLevel,
                                                priceMin = selectedMinPrice,
                                                priceMax = selectedMaxPrice,
                                            ).forEach { func ->
                                                filterList.add { func(it) }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        else if (game == "mtg") {

                        }
                        else if (game == "pokemon") {

                        }
                    }
                }
            }

            if (showAllCardAddToSubcollection) {
                Dialog(
                    onDismissRequest = {showAllCardAddToSubcollection = !showAllCardAddToSubcollection}
                ) {
                    AddFromAllCardsPopup(
                        allCards = fullCardPool,
                        game = game,
                        subcollection = subcolInfo.subcollectionid,
                        userid = userid,
                        subcolInfo = subcolInfo,
                        modifier = modifier,
                        refreshUI = { refreshFlag++ }
                    )
                }
            }
            if (showCardPopup) {
                Dialog(
                    onDismissRequest = { showCardPopup = !showCardPopup}
                ) {
                    CardPopup(
                        cardData = currentFocusedCard,
                        subcollections = subcollections,
                        game = game,
                        userid = userid,
                        allCardsFlag = allCardsFlag,
                        onCollectionChange = onCollectionChange,
                        subcolInfo = subcolInfo,
                        removeCard = {card -> cardData.remove(card)},
                        fullCardPool = fullCardPool,
                        navWebsite = { navWebsite = it },
                        refreshUI = {
                            refreshFlag++
                            Log.d("DEBUG", "Refresh flag flipped")
                                    },
                        showCardPopup = {showCardPopup = !showCardPopup}
                    )
                }
            }
            if (showEditPopup) {
                Dialog(
                    onDismissRequest = { showEditPopup = !showEditPopup }
                ) {
                    EditSubcollectionPopup(
                        subcollection = subcolInfo,
                        onCancel = { showEditPopup = !showEditPopup },
                    )
                }
            }
            if (showDeletePopup) {
                Dialog(
                    onDismissRequest = { showDeletePopup = !showDeletePopup }
                ) {
                    DeleteSubcollectionPopup(
                        subcollection = subcolInfo,
                        onCancel = { showDeletePopup = !showDeletePopup },
                        refresh = {  },
                        onDeleteSubcol = {
                            navBack()
                            removeSubcollection(it)
                        },
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
            filteredCardData = sortSubcollection(filteredCardData, game)
            Spacer(modifier = Modifier.height(5.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredCardData) { cardInfo ->
                    CardImage(
                        cardData = cardInfo,
                        setFocusedCard = { currentFocusedCard = it },
                        showCardPopup = { showCardPopup = !showCardPopup },
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 10.dp)
                    )
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
    if (cardData.image != "nocardimage" && cardData.image != null) {
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
    else {
        Image(
            painter = painterResource(R.drawable.nocardimage),
            contentDescription = stringResource(R.string.card_image_not_loaded_context),
            modifier.clickable {
                setFocusedCard(cardData)
                showCardPopup()
            }
        )
    }
}


@OptIn(ExperimentalEncodingApi::class)
@Composable
fun CardPopup(cardData: CardData,
              subcollections: Array<SubcollectionInfo>,
              game: String,
              userid: String,
              allCardsFlag: Boolean,
              fullCardPool: Array<CardData>,
              modifier: Modifier = Modifier,
              subcolInfo: SubcollectionInfo,
              onCollectionChange: (Array<CardData>) -> Unit,
              removeCard: (CardData) -> Unit,
              navWebsite: (String) -> Unit,
              refreshUI: () -> Unit,
              showCardPopup: () -> Unit) {
    val optionInfo = subcollections.filter( predicate = {
        it.game == game
    })
    val context = LocalContext.current
    val optionList = optionInfo.map { it.name }
    var selectedOption by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableStateOf(1) }
    var responseText by remember { mutableStateOf("")}
    val staticResponseText = stringResource(R.string.card_added_to_subcollection_message)
    var cardQuantity by remember { mutableStateOf(cardData.quantity.toString())}
    var cardQuantErr by remember { mutableStateOf(false) }
    var subColQuant by remember { mutableStateOf(cardData.subcollections?.count{ it == subcolInfo.subcollectionid}.toString()) }
    var showCardDeletePopup by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        when (cardData.game) {
            "yugioh" -> {
                Row {
                    Column {
                        if (cardData.image != "nocardimage" && cardData.image != null) {
                            val decodedString = Base64.decode(cardData.image!!, 0)
                            val img =
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            Image(
                                bitmap = img.asImageBitmap(),
                                contentDescription = "Card",
                                modifier
                                    .size(120.dp, 200.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.nocardimage),
                                contentDescription = stringResource(R.string.card_image_not_loaded_context),
                                modifier
                                    .size(120.dp, 200.dp)
                            )
                        }
                        Text(cardData.cardname)
                        Text(
                            text = String.format(
                                stringResource(R.string.card_id_label),
                                cardData.cardid
                            ),
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                        Text(
                            text = String.format(
                                stringResource(R.string.card_setcode_label),
                                cardData.setcode
                            ),
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
                            onClick = {
                                if (cardData.purchaseurl != null) {
                                    navWebsite(cardData.purchaseurl!!)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.tcgplayer_label))
                        }
                        if (cardData.level != null) {
                            Row {
                                Text(
                                    String.format(
                                        stringResource(R.string.level_label),
                                        cardData.level
                                    )
                                )
                                Text(
                                    String.format(
                                        stringResource(R.string.attribute_label),
                                        cardData.attribute
                                    )
                                )
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
                                Text(
                                    String.format(
                                        stringResource(R.string.atk_label),
                                        cardData.atk
                                    )
                                )
                                Text(
                                    String.format(
                                        stringResource(R.string.def_label),
                                        cardData.def
                                    )
                                )
                            }
                        }
                    }
                }
            }
            "mtg" -> {
                Row {
                    Column {
                        if (cardData.image != "nocardimage" && cardData.image != null) {
                            val decodedString = Base64.decode(cardData.image!!, 0)
                            val img =
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            Image(
                                bitmap = img.asImageBitmap(),
                                contentDescription = "Card",
                                modifier
                                    .size(120.dp, 200.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.nocardimage),
                                contentDescription = stringResource(R.string.card_image_not_loaded_context),
                                modifier
                                    .size(120.dp, 200.dp)
                            )
                        }
                        Text(cardData.cardname)
                        Text(
                            text = String.format(
                                stringResource(R.string.card_setcode_label),
                                cardData.setcode
                            ),
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
                            onClick = {
                                if (cardData.purchaseurl != null) {
                                    navWebsite(cardData.purchaseurl!!)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.tcgplayer_label))
                        }
                        Row {
                            Text(String.format(stringResource(R.string.cost_label), cardData.cost))
                            Text(
                                String.format(
                                    stringResource(R.string.color_label),
                                    cardData.attribute
                                )
                            )
                        }
                        Text(String.format(stringResource(R.string.type_label), cardData.type))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Gray)
                        ) {
                            Text(cardData.description!!.replace("\\n", "\n"))
                        }
                        if (cardData.atk != null) {
                            Row {
                                Text(
                                    String.format(
                                        stringResource(R.string.power_label),
                                        cardData.atk
                                    )
                                )
                                Text(
                                    String.format(
                                        stringResource(R.string.toughness_label),
                                        cardData.def
                                    )
                                )
                            }
                        }
                    }
                }
            }

            else -> { //Pokemon
                Row {
                    Column {
                        if (cardData.image != "nocardimage" && cardData.image != null) {
                            val decodedString = Base64.decode(cardData.image!!, 0)
                            val img =
                                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            Image(
                                bitmap = img.asImageBitmap(),
                                contentDescription = "Card",
                                modifier
                                    .size(120.dp, 200.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(R.drawable.nocardimage),
                                contentDescription = stringResource(R.string.card_image_not_loaded_context),
                                modifier
                                    .size(120.dp, 200.dp)
                            )
                        }
                        Text(cardData.cardname)
                        Text(
                            text = String.format(
                                stringResource(R.string.card_id_label),
                                cardData.cardid
                            ),
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                        Text(
                            text = String.format(
                                stringResource(R.string.card_setcode_label),
                                cardData.setcode
                            ),
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                        Text(
                            text = String.format(stringResource(R.string.hp), cardData.hp),
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                        )
                        cardData.weaknesses!!.forEach { weakness ->
                            Text(
                                text = String.format(
                                    stringResource(R.string.weakness_label),
                                    weakness.type,
                                    weakness.value
                                ),
                                fontSize = 10.sp,
                                lineHeight = 15.sp,
                            )
                        }
                        Text(
                            text = String.format(
                                stringResource(R.string.retreat),
                                arrToPrintableString(cardData.retreat!!)
                            ),
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
                            onClick = {
                                if (cardData.purchaseurl != null) {
                                    navWebsite(cardData.purchaseurl!!)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.tcgplayer_label))
                        }

                        Text(
                            String.format(
                                stringResource(R.string.attribute_label),
                                cardData.attribute
                            )
                        )

                        Text(String.format(stringResource(R.string.type_label), cardData.type))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Gray)
                        ) {
                            cardData.attacks!!.forEach { attack ->
                                Text(
                                    String.format(
                                        stringResource(R.string.cost_label),
                                        arrToPrintableString(attack.cost)
                                    )
                                )
                                Text(
                                    String.format(
                                        stringResource(R.string.card_name_label),
                                        attack.name
                                    )
                                )
                                Text(
                                    String.format(
                                        stringResource(R.string.damage_label),
                                        attack.damage
                                    )
                                )
                                if (attack.text != null) {
                                    Text(
                                        String.format(
                                            stringResource(R.string.effect_label),
                                            attack.text
                                        )
                                    )
                                }
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
                        subcolInfo = optionInfo.get(selectedIndex),
                        refreshUI = { }
                    )
                    responseText = staticResponseText
                },
                enabled = selectedOption != ""
            ) {
                Text(stringResource(R.string.add_to_subcollection_button_label))
            }
            Row {
                Text(stringResource(R.string.quantity_filter_label) + ": ")
                Button(
                    onClick = {
                        if (cardData.quantity > 1) {
                            removeFromCollectionPost(
                                card = cardData,
                                userid = userid,
                                game = cardData.game,
                                quantity = 1,
                                fullCardPool = fullCardPool,
                                onCollectionChange = onCollectionChange,
                                removeCard = removeCard,
                                subcolInfo = subcolInfo,
                                refreshUI = refreshUI,
                            )
                            cardQuantity = (cardQuantity.toInt() - 1).toString()
                        }
                        else {
                            showCardDeletePopup = true
                        }
                    }
                ) { Text("-")}
                TextField(
                    value = cardQuantity,
                    onValueChange = {
                        if (it != "" && it.toInt() > 0) {
                            cardQuantErr = false
                            val quantityChange = (it.toInt() - cardData.quantity)
                            if (quantityChange > 0) {
                                increaseQuantityPost(
                                    userid = userid,
                                    card = cardData,
                                    quantityChange = quantityChange
                                )
                            }
                            else {
                                removeFromCollectionPost(
                                    card = cardData,
                                    userid = userid,
                                    game = cardData.game,
                                    quantity = (quantityChange * -1),
                                    fullCardPool = fullCardPool,
                                    onCollectionChange = onCollectionChange,
                                    removeCard = removeCard,
                                    subcolInfo = subcolInfo,
                                    refreshUI = refreshUI,
                                )
                            }
                        }
                        else {
                            cardQuantErr = true
                        }
                        cardQuantity = it
                                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = modifier.fillMaxWidth(.25f),
                    isError = cardQuantErr,
                    supportingText = {
                        if (cardQuantErr) {
                            Text(
                                text = stringResource(R.string.invalid_value_error),
                                color = Color.Red
                            )
                        }
                    }
                )
                Button(
                    onClick = {
                        increaseQuantityPost(
                            userid = userid,
                            card = cardData,
                            quantityChange = 1
                        )
                        cardQuantity = (cardQuantity.toInt() + 1).toString()
                    }
                ) { Text("+")}
            }
            /*Column {
                Button(onClick = {
                    showCardPopup()
                    val successful = removeFromCollectionPost(card = cardData, userid = userid, game = game, quantity = 1, fullCardPool = fullCardPool, onCollectionChange = onCollectionChange, removeCard = removeCard, subcolInfo = subcolInfo, refreshUI = refreshUI)
                    if (successful) Toast.makeText(context, "Card Successfully Deleted", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(context, "Card Deletion Failed", Toast.LENGTH_SHORT).show()
                }, modifier = Modifier.align(Alignment.End)) {
                    Text(text="Delete")
                }
            }*/
        } else {
            Row {
                Text(stringResource(R.string.subcollection_quantity_label) + ": ")
                Button(
                    onClick = {
                        if (subColQuant.toInt() > 1) {
                            removeFromSubcollectionPost(
                                card = cardData,
                                userid = userid,
                                subcolInfo = subcolInfo,
                                game = cardData.game,
                                refreshUI = {
                                    refreshUI()
                                },
                            )
                            subColQuant = (subColQuant.toInt() - 1).toString()
                        }
                        else {
                            showCardDeletePopup = true
                        }

                    }
                ) { Text("-")}
                Text(subColQuant)
                Button(
                    onClick = {
                        saveToSubcollectionPost(
                            card = cardData,
                            userid = userid,
                            subcollection = subcolInfo.subcollectionid,
                            subcolInfo = subcolInfo,
                            refreshUI = {
                                refreshUI()
                                        },
                        )
                        subColQuant = (subColQuant.toInt() + 1).toString()
                    }
                ) { Text("+")}
            }
            /*Column {
                Button(onClick = {
                    showCardPopup()
                    if (removeFromSubcollectionPost(card = cardData, userid = userid, game = game, subcolInfo = subcolInfo, refreshUI = refreshUI)) {
                        Toast.makeText(context, "Card Successfully Removed", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Card Removal Failed", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = "Remove")
                }
            }*/
        }
    }
    if (showCardDeletePopup) {
        AlertDialog(
            onDismissRequest = { showCardDeletePopup = false },
            title = { Text(stringResource(R.string.remove_card_title)) },
            text = {
                if (allCardsFlag) {
                    Text(stringResource(R.string.remove_card_all_cards_text))
                }
                else {
                    Text(String.format(stringResource(R.string.remove_card_subcollection_text), subcolInfo.name))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCardDeletePopup = false
                    if (allCardsFlag) {
                        removeFromCollectionPost(
                            card = cardData,
                            userid = userid,
                            game = cardData.game,
                            quantity = 1,
                            fullCardPool = fullCardPool,
                            onCollectionChange = onCollectionChange,
                            removeCard = removeCard,
                            subcolInfo = subcolInfo,
                            refreshUI = refreshUI,
                        )
                    }
                    else {
                        removeFromSubcollectionPost(
                            card = cardData,
                            userid = userid,
                            subcolInfo = subcolInfo,
                            game = cardData.game,
                            refreshUI = {
                                refreshUI()
                            },
                        )
                    }
                    showCardPopup()
                }) {
                    Text(stringResource(R.string.yes_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCardDeletePopup = false }) {
                    Text(stringResource(R.string.no_label))
                }
            }
        )
    }
}

@Composable
fun AddFromAllCardsPopup(allCards: Array<CardData>,
                         game: String,
                         subcollection: String,
                         userid: String,
                         subcolInfo: SubcollectionInfo,
                         refreshUI: () -> Unit,
                         modifier: Modifier = Modifier) {
    val scrollstate = rememberScrollState()
    val cardList = allCards.filter {
        it.game == game
    }
    val checkedStates = remember { mutableStateListOf<Boolean>() }
    repeat(cardList.size) {
        checkedStates.add(false)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Column(
             modifier = modifier.verticalScroll(scrollstate)
        ) {
            cardList.forEachIndexed { index, card ->
                Row {
                    Text(card.cardname)
                    Checkbox(
                        checked = checkedStates[index],
                        onCheckedChange = {
                            isChecked ->
                            checkedStates[index] = isChecked
                        }
                    )
                }
            }
        }
        Button(
            onClick = {
                cardList.forEachIndexed { index, card ->
                    if (checkedStates[index]) {
                        saveToSubcollectionPost(card = card, userid = userid, subcollection = subcollection, subcolInfo = subcolInfo, refreshUI = refreshUI)
                    }
                }
            }
        ) {
            Text(stringResource(R.string.add_to_subcollection_button_label))
        }
    }
}

@Composable
fun MinMaxIntComponent(minVal: String,
                       maxVal: String,
                       onMinValChange: (String) -> Unit,
                       onMaxValChange: (String) -> Unit,
                       label: String,
                       minError: Boolean,
                       onMinErrChange: (Boolean) -> Unit,
                       maxError: Boolean,
                       onMaxErrChange: (Boolean) -> Unit,
                       modifier: Modifier,
                       recalculateFilter: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = modifier) {
        Text(modifier = Modifier.weight(.2f),
            text = label)
        FilterTextfield(modifier = Modifier.weight(.3f), label = stringResource(R.string.min), value = minVal,
            onValueChange = {
                if (it.isDigitsOnly()) {
                    onMinValChange(it)
                    if (it == "") {
                        onMinErrChange(false)
                        recalculateFilter()
                    } else if (it.toInt() < 0 || (maxVal != "" && maxVal.toInt() < it.toInt())) {
                        onMinErrChange(true)
                    } else {
                        onMinErrChange(false)
                        recalculateFilter()
                    }
                }
            },
            isError = minError,
        )

        Spacer(modifier = Modifier.weight(.09f))
        Column(modifier = Modifier.weight(.11f).height(30.dp),
            verticalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.weight(.5f))
            Text(modifier = Modifier.weight(.5f),
                text = "-")
        }

        FilterTextfield(modifier = Modifier.weight(.3f), label = stringResource(R.string.max), value = maxVal,
            onValueChange = {
                if (it.isDigitsOnly()) {
                    onMaxValChange(it)
                    if (it == "") {
                        onMaxErrChange(false)
                        recalculateFilter()
                    } else if (it.toInt() < 0 || (minVal != "" && minVal.toInt() > it.toInt())) {
                        onMaxErrChange(true)
                    } else {
                        onMaxErrChange(false)
                        recalculateFilter()
                    }
                }
            },
            isError = maxError,
        )
    }
}
@Composable
fun MinMaxDoubleComponent(minVal: String,
                       maxVal: String,
                       onMinValChange: (String) -> Unit,
                       onMaxValChange: (String) -> Unit,
                       label: String,
                       minError: Boolean,
                       onMinErrChange: (Boolean) -> Unit,
                       maxError: Boolean,
                       onMaxErrChange: (Boolean) -> Unit,
                       modifier: Modifier,
                       recalculateFilter: () -> Unit) {
    Row(modifier = modifier,
        verticalAlignment = Alignment.CenterVertically) {
        Text(modifier = Modifier.weight(.2f),
            text = label)
        FilterTextfield(modifier = Modifier.weight(.3f), label = stringResource(R.string.min), value = minVal,
            onValueChange = {
                onMinValChange(it)
                if (it == "") {
                    onMinErrChange(false)
                    recalculateFilter()
                } else if (it.toDouble() < 0 || (maxVal != "" && maxVal.toDouble() < it.toDouble())) {
                    onMinErrChange(true)
                } else {
                    onMinErrChange(false)
                    recalculateFilter()
                }
            },
            isError = minError
        )

        Spacer(modifier = Modifier.weight(.09f))
        Column(modifier = Modifier.weight(.11f).height(30.dp),
            verticalArrangement = Arrangement.Center) {
            Spacer(modifier = Modifier.weight(.5f))
            Text(modifier = Modifier.weight(.5f),
                text = "-")
        }

        FilterTextfield(modifier = Modifier.weight(.3f), label = stringResource(R.string.max), value = maxVal,
            onValueChange = {
                onMaxValChange(it)
                if (it == "") {
                    onMaxErrChange(false)
                    recalculateFilter()
                } else if (it.toDouble() < 0 || (minVal != "" && minVal.toDouble() > it.toDouble())) {
                    onMaxErrChange(true)
                } else {
                    onMaxErrChange(false)
                    recalculateFilter()
                }
            },
            isError = maxError
        )
    }
}

fun arrToPrintableString(arr: Array<String>): String {
    var str = ""
    arr.forEach {
            itm ->
        str += "$itm,"
    }
    return str
}

fun removeFromCollectionPost(card: CardData, userid: String, game: String, quantity: Int, fullCardPool: Array<CardData>,
                             onCollectionChange: (Array<CardData>) -> Unit, removeCard: (CardData) -> Unit,
                             subcolInfo: SubcollectionInfo, refreshUI: () -> Unit): Boolean {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
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
            if (card.quantity <= quantity) {
                val cardsWithoutRemoved = ArrayList<CardData>()
                for (cardData in fullCardPool) {
                    if (card != cardData) {
                        cardsWithoutRemoved.add(cardData)
                    }
                }
                removeCard(card)
                onCollectionChange(cardsWithoutRemoved.toTypedArray())
            } else {
                card.quantity = card.quantity - quantity
            }

            updateSubcollectionInfo(subcolInfo = subcolInfo, card = card, quantity = 1, adding = false)
            successful = true
            refreshUI()
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }
    })
    return successful
}

fun increaseQuantityPost(userid: String, card: CardData, quantityChange: Int) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)

    card.quantity += quantityChange

    val requestData = AddRemoveCardModel(userid = userid, game = card.game, cardid = card.cardid, setcode = card.setcode,
        cardname = card.cardname, price = card.price, quantity = quantityChange, level = card.level, attribute = card.attribute,
        type = card.type, atk = card.atk, def = card.def, description = card.description, cost = card.cost, attacks = card.attacks,
        weaknesses = card.weaknesses, hp = card.hp, retreat = card.retreat, rarity = card.rarity)

    retrofitAPI.addToCollection(requestData).enqueue(object:
        Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            val respData = response.body()
            if (respData != null) {
                //Do Nothing
            }
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

fun removeFromSubcollectionPost(card: CardData, userid: String, subcolInfo: SubcollectionInfo, game: String, refreshUI: () -> Unit): Boolean {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = AddRemoveCardModel(userid = userid, game = game, cardid = card.cardid, setcode = card.setcode, cardname = card.cardname, price = card.price, quantity = card.quantity, rarity = card.rarity, subcollection = subcolInfo.subcollectionid)
    var successful = false

    retrofitAPI.removeFromSubcollection(requestData).enqueue(object : Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            if (card.subcollections != null) {
                card.subcollections = card.subcollections!!.toList().remove(subcolInfo.subcollectionid).toTypedArray()
            }
            successful = true
            updateSubcollectionInfo(subcolInfo = subcolInfo, card = card, quantity = 1, adding = false)
            refreshUI()
        }

        override fun onFailure(call: Call<GenericSuccessErrorResponseModel>, t: Throwable) {
            t.printStackTrace()
        }
    })
    return successful
}

fun saveToSubcollectionPost(card: CardData, userid: String, subcollection: String, subcolInfo: SubcollectionInfo, refreshUI: () -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = SaveToSubcollectionRequestModel(
        userid = userid,
        cardid = card.cardid,
        setcode = card.setcode,
        game = card.game,
        rarity = card.rarity!!,
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
            updateSubcollectionInfo(subcolInfo = subcolInfo, card = card, quantity = 1, adding = true)
            refreshUI()
        }

        override fun onFailure(call: Call<SaveToSubcollectionResponseModel>, t: Throwable) {
            t.printStackTrace()
        }

    })
}

fun recalculateFilterList(type: String, quantityMin: String, levelMin: String, priceRange: Int, quantityMax: String, levelMax: String, priceMin: String, priceMax: String): MutableList<(CardData) -> Boolean> {
    var ret = mutableListOf<(CardData) -> Boolean>()
    if (type != "") {
        ret.add({it.type == type})
    }
    if (quantityMin != "" && (quantityMax == "" || quantityMin.toInt() < quantityMax.toInt())) {
        ret.add({it.quantity >= quantityMin.toInt()})
    }
    if (quantityMax != "" && (quantityMin == "" || quantityMax.toInt() > quantityMin.toInt())) {
        ret.add({it.quantity <= quantityMax.toInt()})
    }
    if (levelMin != "" && (levelMax == "" || levelMin.toInt() < levelMax.toInt())) {
        ret.add({ (it.level?.toInt() ?: -1) >= levelMin.toInt()})
    }
    if (levelMax != "" && (levelMin == "" || levelMin.toInt() < levelMax.toInt())) {
        ret.add({ (it.level?.toInt() ?: Int.MAX_VALUE) <= levelMax.toInt()})
    }
    if (priceMin != "" && (priceMax == "" || priceMin.toDouble() < priceMax.toDouble())) {
        ret.add({ (it.price) >= priceMin.toDouble()})
    }
    if (priceMax != "" && (priceMin == "" || priceMin.toDouble() < priceMax.toDouble())) {
        ret.add({ (it.price) <= priceMax.toDouble()})
    }
    return ret
}

fun sortSubcollection(subCol: MutableList<CardData> , game: String): MutableList<CardData> {
    if (game == "yugioh") {
        return subCol.sortedWith( compareBy<CardData> {
            yugiohSort(it.type!!)
        }.thenBy { it.cardname }).toMutableList()
    }
    return subCol.sortedWith( compareBy<CardData> {
        it.attribute
    }.thenBy { it.cardname }).toMutableList()
}

fun yugiohSort(type: String): Int {
    if (type.contains(" Spell")) {
        return 2
    }
    else if (type.contains(" Trap")) {
        return 3
    }
    else {
        return 1
    }
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
        cardname = "Test Card"
    )

    val cards = arrayOf(card1, card2, card3)
    TCGCardDetectionAppTheme {
        SubcollectionScreen(
            subcolInfo = subCol1,
            navBack = { },
            allCardsFlag = false,
            fullCardPool = cards,
            subcollections = arrayOf(subCol1),
            userid = "1",
            game = "yugioh",
            onCollectionChange = {},
            removeSubcollection = {},
        )
    }
}

