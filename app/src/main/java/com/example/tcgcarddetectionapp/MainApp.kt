package com.example.tcgcarddetectionapp

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tcgcarddetectionapp.models.CardData
import com.example.tcgcarddetectionapp.models.SubcollectionInfo
import kotlinx.coroutines.*

enum class CardDetectionScreens(@StringRes val title: Int) {
    Login(title = R.string.app_name),
    YugiohCollection(title = R.string.yugioh_collection),
    MagicCollection(title = R.string.mtg_collection),
    PokemonCollection(title = R.string.pokemon_collection),
    Profile(title = R.string.profile_page),
    Scan(title = R.string.scan_page),
    NewUser(title = R.string.new_user_page),
    Subcollection(title = R.string.subcollection_page),
}

@Composable
fun CardDetectionBottomBar(
    navigateScan: () -> Unit,
    navigateYugioh: () -> Unit,
    navigateMTG: () -> Unit,
    navigatePokemon: () -> Unit,
    navigateProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(modifier = modifier.fillMaxWidth(),
        actions = {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                IconButton(onClick = { navigateScan() }) {
                    Icon(Icons.Filled.AddCircle, contentDescription = stringResource(R.string.scan_page))
                }
                IconButton(onClick = { navigateYugioh() }) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = stringResource(R.string.yugioh_collection))
                }
                IconButton(onClick = { navigateMTG() }) {
                    Icon(Icons.Filled.Create, contentDescription = stringResource(R.string.mtg_collection))
                }
                IconButton(onClick = { navigatePokemon() }) {
                    Icon(Icons.Filled.Face, contentDescription = stringResource(R.string.pokemon_collection))
                }
                IconButton(onClick = { navigateProfile() }) {
                    Icon(Icons.Filled.Person, contentDescription = stringResource(R.string.profile_page))
                }
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainApp(navController: NavHostController = rememberNavController()) {
    var username by remember { mutableStateOf("") }
    var userid by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var collection by remember { mutableStateOf(arrayOf<CardData>()) }
    var subColInfo by remember { mutableStateOf(arrayOf<SubcollectionInfo>()) }
    var lockdownEmail by remember { mutableStateOf(false)}

    val credentialManager = CredentialManager.create(LocalContext.current)

    NavHost(
        navController = navController,
        startDestination = CardDetectionScreens.Login.name,
    ) {
        composable(route = CardDetectionScreens.Login.name) {
            LoginScreen(
                onLoginNavigate = { navController.navigate(CardDetectionScreens.YugiohCollection.name) },
                onNewUserNavigate = {navController.navigate((CardDetectionScreens.NewUser.name))},
                username = username,
                userid = userid,
                credentialManager = credentialManager,
                onUsernameChange = { username = it },
                onUserIdChange = { userid = it },
                onUserEmailChange = { email = it },
                onUserCollectionChange = {collection = it},
                onUserSubColInfoChange = { onUserSubColInfoChange(subColInfo = it, cardDataCollection = collection, setSubColInfo = {subColInfo = it})},
                onLockdownEmailChange = {lockdownEmail = it}
            )
        }
        composable(route = CardDetectionScreens.YugiohCollection.name) {
            Scaffold(modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    CardDetectionBottomBar(
                        navigateScan = { navController.navigate(CardDetectionScreens.Scan.name) },
                        navigateYugioh = { navController.navigate(CardDetectionScreens.YugiohCollection.name) },
                        navigateMTG = { navController.navigate(CardDetectionScreens.MagicCollection.name) },
                        navigatePokemon = { navController.navigate(CardDetectionScreens.PokemonCollection.name) },
                        navigateProfile = { navController.navigate(CardDetectionScreens.Profile.name) },
                    )
                }
            ) {
                var cardCount = 0
                var cardPriceTotal = 0.0
                collection.forEach {
                    card ->
                    if (card.game == "yugioh") {
                        cardCount += card.quantity
                        cardPriceTotal += (card.quantity * card.price)
                    }
                }
                CollectionScreen(
                    gameName = "Yu-Gi-Oh!",
                    subcollections = subColInfo,
                    gameFilter = "yugioh",
                    navController = navController,
                    totalCardCount = cardCount,
                    totalCardValue = cardPriceTotal,
                    userid = userid,
                    fullCardPool = collection,
                    email = email,
                    onUserSubColInfoChange = { onUserSubColInfoChange(subColInfo = it, cardDataCollection = collection, setSubColInfo = {subColInfo = it}) },
                    removeSubcollection = {
                        val removedSubcol = it
                        subColInfo = subColInfo.filter { subcol -> subcol != removedSubcol }.toTypedArray()
                    },
                )
            }
        }
        composable(route = CardDetectionScreens.MagicCollection.name) {
            Scaffold(
                bottomBar = {
                    CardDetectionBottomBar(
                        navigateScan = { navController.navigate(CardDetectionScreens.Scan.name) },
                        navigateYugioh = { navController.navigate(CardDetectionScreens.YugiohCollection.name) },
                        navigateMTG = { navController.navigate(CardDetectionScreens.MagicCollection.name) },
                        navigatePokemon = { navController.navigate(CardDetectionScreens.PokemonCollection.name) },
                        navigateProfile = { navController.navigate(CardDetectionScreens.Profile.name) },
                    )
                }
            ) {
                var cardCount = 0
                var cardPriceTotal = 0.0
                collection.forEach {
                        card ->
                    if (card.game == "mtg") {
                        cardCount += card.quantity
                        cardPriceTotal += (card.quantity * card.price)
                    }
                }

                CollectionScreen(
                    gameName = "Magic",
                    subcollections = subColInfo,
                    gameFilter = "mtg",
                    navController = navController,
                    totalCardCount = cardCount,
                    totalCardValue = cardPriceTotal,
                    userid = userid,
                    fullCardPool = collection,
                    email = email,
                    onUserSubColInfoChange = { onUserSubColInfoChange(subColInfo = it, cardDataCollection = collection, setSubColInfo = {subColInfo = it}) },
                    removeSubcollection = {
                        val removedSubcol = it
                        subColInfo = subColInfo.filter { subcol -> subcol != removedSubcol }.toTypedArray()
                    },
                )
            }
        }
        composable(route = CardDetectionScreens.PokemonCollection.name) {
            Scaffold(
                bottomBar = {
                    CardDetectionBottomBar(
                        navigateScan = { navController.navigate(CardDetectionScreens.Scan.name) },
                        navigateYugioh = { navController.navigate(CardDetectionScreens.YugiohCollection.name) },
                        navigateMTG = { navController.navigate(CardDetectionScreens.MagicCollection.name) },
                        navigatePokemon = { navController.navigate(CardDetectionScreens.PokemonCollection.name) },
                        navigateProfile = { navController.navigate(CardDetectionScreens.Profile.name) },
                    )
                }
            ) {
                var cardCount = 0
                var cardPriceTotal = 0.0
                collection.forEach {
                        card ->
                    if (card.game == "pokemon") {
                        cardCount += card.quantity
                        cardPriceTotal += (card.quantity * card.price)
                    }
                }

                CollectionScreen(
                    gameName = "Pokemon",
                    subcollections = subColInfo,
                    gameFilter = "pokemon",
                    navController = navController,
                    totalCardCount = cardCount,
                    totalCardValue = cardPriceTotal,
                    userid = userid,
                    fullCardPool = collection,
                    email = email,
                    onUserSubColInfoChange = { onUserSubColInfoChange(subColInfo = it, cardDataCollection = collection, setSubColInfo = {subColInfo = it}) },
                    removeSubcollection = {
                        val removedSubcol = it
                        subColInfo = subColInfo.filter { subcol -> subcol != removedSubcol }.toTypedArray()
                    },
                )
            }
        }
        composable(route = CardDetectionScreens.Profile.name) {
            Scaffold(
                bottomBar = {
                    CardDetectionBottomBar(
                        navigateScan = { navController.navigate(CardDetectionScreens.Scan.name) },
                        navigateYugioh = { navController.navigate(CardDetectionScreens.YugiohCollection.name) },
                        navigateMTG = { navController.navigate(CardDetectionScreens.MagicCollection.name) },
                        navigatePokemon = { navController.navigate(CardDetectionScreens.PokemonCollection.name) },
                        navigateProfile = { navController.navigate(CardDetectionScreens.Profile.name) },
                    )
                }
            ) {
                ProfileScreen(
                    username = username,
                    email = email,
                    onUsernameChange = { username = it },
                    onUserEmailChange = { email = it },
                    userid = userid,
                    lockdownEmail = lockdownEmail,
                    onLogout = {
                        username = ""
                        userid = ""
                        email = ""
                        lockdownEmail = false
                        collection = arrayOf<CardData>()
                        subColInfo = arrayOf<SubcollectionInfo>()
                        runBlocking {
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                        }
                        navController.navigate(CardDetectionScreens.Login.name)
                    }
                )
            }
        }
        composable(route = CardDetectionScreens.Scan.name) {
            Scaffold(
                bottomBar = {
                    CardDetectionBottomBar(
                        navigateScan = { navController.navigate(CardDetectionScreens.Scan.name)},
                        navigateYugioh = { navController.navigate(CardDetectionScreens.YugiohCollection.name) },
                        navigateMTG = { navController.navigate(CardDetectionScreens.MagicCollection.name) },
                        navigatePokemon = { navController.navigate(CardDetectionScreens.PokemonCollection.name) },
                        navigateProfile = { navController.navigate(CardDetectionScreens.Profile.name) },
                    )
                }
            ) {
                ScanScreen(userid = userid, collectionNavigate = { navController.navigate(CardDetectionScreens.YugiohCollection.name)},
                    addToOverallCards = { card ->
                        var found = false
                        for (existingCard in collection) {
                            if (existingCard.equals(card)) {
                                existingCard.quantity += card.quantity
                                found = true
                                break
                            }
                        }
                        if (!found) collection = collection.plus(card)
                })
            }
        }

        composable(route = CardDetectionScreens.NewUser.name) {
            NewUserRegistrationScreen(
                username = username,
                email = email,
                onUsernameChange = { username = it },
                onUserEmailChange = { email = it },
                onUseridChange = { userid = it },
                onLoginNavigate = { navController.navigate(CardDetectionScreens.YugiohCollection.name) },
                onBackNavigate = { navController.navigate((CardDetectionScreens.Login.name))},
            )
        }

        composable(route = CardDetectionScreens.Subcollection.name + "/{subColId}/{game}") {
            navBackStackEntry ->
            val subColId = navBackStackEntry.arguments?.getString("subColId")
            val game = navBackStackEntry.arguments?.getString("game")
            var thisSubCol: SubcollectionInfo? = null
            var allCardsFlag = false

            if (subColId == "all") {
                thisSubCol = SubcollectionInfo(
                    subcollectionid = "all",
                    name = stringResource(R.string.all_cards_subcol_label),
                    totalValue = 0.0,
                    physLoc = "",
                    cardCount = 0,
                    game = game!!,
                    isDeck = false,
                    userid = userid
                )
                allCardsFlag = true
                collection.forEach {
                        card ->
                    if (card.game == game) {
                        thisSubCol!!.totalValue = thisSubCol!!.totalValue?.plus((card.quantity * card.price))
                        thisSubCol!!.cardCount = thisSubCol!!.cardCount?.plus(card.quantity)
                    }
                }
            }
            else {
                subColInfo.forEach {
                        subCol ->
                    if (subCol.subcollectionid == subColId) {
                        thisSubCol = subCol
                    }
                }
            }
            var navBack = {}
            if (game == "yugioh") {
                navBack = { navController.navigate(CardDetectionScreens.YugiohCollection.name) }
            }
            else if (game == "mtg") {
                navBack = { navController.navigate(CardDetectionScreens.MagicCollection.name) }
            }
            else if (game == "pokemon") {
                navBack = { navController.navigate(CardDetectionScreens.PokemonCollection.name) }
            }

            Scaffold(
                bottomBar = {
                    CardDetectionBottomBar(
                        navigateScan = { navController.navigate(CardDetectionScreens.Scan.name)},
                        navigateYugioh = { navController.navigate(CardDetectionScreens.YugiohCollection.name) },
                        navigateMTG = { navController.navigate(CardDetectionScreens.MagicCollection.name) },
                        navigatePokemon = { navController.navigate(CardDetectionScreens.PokemonCollection.name) },
                        navigateProfile = { navController.navigate(CardDetectionScreens.Profile.name) },
                    )
                }
            ) {
                SubcollectionScreen(
                    subcolInfo = thisSubCol ?: SubcollectionInfo(
                        subcollectionid = "Error",
                        name = "Error",
                        totalValue = 0.0,
                        physLoc = "error",
                        cardCount = 0,
                        game = "error",
                        isDeck = false,
                        userid = "error"
                    ),
                    allCardsFlag = allCardsFlag,
                    fullCardPool = collection,
                    subcollections = subColInfo,
                    game = game!!,
                    userid = userid,
                    navBack = navBack,
                    onCollectionChange = {collection = it},
                    removeSubcollection = {
                        val removedSubcol = it
                        subColInfo = subColInfo.filter { subcol -> subcol != removedSubcol }.toTypedArray()
                    },
                )
            }
        }
    }

}

fun onUserSubColInfoChange(subColInfo: Array<SubcollectionInfo>, cardDataCollection: Array<CardData>, setSubColInfo: (Array<SubcollectionInfo>) -> Unit) {
    cardDataCollection.forEach {
            card ->
        if (card.subcollections != null) {
            subColInfo.forEach {
                    subCol ->
                if (subCol.subcollectionid in card.subcollections!!) {
                    val amt = card.subcollections!!.count { it == subCol.subcollectionid}
                    if (subCol.cardCount == null) {
                        subCol.cardCount = amt
                    }
                    else {
                        subCol.cardCount = subCol.cardCount!! + amt
                    }
                    if (subCol.totalValue == null) {
                        subCol.totalValue = (card.price * amt)
                    }
                    else {
                        subCol.totalValue = subCol.totalValue!! + (card.price * amt)
                    }
                }
            }
        }
    }
    setSubColInfo(subColInfo)
}
