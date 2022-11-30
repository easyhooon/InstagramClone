package com.example.instagramclone.ui.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.R
import com.example.instagramclone.DestinationScreen
import com.example.instagramclone.SharedViewModel
import com.example.instagramclone.components.*
import com.example.instagramclone.data.PostData
import com.example.instagramclone.util.Constants.POST

//TODO lazyGrid 가 없었기 때문에 해당 api를 사용하지 않음, 이걸 사용해서 만들어보기
data class PostRow(
    var post1: PostData? = null,
    var post2: PostData? = null,
    var post3: PostData? = null
) {
    fun isFull() = post1 != null && post2 != null && post3 != null
    fun add(post: PostData) {
        if (post1 == null) {
            post1 = post
        } else if (post2 == null) {
            post2 = post
        } else if (post3 == null) {
            post3 = post
        }
    }
}

// My posts screen where I can create new posts and see posts that I have created
@Composable
fun MyPostsScreen(navController: NavController, viewModel: SharedViewModel) {

    val newPostImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            val encoded = Uri.encode(it.toString())
            val route = DestinationScreen.NewPost.createRoute(encoded)
            navController.navigate(route)
        }
    }

    val userData = viewModel.userData.value
    val isLoading = viewModel.inProgress.value

    val postsLoading = viewModel.refreshPostsProgress.value
    val posts = viewModel.posts.value

    val followers = viewModel.followers.value

    Column {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                ProfileImage(userData?.imageUrl) {
                    newPostImageLauncher.launch("image/*")
                }

                Text(
                    text = "${posts.size}\nposts",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "$followers\nfollowers",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${userData?.following?.size ?: 0}\nfollowing",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                val usernameDisplay =
                    if (userData?.username == null) "" else "@${userData.username}"
                Text(text = userData?.name ?: "", fontWeight = FontWeight.Bold)
                Text(text = usernameDisplay)
                Text(userData?.bio ?: "")
            }

            OutlinedButton(
                onClick = { navigateTo(navController, DestinationScreen.Profile) },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )
            ) {
                Text(text = "Edit Profile", color = Color.Black)
            }

            PostList(
                isContextLoading = isLoading,
                postsLoading = postsLoading,
                posts = posts,
                modifier = Modifier
                    .weight(1f)
                    .padding(1.dp)
                    .fillMaxSize(),
            ) { post ->
                //On post click
                navigateTo(
                    navController = navController,
                    DestinationScreen.SinglePost,
                    NavParams(POST, post)
                )
            }
        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )
    }

    if (isLoading)
        CommonProgressSpinner()
}

@Composable
fun ProfileImage(imageUrl: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(top = 16.dp)
            .clickable { onClick.invoke() }
    ) {
        UserImageCard(
            userImage = imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        )

        Card(
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.White),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier
                    .background(Color.Blue)
            )
        }
    }
}

@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostData>,
    modifier: Modifier,
    onPostClick: (PostData) -> Unit
) {
    if (postsLoading) {
        CommonProgressSpinner()
    } else if (posts.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isContextLoading)
                Text(text = stringResource(R.string.no_posts_available))
        }
    } else {
        LazyColumn(modifier = modifier) {

            val rows = arrayListOf<PostRow>()
            var currentRow = PostRow()
            rows.add(currentRow)

            for (post in posts) {
                if (currentRow.isFull()) {
                    currentRow = PostRow()
                    rows.add(currentRow)
                }
                currentRow.add(post = post)
            }

            items(items = rows) { row ->
                PostRow(item = row, onPostClick = onPostClick)
            }
        }
    }
}

@Composable
fun PostRow(item: PostRow, onPostClick: (PostData) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        PostImage(imageUrl = item.post1?.postImage, modifier = Modifier
            .weight(1f)
            .clickable { item.post1?.let { post -> onPostClick(post) } }
        )
        PostImage(imageUrl = item.post2?.postImage, modifier = Modifier
            .weight(1f)
            .clickable { item.post2?.let { post -> onPostClick(post) } }
        )
        PostImage(imageUrl = item.post3?.postImage, modifier = Modifier
            .weight(1f)
            .clickable { item.post3?.let { post -> onPostClick(post) } }
        )
    }
}

@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {
    Box(modifier = modifier) {
        var modifier = Modifier
            .padding(1.dp)
            .fillMaxSize()
        if (imageUrl == null) {
            modifier = modifier.clickable(enabled = false) {}
        }
        CommonImage(data = imageUrl, modifier = modifier, contentScale = ContentScale.Crop)
    }
}