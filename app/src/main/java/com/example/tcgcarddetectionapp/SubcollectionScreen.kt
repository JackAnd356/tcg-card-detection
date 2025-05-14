package com.example.tcgcarddetectionapp

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.core.text.isDigitsOnly
import coil3.compose.AsyncImage
import com.example.tcgcarddetectionapp.models.AddRemoveCardModel
import com.example.tcgcarddetectionapp.models.CardData
import com.example.tcgcarddetectionapp.models.GenericSuccessErrorResponseModel
import com.example.tcgcarddetectionapp.models.SaveToSubcollectionRequestModel
import com.example.tcgcarddetectionapp.models.SaveToSubcollectionResponseModel
import com.example.tcgcarddetectionapp.models.SubcollectionInfo
import com.example.tcgcarddetectionapp.models.Weakness
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Collections
import org.apache.commons.text.StringEscapeUtils

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
    var currentFocusedCard by remember { mutableStateOf(
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
    val scrollState = rememberScrollState()
    var refreshFlag by remember { mutableIntStateOf(0) }
    var cardData = remember { mutableStateListOf<CardData>() }
    var navWebsite by remember { mutableStateOf("") }
    var optionsExpanded by remember { mutableStateOf(false) }
    var showEditPopup by remember { mutableStateOf(false) }
    var showDeletePopup by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var filterList = remember { mutableStateListOf<(CardData) -> Boolean>()}
    var listOfTypes = remember {  mutableStateListOf<String>()}
    var listOfAttributes = remember {  mutableStateListOf<String>()}
    var listOfRarities = remember { mutableStateListOf<String>() }
    var listOfFrames = remember { mutableStateListOf<String>()}
    var selectedTypeFilter by remember { mutableStateOf("") }
    var selectedTypeFilterIndex by remember { mutableIntStateOf(1) }
    var selectedAttributeFilter by remember { mutableStateOf("") }
    var selectedAttributeFilterIndex by remember { mutableIntStateOf(1) }
    var selectedRarityFilter by remember { mutableStateOf("") }
    var selectedRarityFilterIndex by remember { mutableIntStateOf(1) }
    var selectedFrameFilter by remember { mutableStateOf("") }
    var selectedFrameFilterIndex by remember { mutableIntStateOf(1) }
    var selectedMinQuantity by remember { mutableStateOf("") }
    var minQuantErr by remember { mutableStateOf(false) }
    var selectedMaxQuantity by remember { mutableStateOf("") }
    var maxQuantErr by remember { mutableStateOf(false) }
    var selectedMinLevel by remember { mutableStateOf("") }
    var minLevelErr by remember { mutableStateOf(false) }
    var selectedMaxLevel by remember { mutableStateOf("") }
    var maxLevelErr by remember { mutableStateOf(false) }
    var selectedMinPrice by remember { mutableStateOf("") }
    var minPriceErr by remember { mutableStateOf(false) }
    var selectedMaxPrice by remember { mutableStateOf("") }
    var maxPriceErr by remember { mutableStateOf(false) }

    val screenHeight = LocalConfiguration.current.screenHeightDp
    val forceRecomposeState = rememberUpdatedState(refreshFlag) //Says unused. However it is very much so required
    val backArrowSize = (screenHeight * 0.02)

    if (!listOfTypes.contains("")) {
        listOfTypes.add("")
    }
    if (!listOfAttributes.contains("")) {
        listOfAttributes.add("")
    }
    if (!listOfRarities.contains("")) {
        listOfRarities.add("")
    }
    if(!listOfFrames.contains("")) {
        listOfFrames.add("")
    }

    val listOfMTGFormats = listOf("",
        stringResource(R.string.mtg_standard),
        stringResource(R.string.mtg_legacy),
        stringResource(R.string.mtg_pioneer),
        stringResource(R.string.mtg_pauper),
        stringResource(R.string.mtg_modern),
        stringResource(R.string.mtg_commander),
        stringResource(R.string.mtg_vintage),)
    var selectedLegalityIndex by remember { mutableStateOf(1) }


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
                if ((game == "yugioh" || game == "mtg") && !listOfTypes.contains(card.type!!)) {
                    listOfTypes.add(card.type)
                }
                if (card.attribute != null && !listOfAttributes.contains(card.attribute)) {
                    listOfAttributes.add(card.attribute)
                }
                if (card.color != null) {
                    for (color in card.color) {
                        if (!listOfAttributes.contains(color)) {
                            listOfAttributes.add(color)
                        }
                    }
                }
                if (card.rarity != null && !listOfRarities.contains(card.rarity)) {
                    listOfRarities.add(card.rarity!!)
                }
                if (game == "yugioh" && !listOfFrames.contains(card.frameType!!)) {
                    listOfFrames.add(card.frameType)
                }
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
                    if (!listOfTypes.contains(card.type!!)) {
                        listOfTypes.add(card.type)
                    }
                    if (card.attribute != null && !listOfAttributes.contains(card.attribute)) {
                        listOfAttributes.add(card.attribute)
                    }
                    if (card.rarity != null && !listOfRarities.contains(card.rarity)) {
                        listOfRarities.add(card.rarity!!)
                    }
                    if (!listOfFrames.contains(card.frameType!!)) {
                        listOfFrames.add(card.frameType)
                    }
                }
            }
        }
        else {
            fullCardPool.sortedWith( compareBy<CardData> {
                it.color!![0]
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
                    if (card.game == "mtg" && !listOfTypes.contains(card.type!!)) {
                        listOfTypes.add(card.type)
                    }
                    if (card.color != null) {
                        for (color in card.color) {
                            if (!listOfAttributes.contains(color)) {
                                listOfAttributes.add(color)
                            }
                        }
                    }
                    if (card.rarity != null && !listOfRarities.contains(card.rarity)) {
                        listOfRarities.add(card.rarity!!)
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
                    onClickLabel = stringResource(R.string.back_to_main_collections_button_label),
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = true
                )
                .width((backArrowSize * 2).dp)
                .padding(top = 18.dp)) {
                Icon(modifier = Modifier
                    .height(backArrowSize.dp)
                    .width(backArrowSize.dp),
                    painter = painterResource(R.drawable.arrow_left_icon),
                    contentDescription = stringResource(R.string.back_to_main_collections_button_label),
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
                                style = appTypography.displayMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = String.format(
                                    stringResource(R.string.total_value_label),
                                    subcolInfo.totalValue ?: 0.00,
                                    "$"
                                ),
                                style = appTypography.headlineMedium,
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
                                        contentDescription = stringResource(R.string.more)
                                    )
                                }
                                DropdownMenu(
                                    expanded = optionsExpanded,
                                    onDismissRequest = {
                                        optionsExpanded = false
                                    }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(
                                            text = stringResource(R.string.edit_option_label),
                                            style = appTypography.labelSmall
                                        ) },
                                        onClick = {
                                            showEditPopup = !showEditPopup
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(
                                            text = stringResource(R.string.delete_option_label),
                                            style = appTypography.labelSmall
                                        ) },
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
                                style = appTypography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.weight(1f))
                        }
                        Text(
                            text = String.format(
                                stringResource(R.string.total_cards_label),
                                subcolInfo.cardCount ?: 0
                            ),
                            style = appTypography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if(!allCardsFlag) {
                Row(modifier = Modifier
                    .fillMaxWidth(.9f)
                    .align(Alignment.CenterHorizontally)) {
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
                            style = appTypography.labelLarge
                        )
                    }
                }
            }

            Row(modifier = Modifier
                .fillMaxWidth(.9f)
                .align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    modifier = Modifier
                        .weight(.75f)
                        .padding(end = 5.dp)
                        .height(60.dp)
                        .heightIn(35.dp),
                    value = searchTerm,
                    onValueChange = { searchTerm = it },
                    singleLine = true,
                    label = { Text(
                        text = stringResource(R.string.search_label),
                        style = appTypography.labelMedium
                    ) }
                )
                Button(
                    modifier = Modifier
                        .weight(.25f)
                        .height(60.dp),
                    shape = if (!showFilters) RoundedCornerShape(10.dp, 10.dp, 10.dp, 10.dp)
                            else RoundedCornerShape(10.dp, 10.dp, 0.dp, 0.dp),
                    onClick = {
                        showFilters = !showFilters
                    },
                    colors = ButtonDefaults.buttonColors(colorResource(R.color.darkGray))
                ) {
                    Text(
                        text = stringResource(R.string.filter_button_label),
                        style = appTypography.labelLarge
                    )
                }
            }

            Box(modifier = Modifier.align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.TopCenter) {
                var filteredCardData = mutableListOf<CardData>()
                cardData.forEach { card ->
                    if (searchTerm.uppercase() in card.cardname.uppercase()) {
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
                    contentPadding = PaddingValues(vertical = 20.dp)
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
                    val alwaysFilters = listOf<@Composable (Modifier) -> Unit> (
                        {modifier -> MinMaxIntComponent(
                            minVal = selectedMinQuantity,
                            maxVal = selectedMaxQuantity,
                            onMinValChange = { selectedMinQuantity = it },
                            onMaxValChange = { selectedMaxQuantity = it },
                            label = stringResource(R.string.quantity_filter_label),
                            minError = minQuantErr,
                            onMinErrChange = { minQuantErr = it },
                            maxError = maxQuantErr,
                            onMaxErrChange = { maxQuantErr = it },
                            modifier = modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colorResource(R.color.darkGray))
                                .padding(5.dp)
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                                attribute = selectedAttributeFilter,
                                rarity = selectedRarityFilter,
                                frame = selectedFrameFilter,
                                legality = selectedLegalityIndex,
                            ).forEach {
                                    func ->
                                filterList.add(func)
                            }
                        }},
                        {modifier -> MinMaxDoubleComponent(
                            minVal = selectedMinPrice,
                            maxVal = selectedMaxPrice,
                            onMinValChange = { selectedMinPrice = it },
                            onMaxValChange = {  selectedMaxPrice = it },
                            label = stringResource(R.string.price_filter_label),
                            minError = minPriceErr,
                            onMinErrChange = { minPriceErr = it },
                            maxError = maxPriceErr,
                            modifier = modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colorResource(R.color.darkGray))
                                .padding(5.dp),
                            onMaxErrChange = { maxPriceErr = it }
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                                attribute = selectedAttributeFilter,
                                rarity = selectedRarityFilter,
                                frame = selectedFrameFilter,
                                legality = selectedLegalityIndex,
                            ).forEach {
                                    func ->
                                filterList.add(func)
                            }
                        }},
                        {modifier -> DropdownSelectorFilter(
                            label = stringResource(R.string.rarity_label),
                            data = selectedRarityFilterIndex,
                            options = listOfRarities.toList(),
                            onSelectedValChange = { rarity, index ->
                                selectedRarityFilter = rarity
                                selectedRarityFilterIndex = index + 1
                            },
                            modifier = modifier.padding(5.dp)
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                                attribute = selectedAttributeFilter,
                                rarity = selectedRarityFilter,
                                frame = selectedFrameFilter,
                                legality = selectedLegalityIndex,
                            ).forEach { func ->
                                filterList.add(func)
                            }
                        }},
                    )

                    val conditionalFilters = listOf<Pair<Boolean, @Composable (Modifier) -> Unit>>(
                        (game == "yugioh") to {modifier -> MinMaxIntComponent(
                            minVal = selectedMinLevel,
                            maxVal = selectedMaxLevel,
                            onMinValChange = { selectedMinLevel = it },
                            onMaxValChange = { selectedMaxLevel = it },
                            label = stringResource(R.string.level_filter_label),
                            minError = minLevelErr,
                            onMinErrChange = { minLevelErr = it },
                            maxError = maxLevelErr,
                            onMaxErrChange = { maxLevelErr = it },
                            modifier = modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colorResource(R.color.darkGray))
                                .padding(5.dp)
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                                attribute = selectedAttributeFilter,
                                rarity = selectedRarityFilter,
                                frame = selectedFrameFilter,
                                legality = selectedLegalityIndex,
                            ).forEach { func ->
                                filterList.add(func)
                            }
                        }},
                        (game == "yugioh" || game == "mtg") to { modifier -> DropdownSelectorFilter(
                            label = "Type",
                            data = selectedTypeFilterIndex,
                            options = listOfTypes.toList(),
                            onSelectedValChange = { type, index ->
                                selectedTypeFilter = type
                                selectedTypeFilterIndex = index + 1
                            },
                            modifier = modifier.padding(5.dp)
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                                attribute = selectedAttributeFilter,
                                rarity = selectedRarityFilter,
                                frame = selectedFrameFilter,
                                legality = selectedLegalityIndex,
                            ).forEach { func ->
                                filterList.add(func)
                            }
                        }},
                        (game == "yugioh") to { modifier -> DropdownSelectorFilter(
                            label = stringResource(R.string.attribute_label),
                            data = selectedAttributeFilterIndex,
                            options = listOfAttributes.toList(),
                            onSelectedValChange = { attr, index ->
                                selectedAttributeFilter = attr
                                selectedAttributeFilterIndex = index + 1
                            },
                            modifier = modifier.padding(5.dp),
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                                attribute = selectedAttributeFilter,
                                rarity = selectedRarityFilter,
                                frame = selectedFrameFilter,
                                legality = selectedLegalityIndex,
                            ).forEach { func ->
                                filterList.add(func)
                            }
                        }},
                        (game == "yugioh") to { modifier -> DropdownSelectorFilter(
                            label = "Border",
                            data = selectedFrameFilterIndex,
                            options = listOfFrames.toList(),
                            onSelectedValChange = { attr, index ->
                                selectedFrameFilter = attr
                                selectedFrameFilterIndex = index + 1
                            },
                            modifier = modifier.padding(5.dp),
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                                attribute = selectedAttributeFilter,
                                rarity = selectedRarityFilter,
                                frame = selectedFrameFilter,
                                legality = selectedLegalityIndex,
                            ).forEach { func ->
                                filterList.add(func)
                            }
                        }},
                        (game == "mtg") to {modifier -> DropdownSelectorFilter(
                            label = stringResource(R.string.color_label),
                            data = selectedAttributeFilterIndex,
                            options = listOfAttributes.toList(),
                            onSelectedValChange = {attr, index ->
                                selectedAttributeFilter = attr
                                selectedAttributeFilterIndex = index + 1
                            },
                            modifier = modifier.padding(5.dp),
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                                attribute = selectedAttributeFilter,
                                rarity = selectedRarityFilter,
                                frame = selectedFrameFilter,
                                legality = selectedLegalityIndex,
                            ).forEach { func ->
                                filterList.add(func)
                            }
                        }},
                        (game == "mtg") to {modifier -> DropdownSelectorFilter(
                            label = stringResource(R.string.mtg_legalities),
                            data = selectedLegalityIndex,
                            options = listOfMTGFormats,
                            onSelectedValChange = { _, index ->
                                selectedLegalityIndex = index + 1
                            },
                            modifier = modifier.padding(5.dp),
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                                attribute = selectedAttributeFilter,
                                rarity = selectedRarityFilter,
                                frame = selectedFrameFilter,
                                legality = selectedLegalityIndex,
                            ).forEach { func ->
                                filterList.add(func)
                            }
                        }},
                        (game == "pokemon") to { modifier -> DropdownSelectorFilter(
                            label = stringResource(R.string.type_label),
                            data = selectedAttributeFilterIndex,
                            options = listOfAttributes.toList(),
                            onSelectedValChange = {attr, index ->
                                selectedAttributeFilter = attr
                                selectedAttributeFilterIndex = index + 1
                            },
                            modifier = modifier.padding(5.dp),
                        ) {
                            filterList.clear()
                            recalculateFilterList(
                                type = selectedTypeFilter,
                                quantityMin = selectedMinQuantity,
                                levelMin = selectedMinLevel,
                                quantityMax = selectedMaxQuantity,
                                levelMax = selectedMaxLevel,
                                priceMin = selectedMinPrice,
                                priceMax = selectedMaxPrice,
                                attribute = selectedAttributeFilter,
                                rarity = selectedRarityFilter,
                                frame = selectedFrameFilter,
                                legality = selectedLegalityIndex,
                            ).forEach { func ->
                                filterList.add(func)
                            }
                        }},
                    )

                    val visibleItems = buildList {
                        addAll(alwaysFilters)
                        addAll(conditionalFilters.filter { it.first }.map { it.second })
                    }

                    val rows = visibleItems.chunked(2)

                    Column(modifier = Modifier
                        .fillMaxWidth(.9f)
                        .clip(RoundedCornerShape(10.dp, 0.dp, 10.dp, 10.dp))
                        .background(color = colorResource(R.color.darkGray)),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        for (row in rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                row.getOrNull(0)?.let { item ->
                                    item(
                                        Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colorResource(R.color.darkGray))
                                            .padding(5.dp)
                                            .weight(0.48f)
                                    )
                                }

                                if (row.size == 2) {
                                    Spacer(modifier = Modifier.weight(0.04f))
                                    row.getOrNull(1)?.let { item ->
                                        item(
                                            Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(colorResource(R.color.darkGray))
                                                .padding(5.dp)
                                                .weight(0.48f)
                                        )
                                    }
                                } else {
                                    // Fills remaining space if only one item
                                    Spacer(modifier = Modifier.weight(0.52f))
                                }
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

@Composable
fun CardImage(
    cardData: CardData,
    setFocusedCard: (CardData) -> Unit,
    showCardPopup: () -> Unit,
    modifier: Modifier,
    allCardsFlag: Boolean,
    game: String
) {
    val ribbon = mapRarityToRibbon(cardData.rarity)
    val noCardImage = R.drawable.nocardimage
    val cardImageNotLoadedText = stringResource(R.string.card_image_not_loaded_context)

    Box(modifier = modifier.clickable {
        setFocusedCard(cardData)
        showCardPopup()
    }) {
        if (cardData.imageBitmap != null) {
            Box {
                Image(
                    bitmap = cardData.imageBitmap!!,
                    contentDescription = cardData.cardname,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
                if (game == "yugioh") {
                    AsyncImage(
                        model = ribbon,
                        contentDescription = cardData.rarity,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
            }
        } else {
            AsyncImage(
                model = noCardImage,
                contentDescription = cardImageNotLoadedText,
                modifier = Modifier.size(120.dp, 200.dp)
            )
        }

        if (allCardsFlag) {
            Text(
                text = "${cardData.quantity}x",
                color = Color.White,
                fontSize = appTypography.labelMedium.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(y = 7.dp)
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
            )
            ChangeQuantityCardComponent(
                modifier = Modifier,
                label = stringResource(R.string.quantity_filter_label),
                cardData = cardData,
                userid = userid,
                fullCardPool = fullCardPool,
                onCollectionChange = onCollectionChange,
                removeCard = removeCard,
                refreshUI = refreshUI,
                subcolInfo = subcolInfo,
                showCardDeletePopup = { showCardDeletePopup = true }
            )
        } else {
            ChangeQuantityCardComponent(modifier = Modifier,
                minusOnClick = {
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

                },
                plusOnClick = {
                    if (!subcolInfo.isDeck || (cardData.subcollections!!.count{ it == subcolInfo.subcollectionid} < mapGameToMaxCopiesInDeck(subcolInfo.game))) {
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
                },
                subColQuant = subColQuant,
                label = stringResource(R.string.subcollection_quantity_label)
            )
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

@Composable
fun YugiohCardPopupInfo(cardData: CardData, navWebsite: (String) -> Unit, modifier: Modifier) {
    Column(modifier = modifier.padding(3.dp)) {
        Text(text = StringEscapeUtils.unescapeJava(cardData.cardname),
            style = appTypography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Row(modifier = Modifier) {
            Column(modifier = Modifier.weight(.4f)) {
                if (cardData.imageBitmap != null) {
                    Image(
                        bitmap = cardData.imageBitmap!!,
                        contentDescription = cardData.cardname,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.nocardimage),
                        contentDescription = stringResource(R.string.card_image_not_loaded_context),
                        modifier = Modifier.padding(end = 5.dp)
                    )
                }
                CardPriceComponent(modifier = Modifier.padding(vertical = 5.dp),
                    cardData = cardData, navWebsite = navWebsite)
            }
            Column(modifier = Modifier.weight(.6f)) {
                CardInfoBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    infoType = stringResource(R.string.card_id_label),
                    infoData = cardData.cardid
                )

                CardInfoBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    infoType = stringResource(R.string.card_setcode_label),
                    infoData = cardData.setcode
                )

                if (cardData.level != null) {
                    CardInfoBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        infoType = stringResource(R.string.level_label),
                        infoData = cardData.level
                    )
                }

                if (cardData.attribute != null) {
                    CardInfoBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        infoType = stringResource(R.string.attribute_label),
                        infoData = cardData.attribute
                    )
                }

                if (cardData.type != null) {
                    CardInfoBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        infoType = stringResource(R.string.type_label),
                        infoData = cardData.type
                    )
                }

                if (cardData.atk != null) {
                    CardInfoBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        infoType = stringResource(R.string.atk_label),
                        infoData = cardData.atk
                    )
                }

                if (cardData.def != null) {
                    CardInfoBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        infoType = stringResource(R.string.def_label),
                        infoData = cardData.def
                    )
                }

                if (cardData.rarity != null) {
                    CardInfoBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        infoType = stringResource(R.string.rarity_label),
                        infoData = cardData.rarity!!,
                    )
                }
            }
        }
        Card(
            modifier = Modifier
                .height(120.dp)
                .padding(vertical = 5.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Gray)
        ) {
            AutoResizeText(
                text = StringEscapeUtils.unescapeJava(cardData.description!!),
                fontSizeRange = FontSizeRange(15.sp, 30.sp, 2.sp),
                modifier = Modifier.fillMaxSize(),
                color = Color.Black,
            )
        }
    }
}

