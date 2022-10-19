package com.example.instagramclone.ui.main

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.instagramclone.R
import com.example.instagramclone.SharedViewModel
import com.example.instagramclone.components.CommonDivider
import com.example.instagramclone.components.CommonProgressSpinner


@Composable
fun NewPostScreen(navController: NavController, viewModel: SharedViewModel, encodingUri: String) {

    val imageUri by remember { mutableStateOf(encodingUri) }
    var description by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    //focus 도 context 처럼 받아올 수 current 의 형식으로 받아올 수 있다.
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            ///.fillMaxWidth(),
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.cancel),
                modifier = Modifier.clickable { navController.popBackStack() })
            Text(text = stringResource(R.string.post), modifier = Modifier.clickable {
                focusManager.clearFocus()
                viewModel.onNewPost(
                    Uri.parse(imageUri),
                    description
                ) { navController.popBackStack() }
            })
        }

        //TODO 성능 개선을 위해 다른 방법으로 구현
        CommonDivider()

        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 150.dp)
                .padding(top = 8.dp),
            contentScale = ContentScale.FillWidth
        )

        Row(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                label = { Text(text = stringResource(R.string.description)) },
                singleLine = false,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                )
            )
        }
    }

    val inProgress = viewModel.inProgress.value
    if (inProgress)
        CommonProgressSpinner()
}

//@Preview
//@Composable
//fun NewPostScreenPreview() {
//    NewPostScreen(navController = , viewModel = , encodingUri = )
//}