package com.example.instagramclone.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.instagramclone.ui.DestinationScreen
import com.example.instagramclone.ui.SharedViewModel
import com.example.instagramclone.data.model.PostData
import com.example.instagramclone.extensions.NavParams
import com.example.instagramclone.extensions.navigateTo
import com.example.instagramclone.ui.components.CommonImage
import com.example.instagramclone.ui.components.CommonProgressSpinner
import com.example.instagramclone.ui.components.LikeAnimation
import com.example.instagramclone.ui.components.UserImageCard
import com.example.instagramclone.util.Constants.POST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Feed screen that will display posts from followed users / general posts list if we haven't followed anyone
@Composable
fun FeedScreen(navController: NavController, viewModel: SharedViewModel) {

    val userDataLoading = viewModel.inProgress.value
    val userData = viewModel.userData.value
    val personalizedFeed = viewModel.postsFeed.value
    val personalizedFeedLoading = viewModel.postsFeedProgress.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
        ) {
            UserImageCard(userImage = userData?.imageUrl)
        }
        PostList(
            posts = personalizedFeed,
            modifier = Modifier.weight(1f),
            loading = personalizedFeedLoading or userDataLoading,
            navController = navController,
            viewModel = viewModel,
            currentUserId = userData?.userId ?: ""
        )

        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.FEED,
            navController = navController
        )
    }
}

@Composable
fun PostList(
    posts: List<PostData>,
    modifier: Modifier,
    loading: Boolean,
    navController: NavController,
    viewModel: SharedViewModel,
    currentUserId: String
) {
    Box(modifier = modifier) {
        LazyColumn {
            items(items = posts) {
                Post(post = it, currentUserId = currentUserId, viewModel = viewModel) {
                    navigateTo(
                        navController,
                        DestinationScreen.SinglePost,
                        NavParams(POST, it)
                    )
                }
            }
        }
        if (loading)
            CommonProgressSpinner()
    }
}

@Composable
fun Post(
    post: PostData,
    currentUserId: String,
    viewModel: SharedViewModel,
    onPostClick: () -> Unit
) {
    val likeAnimation = remember { mutableStateOf(false) }
    val dislikeAnimation = remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(corner = CornerSize(4.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // user image
                Card(
                    shape = CircleShape, modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    CommonImage(data = post.userImage, contentScale = ContentScale.Crop)
                }
                // user name
                Text(text = post.username ?: "", modifier = Modifier.padding(4.dp))
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 150.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (post.likes?.contains(currentUserId) == true) {
                                    dislikeAnimation.value = true
                                } else {
                                    likeAnimation.value = true
                                }
                                viewModel.onLikePost(post)
                            },
                            onTap = {
                                onPostClick.invoke()
                            }
                        )
                    }
                CommonImage(
                    data = post.postImage,
                    modifier = modifier,
                    contentScale = ContentScale.FillWidth
                )
                if (likeAnimation.value) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000L)
                        likeAnimation.value = false
                    }
                    LikeAnimation()
                }
                if (dislikeAnimation.value) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000L)
                        likeAnimation.value = false
                    }
                    LikeAnimation(false)
                }
            }
        }
    }
}