@Composable
fun MTGCardPopupInfo(cardData: CardData, navWebsite: (String) -> Unit, modifier: Modifier) {
    Column(modifier = modifier.padding(3.dp)) {
        Text(
            text = StringEscapeUtils.unescapeJava(cardData.cardname),
            style = appTypography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Row(modifier = Modifier) {
            Column(modifier = Modifier.weight(.4f)) {
                if (cardData.imageBitmap != null) {
                    Image(
                        bitmap = cardData.imageBitmap!!,
                        contentDescription = cardData.cardname,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.nocardimage),
                        contentDescription = stringResource(R.string.card_image_not_loaded_context),
                        Modifier.padding(end = 5.dp)
                    )
                }

                CardPriceComponent(modifier = Modifier.padding(vertical = 5.dp),
                    cardData = cardData,
                    navWebsite = navWebsite
                )
            }
            Column(modifier = Modifier.weight(.6f)) {
                if (cardData.cost != null) {
                    CardInfoBox(
                        modifier = Modifier.padding(vertical = 5.dp),
                        infoType = stringResource(R.string.cost_label),
                        infoData = "",
                        icons = stripColorString(cardData.cost),
                        split = 0.5f
                    )
                }

                if (cardData.type != null) {
                    CardInfoBox(
                        modifier = Modifier.padding(bottom = 5.dp),
                        infoType = stringResource(R.string.type_label),
                        infoData = cardData.type,
                        split = 0.5f
                    )
                }

                if (cardData.atk != null) {
                    CardInfoBox(
                        modifier = Modifier.padding(bottom = 5.dp),
                        infoType = stringResource(R.string.power_label),
                        infoData = cardData.atk,
                        split = 0.5f
                    )
                }

                if (cardData.def != null) {
                    CardInfoBox(
                        modifier = Modifier.padding(bottom = 5.dp),
                        infoType = stringResource(R.string.toughness_label),
                        infoData = cardData.def,
                        split = 0.5f
                    )
                }
            }
        }
        Card(
            modifier = Modifier
                .height(120.dp)
                .padding(vertical = 5.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Gray)
        ) {
            AutoResizeText(
                text = StringEscapeUtils.unescapeJava(cardData.description!!),
                fontSizeRange = FontSizeRange(15.sp, 30.sp, 2.sp),
                modifier = Modifier.fillMaxSize(),
                color = Color.Black,
            )
        }
    }
}

