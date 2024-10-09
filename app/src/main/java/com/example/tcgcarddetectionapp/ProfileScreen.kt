package com.example.tcgcarddetectionapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.example.tcgcarddetectionapp.ui.theme.TCGCardDetectionAppTheme

@Composable
fun ProfileScreen(username: String, email: String, modifier: Modifier = Modifier) {
    Column(verticalArrangement = Arrangement.Top,
        modifier = modifier) {
        Text(
            text = "Profile Screen",
            fontSize = 50.sp,
            lineHeight = 100.sp,
            textAlign = TextAlign.Center
        )
        UserDataComponent(label = "Username", data = username, modifier = modifier, onClick = {})
        UserDataComponent(label = "Password", data = "******", modifier = modifier, onClick = {})
        UserDropdownSelector(label = "TCG Storefront: ", options = listOf("TCGPlayer", "Card Market"))
        UserDataComponent(label = "Email", data = email, modifier = modifier, onClick = {})
    }
}

@Composable
fun UserDataComponent(label: String, data: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(corner = CornerSize(0.dp)),
        modifier = modifier.fillMaxWidth().height(70.dp).requiredHeight(70.dp)) {
        Row {
            Text(
                text = "$label: $data",
                fontSize = 20.sp,
                lineHeight = 50.sp,
                textAlign = TextAlign.Left
            )
            Spacer(Modifier.weight(1f))
            Button(onClick = {onClick()}, modifier = Modifier
                .size(width = 100.dp, height = 40.dp)
                .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = "Change",
                    fontSize = 15.sp,
                    lineHeight = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun UserDropdownSelector(label: String, options: List<String>, modifier: Modifier = Modifier) {
    var mExpanded by remember { mutableStateOf(false) }
    var mSelectedText by remember { mutableStateOf("") }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(corner = CornerSize(0.dp)),
        modifier = modifier.fillMaxWidth().height(70.dp).requiredHeight(70.dp)) {
        Row {
            Text(
                text = label,
                fontSize = 20.sp,
                lineHeight = 50.sp,
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
                        },
                    trailingIcon = {
                        Icon(icon,"contentDescription",
                            Modifier.clickable { mExpanded = !mExpanded })
                    }
                )
                DropdownMenu(
                    expanded = mExpanded,
                    onDismissRequest = {mExpanded = false}
                ) {
                    options.forEach{ optionLabel ->
                        DropdownMenuItem(
                            onClick = {
                                mSelectedText = optionLabel
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




@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview(modifier: Modifier = Modifier) {
    TCGCardDetectionAppTheme {
        ProfileScreen(username = "TestUser", email = "TestUser@void.com", modifier = modifier)
    }
}