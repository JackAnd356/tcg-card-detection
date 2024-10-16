package com.example.tcgcarddetectionapp

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
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
    val subCol1 = SubcollectionInfo(
        id = "1",
        name = "Subcollection 1",
        totalValue = 500.12,
        location = "Mega Tin 2013",
        cardCount = 42
    )
    val subCol2 = SubcollectionInfo(
        id = "1",
        name = "Subcollection 2",
        totalValue = 12.13,
        location = "Mega Tin 2013",
        cardCount = 4
    )
    val subCol3 = SubcollectionInfo(
        id = "1",
        name = "Subcollection 3",
        totalValue = 56.0,
        location = "Mega Tin 2013",
        cardCount = 7
    )
    val subCol4 = SubcollectionInfo(
        id = "1",
        name = "Subcollection 4",
        totalValue = 1000.45,
        location = "Binder",
        cardCount = 67
    )
    val colList = listOf(subCol1, subCol2, subCol3, subCol4)
    NavHost(
        navController = navController,
        startDestination = CardDetectionScreens.Login.name,
    ) {
        composable(route = CardDetectionScreens.Login.name) {
            LoginScreen(
                onLoginNavigate = { navController.navigate(CardDetectionScreens.YugiohCollection.name) },
                username = username,
                onUsernameChange = { username = it },
                onUserIdChange = { userid = it },
                onUserEmailChange = { email = it },
                onUserStorefrontChange = { storefront = it },
            )
        }
        composable(route = CardDetectionScreens.YugiohCollection.name) {
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
                CollectionScreen(
                    gameName = "Yu-Gi-Oh!",
                    subcollections = colList,
                )
            }
        }
        composable(route = CardDetectionScreens.MagicCollection.name) {
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
                CollectionScreen(gameName = "Magic",
                    subcollections = colList,)
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
                CollectionScreen(gameName = "Pokemon",
                    subcollections = colList,)
            }
        }
        composable(route = CardDetectionScreens.Profile.name) {
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
                        val requestData = SaveStorefrontRequestModel(userid = userid, storefront = storefront)
                        var saveSuccess = false
                        var message = "Unexpected Error"
                        retrofitAPI.saveUserStorefront(requestData).enqueue(object: Callback<SaveStorefrontResponseModel> {
                            override fun onResponse(
                                call: Call<SaveStorefrontResponseModel>,
                                response: Response<SaveStorefrontResponseModel>
                            ) {

                            }

                            override fun onFailure(call: Call<SaveStorefrontResponseModel>, t: Throwable) {
                                t.printStackTrace()
                            }
                        })},
                )
            }
        }
    }

}