@Composable
fun PokemonCardPopupInfo(cardData: CardData, navWebsite: (String) -> Unit, modifier: Modifier) {
    Column(modifier = modifier.padding(3.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center) {
            Text(
                text = StringEscapeUtils.unescapeJava(cardData.cardname),
                style = appTypography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            if (cardData.color != null) {
                for (color in cardData.color) {
                    Image(
                        painter = painterResource(mapPokemonTypeToIcon(color)),
                        contentDescription = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Row(modifier = Modifier) {
            Column(modifier = Modifier.weight(.4f)) {
                if (cardData.imageBitmap != null) {
                    Image(
                        bitmap = cardData.imageBitmap!!,
                        contentDescription = cardData.cardname,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.nocardimage),
                        contentDescription = stringResource(R.string.card_image_not_loaded_context),
                        modifier = Modifier.padding(end = 5.dp)
                    )
                }

                CardPriceComponent(modifier = Modifier.padding(vertical = 5.dp),
                    cardData = cardData,
                    navWebsite = navWebsite
                )
            }
            Column(modifier = Modifier.weight(.6f)) {
                CardInfoBox(
                    modifier = Modifier.padding(vertical = 5.dp),
                    infoType = stringResource(R.string.card_id_label),
                    infoData = cardData.cardid,
                    split = .5f
                )
                CardInfoBox(
                    modifier = Modifier.padding(bottom = 5.dp),
                    infoType = stringResource(R.string.card_setcode_label),
                    infoData = cardData.setcode,
                    split = .5f
                )
                if (cardData.type != null) {
                    CardInfoBox(
                        modifier = Modifier.padding(bottom = 5.dp),
                        infoType =  stringResource(R.string.pokemon_type_label),
                        infoData = cardData.type,
                        split = .5f
                    )
                }
                if (cardData.hp != null) {
                    CardInfoBox(
                        modifier = Modifier.padding(bottom = 5.dp),
                        infoType = stringResource(R.string.hp),
                        infoData = cardData.hp,
                        split = .5f
                    )
                }
                if (cardData.weaknesses != null) {
                    CardInfoBox(
                        modifier = Modifier.padding(bottom = 5.dp),
                        weaknesses = cardData.weaknesses,
                        split = .5f
                    )
                }
                if (cardData.retreat != null) {
                    CardInfoBox(
                        modifier = Modifier.padding(bottom = 5.dp),
                        infoType = stringResource(R.string.retreat),
                        icons = cardData.retreat,
                        split = .5f
                    )
                }

                if (cardData.evolvesFrom != null) {
                    CardInfoBox(
                        modifier = Modifier.padding(bottom = 5.dp),
                        infoType = stringResource(R.string.evolves_from),
                        infoData = cardData.evolvesFrom,
                        split = .5f
                    )
                }

            }
        }
        if (cardData.abilities != null) {
            PokemonAbilityBox(cardData = cardData)
        }
        PokemonAttackBox(cardData = cardData)
    }
}

@Composable
fun CardInfoBox(modifier: Modifier = Modifier, infoType: String, infoData: String,
                icons: CharArray? = null, split: Float = 0.4f, iconSize: Dp = 16.dp) {
    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
        ) {
            Box(modifier = Modifier
                .weight(split)
                .background(colorResource(R.color.textLightGrey))) {
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    style = appTypography.labelMedium,
                    text = infoType,
                )
            }
            Box(modifier = Modifier
                .weight(1 - split)
                .background(colorResource(R.color.gray))) {
                if (icons != null) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (icon in icons) {
                            Image(painter = painterResource(mapMTGColorToIcon(icon)),
                                contentDescription = icon.toString(),
                                modifier = Modifier.size(iconSize))
                        }
                    }
                } else {
                    Text(modifier = Modifier.padding(start = 5.dp),
                        text = infoData,
                        style = appTypography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun CardInfoBox(modifier: Modifier, infoType: String, icons: Array<String>,
                split: Float = .4f, iconSize: Dp = 16.dp
) {
    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
        ) {
            Box(modifier = Modifier
                .weight(split)
                .background(colorResource(R.color.textLightGrey))) {
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    style = appTypography.labelMedium,
                    text = infoType,
                )
            }
            Box(modifier = Modifier
                .weight(1 - split)
                .background(colorResource(R.color.gray))) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (icon in icons) {
                        Image(painter = painterResource(mapPokemonTypeToIcon(icon)),
                            contentDescription = icon,
                            modifier = Modifier.size(iconSize))
                    }
                }
            }
        }
    }
}

