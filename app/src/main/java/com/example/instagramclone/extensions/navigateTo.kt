package com.example.instagramclone.extensions

import android.os.Parcelable
import androidx.navigation.NavController
import com.example.instagramclone.ui.DestinationScreen

data class NavParams(
    val name: String,
    val value: Parcelable
)

fun navigateTo(navController: NavController, destination: DestinationScreen, vararg params: NavParams) {
    for (param in params) {
        navController.currentBackStackEntry?.arguments?.putParcelable(param.name, param.value)
    }

    navController.navigate(destination.route) {
        popUpTo(destination.route)
        launchSingleTop = true
    }
}