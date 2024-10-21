package com.example.tcgcarddetectionapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme
import java.math.BigDecimal
import kotlin.math.round

@Composable
fun CollectionScreen(gameName: String,
                     subcollections: Array<SubcollectionInfo>,
                     gameFilter: String,
                     modifier: Modifier = Modifier) {
    var searchTerm by remember { mutableStateOf("") }
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
                    .padding(vertical = 20.dp)
            ) {
                Text(
                    text = String.format(
                        stringResource(R.string.all_cards_label),
                        200
                    ),
                    fontSize = 20.sp,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = String.format(
                        stringResource(R.string.total_value_label),
                        100000.0000,
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
            Button(onClick = { },
                colors = ButtonColors(
                    containerColor = colorResource(R.color.lightGreen),
                    contentColor = Color.Black,
                    disabledContainerColor = colorResource(R.color.lightGreen),
                    disabledContentColor = Color.Black),
            ) {
                Text(stringResource(R.string.create_new_collection_label))
            }
            subcollections.forEach { subcollection ->
                if (subcollection.game == gameFilter) {
                    CollectionSummary(
                        name = subcollection.name,
                        cardCount = subcollection.cardCount ?: 0,
                        location = subcollection.physLoc,
                        totalValue = subcollection.totalValue ?: 0.0,
                        modifier = modifier
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionSummary(name: String, cardCount: Int, location: String, totalValue: Double, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .fillMaxWidth(.8f)
            .padding(vertical = 20.dp)
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
            gameFilter = "yugioh"
        )
    }
}