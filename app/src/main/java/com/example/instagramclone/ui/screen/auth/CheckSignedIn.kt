package com.example.instagramclone.ui.screen.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.instagramclone.ui.DestinationScreen
import com.example.instagramclone.ui.SharedViewModel

@Composable
fun CheckSignedIn(navController: NavController, viewModel: SharedViewModel) {
    val alreadyLoggedIn = remember { mutableStateOf(false) }
    val signedIn = viewModel.signedIn.value
    // auto login
    // TODO 로그인 화면이 보였다가 사라지는 문제 해결
    if (signedIn && !alreadyLoggedIn.value) {
        alreadyLoggedIn.value = true
        navController.navigate(DestinationScreen.Feed.route) {
            // remove every backstack
            popUpTo(0)
        }
    }
}