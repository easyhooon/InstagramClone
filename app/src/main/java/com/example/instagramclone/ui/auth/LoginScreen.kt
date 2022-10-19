package com.example.instagramclone.ui.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.R
import com.example.instagramclone.SharedViewModel
import com.example.instagramclone.components.CheckSignedIn
import com.example.instagramclone.components.CommonProgressSpinner
import com.example.instagramclone.components.navigateTo

@Composable
fun LoginScreen(navController: NavController, viewModel: SharedViewModel) {
    
    CheckSignedIn(navController = navController, viewModel = viewModel)

    // to use this focus to dismiss the keyboard
    val focus = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val emailState = remember { mutableStateOf(TextFieldValue()) }
            val passwordState = remember { mutableStateOf(TextFieldValue()) }

            Image(
                painter = painterResource(id = R.drawable.ig_logo),
                contentDescription = null,
                modifier = Modifier
                    .width(250.dp)
                    .padding(top = 16.dp)
                    .padding(8.dp)
            )
            Text(
                text = stringResource(R.string.login),
                modifier = Modifier.padding(8.dp),
                fontSize = 30.sp,
                fontFamily = FontFamily.Serif
            )
            OutlinedTextField(
                value = emailState.value,
                onValueChange = { emailState.value = it },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = stringResource(R.string.email)) }
            )
            OutlinedTextField(
                value = passwordState.value,
                onValueChange = { passwordState.value = it },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = stringResource(R.string.password)) },
                visualTransformation = PasswordVisualTransformation()
            )
            Button(
                onClick = {
                    focus.clearFocus(force = true)
                    Log.d("LoginScreen", "${emailState.value.text} ${passwordState.value.text}")
                    viewModel.onLogin(emailState.value.text, passwordState.value.text)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = stringResource(id = R.string.upper_login))
            }
            Text(
                text = stringResource(R.string.go_to_signup),
                color = Color.Blue,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navigateTo(navController, DestinationScreen.Signup)
                    }
            )
        }
        val isLoading = viewModel.inProgress.value
        if (isLoading) {
            CommonProgressSpinner()
        }
    }
}