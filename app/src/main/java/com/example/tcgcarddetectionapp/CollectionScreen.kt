package com.example.tcgcarddetectionapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme

@Composable
fun CollectionScreen(gameName: String, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.Top,
        modifier = modifier.wrapContentWidth(Alignment.CenterHorizontally)) {
        Text(
            text = "$gameName Collection Screen",
            fontSize = 100.sp,
            lineHeight = 116.sp,
            textAlign = TextAlign.Center
        )
    }
}


@Preview(showBackground = true)
@Composable
fun CollectionScreenPreview() {
    TCGCardDetectionAppTheme {
        CollectionScreen(gameName = "Yu-Gi-Oh")
    }
}