@Composable
fun CardInfoBox(modifier: Modifier = Modifier, weaknesses: Array<Weakness>,
                split: Float = .4f, iconSize: Dp = 16.dp) {
    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
        ) {
            Box(modifier = Modifier
                .weight(split)
                .background(colorResource(R.color.textLightGrey))) {
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    style = appTypography.labelMedium,
                    text = stringResource(R.string.weakness_label),
                )
            }
            Box(modifier = Modifier
                .weight(1 - split)
                .background(colorResource(R.color.gray))) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (weakness in weaknesses) {
                        Image(painter = painterResource(mapPokemonTypeToIcon(weakness.type)),
                            contentDescription = weakness.type,
                            modifier = Modifier.size(iconSize))
                        Text(
                            text = weakness.value,
                            style = appTypography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PokemonAttackBox(modifier: Modifier = Modifier, cardData: CardData) {
    Card(modifier = modifier
        .padding(bottom = 5.dp)
        .border(width = 2.dp, Color.Black, shape = RoundedCornerShape(10.dp))
        .clip(RectangleShape)
        .padding(5.dp),
        colors = CardColors(containerColor = Color.White, contentColor = Color.Black,
            disabledContainerColor = Color.White, disabledContentColor = Color.Black)
    ) {
        cardData.attacks!!.forEach { attack ->
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (icon in attack.cost) {
                        Image(painter = painterResource(mapPokemonTypeToIcon(icon)),
                            contentDescription = icon,
                            modifier = Modifier.size(24.dp))
                    }
                    Text(modifier = Modifier,
                        text = attack.name,
                        style = appTypography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }


                Text(
                    text = attack.damage,
                    style = appTypography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (attack.text != null) {
                Text(
                    text = StringEscapeUtils.unescapeJava(attack.text),
                    style = appTypography.labelSmall,
                )
            }
        }
    }
}

@Composable
fun PokemonAbilityBox(modifier: Modifier = Modifier, cardData: CardData) {
    Card(
        modifier = modifier
            .padding(bottom = 5.dp)
            .border(width = 2.dp, Color.Black, shape = RoundedCornerShape(10.dp))
            .clip(RectangleShape)
            .padding(5.dp),
        colors = CardColors(
            containerColor = Color.White, contentColor = Color.Black,
            disabledContainerColor = Color.White, disabledContentColor = Color.Black
        )
    ) {
        cardData.abilities!!.forEach { ability ->
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                Text(modifier = Modifier.padding(end = 10.dp),
                    text = ability.type,
                    style = appTypography.labelLarge)
                Text(modifier = Modifier,
                    text = ability.name,
                    style = appTypography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = StringEscapeUtils.unescapeJava(ability.text),
                style = appTypography.labelSmall,
            )
        }

    }
}

@Composable
fun AddCardToSubcollectionPopup(modifier: Modifier = Modifier, cardData: CardData, userid: String, game: String,
                                subcollections: Array<SubcollectionInfo>) {
    val optionInfo = subcollections.filter( predicate = { subCol ->
        subCol.game == game && (!subCol.isDeck|| cardData.subcollections == null || cardData.subcollections!!.count {it == subCol.subcollectionid} < mapGameToMaxCopiesInDeck(subCol.game))
    })
    val staticResponseText = stringResource(R.string.card_added_to_subcollection_message)
    val optionList = listOf("") + optionInfo.map { it.name }
    var selectedOption by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableIntStateOf(1) }
    var responseText by remember { mutableStateOf("")}

    Row(modifier = modifier.padding(5.dp),
        verticalAlignment = Alignment.CenterVertically) {
        UserDropdownSelector(
            modifier = Modifier.weight(.7f).padding(end = 10.dp),
            label = stringResource(R.string.subcollection_page),
            data = selectedIndex,
            onUserStorefrontChange = { newIndex ->
                selectedOption = optionInfo[newIndex - 1].subcollectionid
                selectedIndex = newIndex - 1
            },
            options = optionList
        )

        Button(
            modifier = Modifier.weight(.3f),
            onClick = {
                saveToSubcollectionPost(
                    card = cardData,
                    userid = userid,
                    subcollection = selectedOption,
                    subcolInfo = optionInfo[selectedIndex],
                    refreshUI = { }
                )
                responseText = staticResponseText
            },
            enabled = selectedOption != "",
            colors = ButtonColors(
                containerColor = colorResource(R.color.lightGreen),
                contentColor = Color.Black,
                disabledContainerColor = colorResource(R.color.gray),
                disabledContentColor = Color.Black
            )
        ) {
            Text(
                text = stringResource(R.string.add_to_subcollection_button_label),
                style = appTypography.labelLarge
            )
        }
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
    val scrollState = rememberScrollState()
    val deckMap = mutableMapOf<String, Int>()
    var searchTerm by remember { mutableStateOf("") }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    if (subcolInfo.isDeck) {
        allCards.forEach {
            card ->
            var numInSubCol = 0
            if (card.subcollections != null) {
                numInSubCol = card.subcollections!!.count { it == subcollection}
            }
            if (card.game == game && numInSubCol > 0) {
                deckMap[card.cardname] = deckMap.getOrDefault(card.cardname, 0) + numInSubCol
            }
        }
    }

    var cardList = allCards.filter {
        if (subcolInfo.isDeck) {
            if (deckMap.containsKey(it.cardname)) {
                it.game == game && deckMap[it.cardname]!! < mapGameToMaxCopiesInDeck(game) && (if (searchTerm != "") { it.cardname.uppercase().contains(searchTerm.uppercase()) } else { true })
            }
            else {
                it.game == game && (if (searchTerm != "") { it.cardname.uppercase().contains(searchTerm.uppercase()) } else { true })
            }
        }
        else {
            it.game == game && (if (searchTerm != "") { it.cardname.uppercase().contains(searchTerm.uppercase()) } else { true })
        }
    }
    cardList = cardList.sortedBy { it.cardname }
    val checkedStates = remember { mutableStateListOf<Boolean>() }
    repeat(cardList.size) {
        checkedStates.add(false)
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .width(300.dp) // or adjust as needed
            .height(screenHeight * 0.8f) // 80% of screen height
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Search Bar
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                value = searchTerm,
                onValueChange = { searchTerm = it },
                singleLine = true,
                label = {
                    Text(
                        text = stringResource(R.string.search_label),
                        style = appTypography.labelMedium
                    )
                }
            )

            // Scrollable List
            Column(
                modifier = Modifier
                    .weight(1f) // takes up all space between search and button
                    .verticalScroll(scrollState)
                    .padding(horizontal = 8.dp)
            ) {
                cardList.forEachIndexed { index, card ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(width = 1.dp, color = Color.Black)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = card.cardname + " " + mapRarityToShortenedVersion(card.rarity!!),
                            style = appTypography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = checkedStates[index],
                            onCheckedChange = { isChecked ->
                                checkedStates[index] = isChecked
                            }
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    cardList.forEachIndexed { index, card ->
                        if (checkedStates[index]) {
                            saveToSubcollectionPost(card = card, userid = userid, subcollection = subcollection, subcolInfo = subcolInfo, refreshUI = refreshUI)
                        }
                    }
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                enabled = checkedStates.any { it },
                colors = ButtonColors(
                    containerColor = colorResource(R.color.buttonLightBlue),
                    contentColor = Color.Black,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.Black)
            ) {
                Text(
                    text = stringResource(R.string.add_to_subcollection_button_label),
                    style = appTypography.labelLarge
                )
            }
        }
    }
}

@Composable
fun RemoveFromCollectionPopup(modifier: Modifier, cardData: CardData, onDismissRequest: () -> Unit, allCardsFlag: Boolean,
                              subcolInfo: SubcollectionInfo, userid: String, fullCardPool: Array<CardData>,
                              onCollectionChange: (Array<CardData>) -> Unit, removeCard: (CardData) -> Unit, refreshUI: () -> Unit,
                              showCardPopup: () -> Unit) {
    AlertDialog(modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.remove_card_title),
                style = appTypography.headlineLarge,
                textAlign = TextAlign.Center
            )
        },
        text = {
            if (allCardsFlag) {
                Text(
                    text = stringResource(R.string.remove_card_all_cards_text),
                    style = appTypography.labelMedium
                )
            }
            else {
                Text(
                    text = String.format(stringResource(R.string.remove_card_subcollection_text), subcolInfo.name),
                    style = appTypography.labelMedium)
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
                Text(
                    text = stringResource(R.string.yes_label),
                    style = appTypography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(R.string.no_label),
                    style = appTypography.labelLarge
                )
            }
        }
    )
}

