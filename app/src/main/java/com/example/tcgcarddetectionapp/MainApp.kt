package com.example.tcgcarddetectionapp

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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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

@Composable
fun MainApp(navController: NavHostController = rememberNavController()) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CardDetectionScreens.Login.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = CardDetectionScreens.Login.name) {
                LoginScreen(onLoginClick = {navController.navigate(CardDetectionScreens.YugiohCollection.name)})
            }
            composable(route = CardDetectionScreens.YugiohCollection.name) {
                CollectionScreen(gameName = "YuGiOh")
            }
            composable(route = CardDetectionScreens.MagicCollection.name) {
                CollectionScreen(gameName = "Magic")
            }
            composable(route = CardDetectionScreens.PokemonCollection.name) {
                CollectionScreen(gameName = "Pokemon")
            }
            composable(route = CardDetectionScreens.Profile.name) {
                ProfileScreen(username = "TestUser", email = "TestUser@void.com")
            }
        }
    }
}