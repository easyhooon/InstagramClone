package com.example.instagramclone.ui.screen.auth

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
import com.example.instagramclone.ui.DestinationScreen
import com.example.instagramclone.R
import com.example.instagramclone.ui.SharedViewModel
import com.example.instagramclone.ui.components.CommonProgressSpinner
import com.example.instagramclone.extensions.navigateTo

@Composable
fun SignupScreen(navController: NavController, viewModel: SharedViewModel) {

    CheckSignedIn(navController = navController, viewModel = viewModel)

    val focus = LocalFocusManager.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(
                    rememberScrollState()
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // remember 와 rememberSaveable 의 차이점
            // -> configration change 가 일어날때 상태를 유지시키기 위해 rememberSaveable 사용
            // 참고로 remember 는 composition 내에 composable 객체들이 유지될때만 올바르게 동작함
            val usernameState = remember { mutableStateOf(TextFieldValue()) }
            val emailState = remember { mutableStateOf(TextFieldValue()) }
            val passwordState = remember { mutableStateOf(TextFieldValue()) }

            Image(
                painter = painterResource(id = R.drawable.ig_logo),
                contentDescription = null,
                modifier = Modifier
                    .width(250.dp)
                    .padding(top = 16.dp)
                    .padding(0.dp)
            )

            Text(
                text = stringResource(R.string.sign_up),
                modifier = Modifier.padding(8.dp),
                fontSize = 30.sp,
                fontFamily = FontFamily.SansSerif
            )

            OutlinedTextField(
                value = usernameState.value,
                onValueChange = { usernameState.value = it },
                modifier = Modifier.padding(8.dp),
                label = { Text(text = stringResource(R.string.username)) }
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
                    focus.clearFocus(true)
                    viewModel.onSignup(
                        usernameState.value.text,
                        emailState.value.text,
                        passwordState.value.text
                    )
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = stringResource(R.string.upper_sign_up))
            }

            Text(
                text = stringResource(R.string.go_to_login),
                color = Color.Blue,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navigateTo(navController, DestinationScreen.Login)
                    }
            )
        }

        val isLoading = viewModel.inProgress.value
        if (isLoading) {
            CommonProgressSpinner()
        }
    }
}