package com.example.instagramclone.ui.screen.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.ui.DestinationScreen
import com.example.instagramclone.R
import com.example.instagramclone.ui.SharedViewModel
import com.example.instagramclone.ui.components.CommonDivider
import com.example.instagramclone.ui.components.CommonImage
import com.example.instagramclone.ui.components.CommonProgressSpinner
import com.example.instagramclone.extensions.navigateTo

@Composable
fun ProfileScreen(navController: NavController, viewModel: SharedViewModel) {
    val isLoading = viewModel.inProgress.value
    if (isLoading) {
        CommonProgressSpinner()
    } else {
        val userData = viewModel.userData.value
        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var username by rememberSaveable { mutableStateOf(userData?.username ?: "") }
        var bio by rememberSaveable { mutableStateOf(userData?.bio ?: "") }

        ProfileContent(
            viewModel = viewModel,
            name = name,
            username = username,
            bio = bio,
            onNameChange = { name = it },
            onUsernameChange = { username = it },
            onBioChange = { bio = it },
            onSave = { viewModel.updateProfileData(name, username, bio) },
            onBack = { navigateTo(navController = navController, DestinationScreen.MyPosts) },
            onLogout = {
                viewModel.onLogout()
                navigateTo(navController, DestinationScreen.Login)
            }
        )
    }
}

@Composable
fun ProfileContent(
    viewModel: SharedViewModel,
    name: String,
    username: String,
    bio: String,
    onNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val imageUrl = viewModel.userData.value?.imageUrl

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = stringResource(R.string.back), modifier = Modifier.clickable { onBack.invoke() })
            Text(text = stringResource(R.string.save), modifier = Modifier.clickable { onSave.invoke() })
        }

        CommonDivider()

        //User image
        ProfileImage(imageUrl = imageUrl, viewModel = viewModel)

        //TODO 성능 개선을 위해 다른 방법으로 구현
        CommonDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.name), modifier = Modifier.width(100.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.username), modifier = Modifier.width(100.dp))
            TextField(
                value = username,
                onValueChange = onUsernameChange,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.bio), modifier = Modifier.width(100.dp))
            TextField(
                value = bio,
                onValueChange = onBioChange,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    textColor = Color.Black
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.logout), modifier = Modifier.clickable { onLogout.invoke() })
        }
    }
}

@Composable
fun ProfileImage(imageUrl: String?, viewModel: SharedViewModel) {

    // Compose 에서 image 경로 접근하는 방법
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadProfileImage(uri) }
    }

    //오오 Intrinsic 사용
    Box(
        modifier = Modifier
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable { launcher.launch("image/*") },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                CommonImage(data = imageUrl)
            }
            Text(text = stringResource(R.string.change_profile_picture))
        }

        val isLoading = viewModel.inProgress.value
        if (isLoading)
            CommonProgressSpinner()
    }
}
