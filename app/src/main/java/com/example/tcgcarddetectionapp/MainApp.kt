package com.example.tcgcarddetectionapp

import android.annotation.SuppressLint
import androidx.annotation.StringRes
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    BottomAppBar(
        actions = {
            IconButton(onClick = { navigateScan() }) {
                Icon(Icons.Filled.AddCircle, contentDescription = "Scan Card")
            }
            IconButton(onClick = { navigateYugioh() }) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = "YuGiOh Collection")
            }
            IconButton(onClick = { navigateMTG() }) {
                Icon(Icons.Filled.Create, contentDescription = "MTG Collection")
            }
            IconButton(onClick = { navigatePokemon() }) {
                Icon(Icons.Filled.Face, contentDescription = "Pokemon Collection")
            }
            IconButton(onClick = { navigateProfile() }) {
                Icon(Icons.Filled.Person, contentDescription = "YuGiOh Collection")
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
    var storefront by remember { mutableIntStateOf(1) }
    var collection by remember { mutableStateOf(arrayOf<CardData>()) }
    var subColInfo by remember { mutableStateOf(arrayOf<SubcollectionInfo>()) }

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
                onUsernameChange = { username = it },
                onUserIdChange = { userid = it },
                onUserEmailChange = { email = it },
                onUserStorefrontChange = { storefront = it },
                onUserCollectionChange = {collection = it},
                onUserSubColInfoChange = { onUserSubColInfoChange(subColInfo = it, cardDataCollection = collection, setSubColInfo = {subColInfo = it})},
            )
        }
        composable(route = CardDetectionScreens.YugiohCollection.name) {
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
                    onUserSubColInfoChange = { onUserSubColInfoChange(subColInfo = it, cardDataCollection = collection, setSubColInfo = {subColInfo = it}) }
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
                    onUserSubColInfoChange = { onUserSubColInfoChange(subColInfo = it, cardDataCollection = collection, setSubColInfo = {subColInfo = it}) }
                )
            }
        }
        composable(route = CardDetectionScreens.PokemonCollection.name) {
            Scaffold(
                bottomBar = {
                    CardDetectionBottomBar(
                        navigateScan = { },
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
                    onUserSubColInfoChange = { onUserSubColInfoChange(subColInfo = it, cardDataCollection = collection, setSubColInfo = {subColInfo = it}) }
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
                    storefront = storefront,
                    onUsernameChange = { username = it },
                    onUserEmailChange = { email = it },
                    onUserStorefrontChange = {
                        storefront = it
                        var url = "http://10.0.2.2:5000/"
                        val retrofit = Retrofit.Builder()
                            .baseUrl(url)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()
                        val retrofitAPI = retrofit.create(ApiService::class.java)
                        val requestData =
                            SaveStorefrontRequestModel(userid = userid, storefront = storefront)
                        var saveSuccess = false
                        var message = "Unexpected Error"
                        retrofitAPI.saveUserStorefront(requestData)
                            .enqueue(object : Callback<GenericSuccessErrorResponseModel> {
                                override fun onResponse(
                                    call: Call<GenericSuccessErrorResponseModel>,
                                    response: Response<GenericSuccessErrorResponseModel>
                                ) {

                                }

                                override fun onFailure(
                                    call: Call<GenericSuccessErrorResponseModel>,
                                    t: Throwable
                                ) {
                                    t.printStackTrace()
                                }
                            })
                    },
                    userid = userid,
                    onLogout = {
                        username = ""
                        userid = ""
                        email = ""
                        storefront = 1
                        collection = arrayOf<CardData>()
                        subColInfo = arrayOf<SubcollectionInfo>()
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
                ScanScreen(userid = userid, collectionNavigate = { navController.navigate(CardDetectionScreens.YugiohCollection.name) })
            }
        }

        composable(route = CardDetectionScreens.NewUser.name) {
            NewUserRegistrationScreen(
                username = username,
                email = email,
                storefront = storefront,
                onUsernameChange = { username = it },
                onUserEmailChange = { email = it },
                onUserStorefrontChange = { storefront = it },
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
            var thisCardPool = mutableListOf<CardData>()
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
                collection.forEach {
                    card ->
                    if (card.game == game) {
                        thisCardPool.add(card)
                        thisSubCol!!.totalValue = thisSubCol!!.totalValue?.plus((card.quantity * card.price))
                        thisSubCol!!.cardCount = thisSubCol!!.cardCount?.plus(card.quantity)
                    }
                }
                allCardsFlag = true
            }
            else {
                subColInfo.forEach {
                        subCol ->
                    if (subCol.subcollectionid == subColId) {
                        thisSubCol = subCol
                    }
                }
                collection.forEach {
                        card ->
                    if (card.subcollections?.contains(subColId) == true) {
                        thisCardPool.add(card)
                    }
                }
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
                    subcolInfo = thisSubCol!!,
                    storefront = storefront,
                    cardData = thisCardPool.toTypedArray(),
                    allCardsFlag = allCardsFlag,
                    fullCardPool = collection,
                    subcollections = subColInfo,
                    game = game!!,
                    userid = userid,
                    navBack = {
                        if (game == "yugioh") {
                            navController.navigate(CardDetectionScreens.YugiohCollection.name)
                        }
                        else if (game == "mtg") {
                            navController.navigate(CardDetectionScreens.MagicCollection.name)
                        }
                        else if (game == "pokemon") {
                            navController.navigate(CardDetectionScreens.PokemonCollection.name)
                        }
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
                    if (subCol.cardCount == null) {
                        subCol.cardCount = card.quantity
                    }
                    else {
                        subCol.cardCount = subCol.cardCount!! + card.quantity
                    }
                    if (subCol.totalValue == null) {
                        subCol.totalValue = (card.price * card.quantity)
                    }
                    else {
                        subCol.totalValue = subCol.totalValue!! + (card.price * card.quantity)
                    }
                }
            }
        }
    }
    setSubColInfo(subColInfo)
}