@Composable
fun CardPriceComponent(modifier: Modifier = Modifier,
                       cardData: CardData, navWebsite: (String) -> Unit) {
    Box(modifier = modifier
        .fillMaxWidth()
        .padding(end = 5.dp)
        .clickable(onClick = {
            if (cardData.purchaseurl != null) {
                navWebsite(cardData.purchaseurl!!)
            }
        })
        .border(width = 1.dp, color = Color.Black),
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp, horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = String.format(
                    stringResource(R.string.card_price_label),
                    cardData.price,
                    "$"
                ),
                style = appTypography.labelMedium
            )
            Image(modifier = Modifier.size(24.dp, 24.dp),
                painter = painterResource(R.drawable.tcg_player_icon),
                contentDescription = stringResource(R.string.tcgplayer_label),
            )
        }
    }
}

@Composable
fun ChangeQuantityCardComponent(modifier: Modifier = Modifier, minusOnClick: () -> Unit,
                                plusOnClick: () -> Unit, subColQuant: String,
                                label: String) {
    Row(modifier = modifier.padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            style = appTypography.labelSmall
        )
        IconButton(
            onClick = minusOnClick,
            modifier = Modifier
                .size(36.dp)
                .padding(5.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(5.dp)
        ) {
            Icon(painter = painterResource(R.drawable.minus_icon),
                contentDescription = stringResource(R.string.minus_label))
        }
        Text(
            text = subColQuant,
            style = appTypography.labelLarge
        )
        IconButton(
            onClick = plusOnClick,
            modifier = Modifier
                .size(36.dp)
                .padding(5.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(5.dp)
        ) {
            Icon(painter = painterResource(R.drawable.plus_icon),
                contentDescription = stringResource(R.string.plus_label))
        }
    }
}

@Composable
fun ChangeQuantityCardComponent(modifier: Modifier = Modifier,
                                label: String, cardData: CardData,
                                userid: String, fullCardPool: Array<CardData>,
                                onCollectionChange: (Array<CardData>) -> Unit,
                                removeCard: (CardData) -> Unit,
                                subcolInfo: SubcollectionInfo,
                                refreshUI: () -> Unit,
                                showCardDeletePopup: () -> Unit) {
    var cardQuantity by remember { mutableStateOf(cardData.quantity.toString())}
    var cardQuantErr by remember { mutableStateOf(false) }
    Row(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        Text(
            text = "$label: ",
            style = appTypography.labelLarge
        )
        IconButton(
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
            },
            modifier = Modifier
                .size(36.dp)
                .padding(5.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(5.dp)
        ) {
            Icon(painter = painterResource(R.drawable.minus_icon),
                contentDescription = stringResource(R.string.minus_label))
        }

        FilterTextfield(
            modifier = Modifier.fillMaxWidth(.2f),
            label = null,
            value = cardQuantity,
            onValueChange = { currQuantity: String ->
                if (currQuantity != "" && currQuantity.toInt() > 0) {
                    cardQuantErr = false
                    val quantityChange = (currQuantity.toInt() - cardData.quantity)
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
                cardQuantity = currQuantity
            },
            isError = cardQuantErr,
            textStyle = appTypography.labelLarge
        )

        IconButton(
            onClick = {
                increaseQuantityPost(
                    userid = userid,
                    card = cardData,
                    quantityChange = 1
                )
                cardQuantity = (cardQuantity.toInt() + 1).toString()
            },
            modifier = Modifier
                .size(36.dp)
                .padding(5.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(5.dp)
        ) {
            Icon(painter = painterResource(R.drawable.plus_icon),
                contentDescription = stringResource(R.string.plus_label))
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
        Text(
            text = label,
            style = appTypography.labelLarge
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterTextfield(
                modifier = Modifier
                    .weight(.3f)
                    .padding(horizontal = 5.dp),
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
                modifier = Modifier
                    .weight(.11f)
                    .height(30.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(.5f))
                Text(
                    modifier = Modifier.weight(.5f),
                    text = "-",
                    style = appTypography.labelSmall
                )
            }

            FilterTextfield(
                modifier = Modifier
                    .weight(.3f)
                    .padding(horizontal = 5.dp),
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
        Text(
            text = label,
            style = appTypography.labelLarge
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterTextfield(
                modifier = Modifier
                    .weight(.3f)
                    .padding(horizontal = 5.dp),
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
                modifier = Modifier
                    .weight(.11f)
                    .height(30.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(.5f))
                Text(
                    modifier = Modifier.weight(.5f),
                    text = "-",
                    style = appTypography.labelSmall
                )
            }

            FilterTextfield(
                modifier = Modifier
                    .weight(.3f)
                    .padding(horizontal = 5.dp),
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
fun DropdownSelectorFilter(label: String? = null, data: Int,
                           options: List<String>,
                           onSelectedValChange: (String, Int) -> Unit,
                           modifier: Modifier = Modifier,
                           recalculateFilter: () -> Unit) {
    var mExpanded by remember { mutableStateOf(false) }
    var mSelectedText by remember { mutableStateOf(options[data - 1]) }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.darkGray)),
        shape = RoundedCornerShape(corner = CornerSize(0.dp)),
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .requiredHeight(70.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (label != null) {
                Text(
                    text = label,
                    style = appTypography.labelLarge,
                    textAlign = TextAlign.Left
                )
            }
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
                        Icon(icon, stringResource(R.string.edit_option_label),
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
                                onSelectedValChange(optionLabel, index)
                                recalculateFilter()
                                mExpanded = false
                            },
                            text = {
                                Text(text = optionLabel,
                                    style = appTypography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AutoResizeText(
    text: String,
    fontSizeRange: FontSizeRange,
    modifier: Modifier = Modifier,
    style: TextStyle = appTypography.bodyMedium,
    color: Color = Color.Unspecified,
) {
    var textSize by remember { mutableStateOf<TextUnit?>(null) }
    var parentSize by remember { mutableStateOf(IntSize.Zero) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .onSizeChanged { parentSize = it }
            .background(Color.LightGray)
            .verticalScroll(scrollState)
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
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

fun removeFromCollectionPost(card: CardData, userid: String, game: String, quantity: Int, fullCardPool: Array<CardData>,
                             onCollectionChange: (Array<CardData>) -> Unit, removeCard: (CardData) -> Unit,
                             subcolInfo: SubcollectionInfo, refreshUI: () -> Unit): Boolean {
    val retrofit = Retrofit.Builder()
        .baseUrl(api_url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitAPI = retrofit.create(ApiService::class.java)
    val requestData = cardToAddRemoveCard(card, userid, quantity)
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
                card.quantity -= quantity
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

    val requestData = cardToAddRemoveCard(card, userid, quantityChange)

    retrofitAPI.addToCollection(requestData).enqueue(object:
        Callback<GenericSuccessErrorResponseModel> {
        override fun onResponse(
            call: Call<GenericSuccessErrorResponseModel>,
            response: Response<GenericSuccessErrorResponseModel>
        ) {
            val respData = response.body()
            if (respData != null) {
                Log.i("IncreaseQuantityPost", "Do Nothing")
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
    val requestData = cardToAddRemoveCard(card, userid, card.quantity, subcollection = subcolInfo.subcollectionid)
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

fun recalculateFilterList(type: String,
                          quantityMin: String,
                          levelMin: String,
                          quantityMax: String,
                          levelMax: String,
                          priceMin: String,
                          priceMax: String,
                          attribute: String,
                          rarity: String,
                          frame: String,
                          legality: Int): MutableList<(CardData) -> Boolean> {
    val ret = mutableListOf<(CardData) -> Boolean>()
    if (type != "") {
        ret.add { it.type == type }
    }
    if (attribute != "") {
        ret.add { (it.attribute != null && it.attribute == attribute) || (it.color != null && it.color.contains(attribute)) }
    }
    if (rarity != "") {
        ret.add { it.rarity != null && it.rarity == rarity }
    }
    if(frame != "") {
        ret.add { it.frameType != null && it.frameType == frame}
    }
    if (legality > 1) {
        when(legality) {
            2 -> ret.add {it.legalities != null && it.legalities.standard == "legal"}
            3 -> ret.add {it.legalities != null &&it.legalities.legacy == "legal"}
            4 -> ret.add {it.legalities != null &&it.legalities.pioneer == "legal"}
            5 -> ret.add {it.legalities != null &&it.legalities.pauper == "legal"}
            6 -> ret.add {it.legalities != null &&it.legalities.modern == "legal"}
            7 -> ret.add {it.legalities != null &&it.legalities.commander == "legal"}
            8 -> ret.add {it.legalities != null &&it.legalities.vintage == "legal"}
        }
    }
    if (quantityMin != "" && (quantityMax == "" || quantityMin.toInt() < quantityMax.toInt())) {
        ret.add { it.quantity >= quantityMin.toInt() }
    }
    if (quantityMax != "" && (quantityMin == "" || quantityMax.toInt() > quantityMin.toInt())) {
        ret.add { it.quantity <= quantityMax.toInt() }
    }
    if (levelMin != "" && (levelMax == "" || levelMin.toInt() < levelMax.toInt())) {
        ret.add { (it.level?.toInt() ?: -1) >= levelMin.toInt() }
    }
    if (levelMax != "" && (levelMin == "" || levelMin.toInt() < levelMax.toInt())) {
        ret.add { (it.level?.toInt() ?: Int.MAX_VALUE) <= levelMax.toInt() }
    }
    if (priceMin != "" && (priceMax == "" || priceMin.toDouble() < priceMax.toDouble())) {
        ret.add { (it.price) >= priceMin.toDouble() }
    }
    if (priceMax != "" && (priceMin == "" || priceMin.toDouble() < priceMax.toDouble())) {
        ret.add { (it.price) <= priceMax.toDouble() }
    }
    return ret
}

fun sortSubcollection(subCol: MutableList<CardData> , game: String): MutableList<CardData> {
    if (game == "yugioh") {
        return subCol.sortedWith( compareBy<CardData> {
            yugiohSort(it.frameType!!)
        }.thenBy { it.cardname }).toMutableList()
    }
    return subCol.sortedWith( compareBy<CardData> {
        it.color!![0]
    }.thenBy { it.cardname }).toMutableList()
}

fun yugiohSort(frameType: String): Int {
    return when (frameType) {
        "spell" -> 2
        "trap" -> 3
        else -> 1
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

