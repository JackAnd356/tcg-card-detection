package com.example.tcgcarddetectionapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint.Align
import android.net.Uri
import android.util.Log
import android.view.RoundedCorner
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
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

    val screenHeight = LocalConfiguration.current.screenHeightDp
    val forceRecomposeState = rememberUpdatedState(refreshFlag)
    val backArrowSize = (screenHeight * 0.02)


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
        contentAlignment = Alignment.TopCenter) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .wrapContentWidth(Alignment.CenterHorizontally)
        ) {
            Box(modifier = Modifier
                .clickable(
                    onClick = navBack,
                    onClickLabel = "Back To Collection Manager"
                )
                .width((backArrowSize * 2).dp)
                .padding(top = 18.dp)) {
                Icon(modifier = Modifier
                    .height(backArrowSize.dp)
                    .width(backArrowSize.dp),
                    painter = painterResource(R.drawable.arrow_left_icon),
                    contentDescription = "Back Arrow",
                    tint = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 10.dp, bottom = 20.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(10.dp),
                        ambientColor = Color.Black,
                        spotColor = Color.Black
                    )
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorResource(R.color.gray)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            colorResource(R.color.borderGray),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                                    onClick = { optionsExpanded = !optionsExpanded }
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
            }

            if(!allCardsFlag) {
                Row(modifier = Modifier.fillMaxWidth(.9f).align(Alignment.CenterHorizontally)) {
                    Button(
                        shape = RoundedCornerShape(10),
                        modifier = Modifier,
                        onClick = {
                            showAllCardAddToSubcollection = !showAllCardAddToSubcollection
                        },
                        colors = ButtonDefaults.buttonColors(colorResource(R.color.lightGreen))
                    ) {
                        Text(
                            text = stringResource(R.string.add_from_all_cards_button_label),
                            color = Color.Black)
                    }
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

            Box(modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.TopCenter) {
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
                    modifier = Modifier.fillMaxWidth(.9f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(filteredCardData) { cardInfo ->
                        CardImage(
                            cardData = cardInfo,
                            setFocusedCard = { currentFocusedCard = it },
                            showCardPopup = { showCardPopup = !showCardPopup },
                            modifier = Modifier,
                            allCardsFlag = allCardsFlag,
                            game = game
                        )
                    }
                }
                if (showFilters) {
                    Column(modifier = Modifier.fillMaxWidth(.9f)
                        .clip(RoundedCornerShape(10.dp, 0.dp, 10.dp, 10.dp))
                        .background(color = colorResource(R.color.darkGray)),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp)) {
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
                                    .background(colorResource(R.color.darkGray))
                                    .padding(5.dp)
                                    .weight(.48f),
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

                            Spacer(modifier = Modifier.weight(.04f))

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
                                    .weight(.48f),
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
        }

    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun CardImage(
    cardData: CardData,
    setFocusedCard: (CardData) -> Unit,
    showCardPopup: () -> Unit,
    modifier: Modifier,
    allCardsFlag: Boolean,
    game: String
) {
    Box(modifier = modifier.clickable {
        setFocusedCard(cardData)
        showCardPopup()
    }) {
        if (cardData.image != "nocardimage" && cardData.image != null) {
            val decodedString = Base64.decode(cardData.image!!, 0)
            val img = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            val ribbon = painterResource(mapRarityToRibbon(cardData.rarity))
            Box {
                Image(
                    bitmap = img.asImageBitmap(),
                    contentDescription = "Card",
                    modifier = Modifier
                        .size(120.dp, 200.dp)
                        .align(Alignment.Center)
                )
                if (game == "yugioh") {
                    Image(
                        painter = ribbon,
                        contentDescription = "Rarity Ribbon",
                        modifier = Modifier
                            .size(120.dp, 200.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-12).dp)
                    )
                }
            }
        } else {
            Image(
                painter = painterResource(R.drawable.nocardimage),
                contentDescription = stringResource(R.string.card_image_not_loaded_context),
                modifier = Modifier.size(120.dp, 200.dp)
            )
        }

        if (allCardsFlag) {
            Text(
                text = "${cardData.quantity}x",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = (-5).dp)
                    .background(Color.Black.copy(alpha = 0.7f), shape = RoundedCornerShape(4.dp))
                    .padding(4.dp)
            )
        }
    }
}

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

    var subColQuant by remember { mutableStateOf(cardData.subcollections?.count{ it == subcolInfo.subcollectionid}.toString()) }
    var showCardDeletePopup by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        when (cardData.game) {
            "yugioh" -> {
                YugiohCardPopupInfo(cardData, navWebsite, Modifier)
            } "mtg" -> {
                MTGCardPopupInfo(cardData, navWebsite, Modifier)
            } else -> { //Pokemon
                PokemonCardPopupInfo(cardData, navWebsite, Modifier)
            }
        }
        if(allCardsFlag) {
            AddCardToSubcollectionPopup(modifier = Modifier,
                cardData = cardData,
                userid = userid,
                game = game,
                subcollections = subcollections,
                fullCardPool = fullCardPool,
                onCollectionChange = onCollectionChange,
                removeCard = removeCard,
                subcolInfo = subcolInfo,
                refreshUI = refreshUI,
                showCardDeletePopup = { showCardDeletePopup = true }
                )
        } else {
            Row {
                Text(stringResource(R.string.subcollection_quantity_label) + ": ")
                IconButton(
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
                ) {
                    Icon(painter = painterResource(R.drawable.minus_icon),
                        contentDescription = "Minus")
                }
                Text(subColQuant)
                IconButton(
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
                ) {
                    Icon(painter = painterResource(R.drawable.plus_icon),
                        contentDescription = "Plus")
                }
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
        RemoveFromCollectionPopup(modifier = Modifier,
            cardData = cardData,
            onDismissRequest = {showCardDeletePopup = false},
            allCardsFlag = allCardsFlag,
            subcolInfo = subcolInfo,
            userid = userid,
            fullCardPool = fullCardPool,
            onCollectionChange = onCollectionChange,
            removeCard = removeCard,
            refreshUI = refreshUI,
            showCardPopup = showCardPopup)
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun YugiohCardPopupInfo(cardData: CardData, navWebsite: (String) -> Unit, modifier: Modifier) {
    Row(modifier = modifier.padding(3.dp)) {
        Column(modifier = Modifier.weight(.4f)) {
            if (cardData.image != "nocardimage" && cardData.image != null) {
                val decodedString = Base64.decode(cardData.image!!, 0)
                val img =
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                Image(
                    bitmap = img.asImageBitmap(),
                    contentDescription = "Card",
                    Modifier
                        .size(120.dp, 200.dp)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.nocardimage),
                    contentDescription = stringResource(R.string.card_image_not_loaded_context),
                    Modifier
                        .size(120.dp, 200.dp)
                )
            }
            Text(cardData.cardname)
            CardPriceComponent(cardData, navWebsite)
            CardInfoBox(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                infoType = stringResource(R.string.card_id_label),
                infoData = cardData.cardid,
                split = .3f
            )

            CardInfoBox(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                infoType = stringResource(R.string.card_setcode_label),
                infoData = cardData.setcode,
                split = .3f)
        }
        Column(modifier = Modifier.weight(.6f)) {
            Card(modifier = Modifier.height(220.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Gray)
            ) {
                AutoResizeText(text = cardData.description!!.replace("\\n", "\n"),
                    fontSizeRange = FontSizeRange(10.sp, 30.sp, 1.sp),
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black,
                )
            }

            if (cardData.level != null) {
                CardInfoBox(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    infoType = stringResource(R.string.level_label),
                    infoData = cardData.level)
            }

            if (cardData.attribute != null) {
                CardInfoBox(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                    infoType = stringResource(R.string.attribute_label),
                    infoData = cardData.attribute)
            }

            if (cardData.type != null) {
                CardInfoBox(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                    infoType = stringResource(R.string.type_label),
                    infoData = cardData.type)
            }

            if (cardData.atk != null) {
                CardInfoBox(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                    infoType = stringResource(R.string.atk_label),
                    infoData = cardData.atk)
            }

            if (cardData.def != null) {
                CardInfoBox(modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                    infoType = stringResource(R.string.def_label),
                    infoData = cardData.def)
            }

            if (cardData.rarity != null) {
                CardInfoBox(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
                    infoType = stringResource(R.string.rarity_label),
                    infoData = cardData.rarity!!,
                )
            }
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun MTGCardPopupInfo(cardData: CardData, navWebsite: (String) -> Unit, modifier: Modifier) {
    Row(modifier = modifier) {
        Column {
            if (cardData.image != "nocardimage" && cardData.image != null) {
                val decodedString = Base64.decode(cardData.image!!, 0)
                val img =
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                Image(
                    bitmap = img.asImageBitmap(),
                    contentDescription = "Card",
                    Modifier
                        .size(120.dp, 200.dp)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.nocardimage),
                    contentDescription = stringResource(R.string.card_image_not_loaded_context),
                    Modifier
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

@OptIn(ExperimentalEncodingApi::class)
@Composable
fun PokemonCardPopupInfo(cardData: CardData, navWebsite: (String) -> Unit, modifier: Modifier) {
    Row(modifier = modifier) {
        Column {
            if (cardData.image != "nocardimage" && cardData.image != null) {
                val decodedString = Base64.decode(cardData.image!!, 0)
                val img =
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                Image(
                    bitmap = img.asImageBitmap(),
                    contentDescription = "Card",
                    Modifier
                        .size(120.dp, 200.dp)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.nocardimage),
                    contentDescription = stringResource(R.string.card_image_not_loaded_context),
                    Modifier
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

@Composable
fun CardInfoBox(modifier: Modifier = Modifier, infoType: String, infoData: String,
                icons: Array<String>? = null, split: Float = 0.4f) {
    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
        ) {
            Box(modifier = Modifier.weight(split).background(colorResource(R.color.textLightGrey))) {
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    text = infoType,
                )
            }
            Box(modifier = Modifier.weight(1-split).background(colorResource(R.color.gray))) {
                if (icons != null) {
                    Text(modifier = Modifier.padding(start = 5.dp),
                        text = icons[0])
                } else {
                    Text(modifier = Modifier.padding(start = 5.dp),
                        text = infoData)
                }
            }
        }
    }
}

@Composable
fun AddCardToSubcollectionPopup(modifier: Modifier, cardData: CardData, userid: String, game: String,
                                subcollections: Array<SubcollectionInfo>, fullCardPool: Array<CardData>,
                                onCollectionChange: (Array<CardData>) -> Unit, removeCard: (CardData) -> Unit,
                                subcolInfo: SubcollectionInfo, refreshUI: () -> Unit,
                                showCardDeletePopup: () -> Unit) {
    val optionInfo = subcollections.filter( predicate = {
        it.game == game
    })
    val staticResponseText = stringResource(R.string.card_added_to_subcollection_message)
    val optionList = optionInfo.map { it.name }
    var selectedOption by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableStateOf(1) }
    var responseText by remember { mutableStateOf("")}
    var cardQuantity by remember { mutableStateOf(cardData.quantity.toString())}
    var cardQuantErr by remember { mutableStateOf(false) }

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
                    showCardDeletePopup()
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
fun RemoveFromCollectionPopup(modifier: Modifier, cardData: CardData, onDismissRequest: () -> Unit, allCardsFlag: Boolean,
                              subcolInfo: SubcollectionInfo, userid: String, fullCardPool: Array<CardData>,
                              onCollectionChange: (Array<CardData>) -> Unit, removeCard: (CardData) -> Unit, refreshUI: () -> Unit,
                              showCardPopup: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
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
                onDismissRequest()
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
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.no_label))
            }
        }
    )
}

@Composable
fun CardPriceComponent(cardData: CardData, navWebsite: (String) -> Unit) {
    Card(modifier = Modifier,
        onClick = {
            if (cardData.purchaseurl != null) {
                navWebsite(cardData.purchaseurl!!)
            }
        },
        shape = RectangleShape,
        border = BorderStroke(1.dp, Color.Black),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Row(modifier = Modifier.padding(vertical = 1.dp, horizontal = 3.dp)) {
            Text(
                String.format(
                    stringResource(R.string.card_price_label),
                    cardData.price,
                    "$"
                )
            )
            Image(modifier = Modifier.size(25.dp, 25.dp),
                painter = painterResource(R.drawable.tcg_player_icon),
                contentDescription = stringResource(R.string.tcgplayer_label),
            )
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
    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterTextfield(
                modifier = Modifier.weight(.3f).padding(horizontal = 5.dp),
                label = stringResource(R.string.min),
                value = minVal,
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
            Column(
                modifier = Modifier.weight(.11f).height(30.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(.5f))
                Text(
                    modifier = Modifier.weight(.5f),
                    text = "-"
                )
            }

            FilterTextfield(
                modifier = Modifier.weight(.3f).padding(horizontal = 5.dp),
                label = stringResource(R.string.max),
                value = maxVal,
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
    Column(modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterTextfield(
                modifier = Modifier.weight(.3f).padding(horizontal = 5.dp),
                label = stringResource(R.string.min),
                value = minVal,
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
            Column(
                modifier = Modifier.weight(.11f).height(30.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(.5f))
                Text(
                    modifier = Modifier.weight(.5f),
                    text = "-"
                )
            }

            FilterTextfield(
                modifier = Modifier.weight(.3f).padding(horizontal = 5.dp),
                label = stringResource(R.string.max),
                value = maxVal,
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
}


@Composable
fun AutoResizeText(
    text: String,
    fontSizeRange: FontSizeRange,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
) {
    var textSize by remember { mutableStateOf<TextUnit?>(null) }
    var parentSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .onSizeChanged { parentSize = it }
            .background(Color.LightGray) // optional: for debugging
    ) {
        if (parentSize.width > 0 && parentSize.height > 0) {
            val constraints = Constraints.fixed(parentSize.width - 10, parentSize.height - 10)
            val fontSize = rememberAutoResizedFontSize(
                text = AnnotatedString(text),
                fontSizeRange = fontSizeRange,
                constraints = constraints,
                textStyle = style
            )
            textSize = fontSize
        }

        if (textSize != null) {
            Text(
                text = text,
                style = style.copy(fontSize = textSize!!),
                color = color,
                modifier = Modifier.fillMaxSize().padding(5.dp),
                softWrap = true,
            )
        }
    }
}

@Composable
fun rememberAutoResizedFontSize(
    text: AnnotatedString,
    fontSizeRange: FontSizeRange,
    constraints: Constraints,
    textStyle: TextStyle = TextStyle.Default,
    density: Density = LocalDensity.current,
): TextUnit {
    val textMeasurer = rememberTextMeasurer()
    val stepSizePx = with(density) { fontSizeRange.step.toPx() }
    val minFontSizePx = with(density) { fontSizeRange.min.toPx() }
    val maxFontSizePx = with(density) { fontSizeRange.max.toPx() }

    return remember(text, constraints) {
        var low = minFontSizePx
        var high = maxFontSizePx
        var bestSize = low

        while ((high - low) >= stepSizePx) {
            val mid = (low + high) / 2
            val fontSizeSp = with(density) { mid.toSp() }

            val result = textMeasurer.measure(
                text = text,
                style = textStyle.copy(fontSize = fontSizeSp),
                constraints = constraints,
                softWrap = true
            )

            val fits = !result.hasVisualOverflow

            if (fits) {
                bestSize = mid
                low = mid + stepSizePx
            } else {
                high = mid - stepSizePx
            }
        }

        // Try stepping up once more to see if it still fits
        val stepUp = bestSize + stepSizePx
        val finalSize = if (stepUp <= maxFontSizePx) {
            val fontSizeSp = with(density) { stepUp.toSp() }
            val result = textMeasurer.measure(
                text = text,
                style = textStyle.copy(fontSize = fontSizeSp),
                constraints = constraints,
                softWrap = true
            )
            if (!result.hasVisualOverflow) stepUp else bestSize
        } else bestSize

        with(density) { finalSize.toSp() }
    }
}

data class FontSizeRange(
    val min: TextUnit,
    val max: TextUnit,
    val step: TextUnit = DEFAULT_TEXT_STEP,
) {
    init {
        require(min < max) { "min should be less than max, $this" }
        require(step.value > 0) { "step should be greater than 0, $this" }
    }

    companion object {
        private val DEFAULT_TEXT_STEP = 1.sp
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

