package com.example.tcgcarddetectionapp

import androidx.camera.video.internal.compat.quirk.ReportedVideoQualityNotSupportedQuirk
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme

@Composable
fun SubcollectionScreen(subcolInfo: SubcollectionInfo,
                        cardData: Array<CardData>,
                        navBack: () -> Unit,
                        modifier: Modifier = Modifier) {
    var searchTerm by remember { mutableStateOf("") }
    val scrollstate = rememberScrollState()

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
            for (i in 0..(cardData.size - 1) step 2) {
                Row{
                    for (j in i..(i + 1).coerceAtMost(cardData.size - 1)) {
                        CardBase(
                            cardid = cardData[j].cardid,
                            setcode = cardData[j].setcode,
                            price = cardData[j].price,
                            quantity = cardData[j].quantity,
                            modifier = modifier
                                .padding(vertical = 20.dp, horizontal = 15.dp)
                        )
                    }
                }
            }
        }

    }
}

@Composable
fun CardBase(cardid: String,
             setcode: String,
             price: Double,
             quantity: Int,
             modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Column {
            Text(
                text = String.format(
                    stringResource(R.string.card_id_label),
                    cardid
                ),
                fontSize = 15.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = String.format(
                    stringResource(R.string.card_setcode_label),
                    setcode
                ),
                fontSize = 15.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = String.format(
                    stringResource(R.string.card_price_label),
                    price,
                    "$"
                ),
                fontSize = 15.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = String.format(
                    stringResource(R.string.card_quantity_label),
                    quantity
                ),
                fontSize = 15.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }
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
    )

    val cards = arrayOf(card1, card2, card3)
    TCGCardDetectionAppTheme {
        SubcollectionScreen(
            subcolInfo = subCol1,
            cardData = cards,
            navBack = {  },
        )
    }
}