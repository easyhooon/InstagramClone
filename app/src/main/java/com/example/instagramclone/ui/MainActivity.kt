package com.example.instagramclone.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.instagramclone.ui.components.NotificationMessage
import com.example.instagramclone.data.model.PostData
import com.example.instagramclone.ui.screen.auth.LoginScreen
import com.example.instagramclone.ui.screen.auth.ProfileScreen
import com.example.instagramclone.ui.screen.auth.SignupScreen
import com.example.instagramclone.ui.screen.CommentsScreen
import com.example.instagramclone.ui.screen.FeedScreen
import com.example.instagramclone.ui.screen.MyPostsScreen
import com.example.instagramclone.ui.screen.NewPostScreen
import com.example.instagramclone.ui.screen.SearchScreen
import com.example.instagramclone.ui.screen.SinglePostScreen
import com.example.instagramclone.ui.theme.InstagramCloneTheme
import com.example.instagramclone.util.Constants.COMMENTS_POST_ID
import com.example.instagramclone.util.Constants.FEED
import com.example.instagramclone.util.Constants.IMAGE_URI
import com.example.instagramclone.util.Constants.LOGIN
import com.example.instagramclone.util.Constants.MY_POSTS
import com.example.instagramclone.util.Constants.NEW_POST_IMAGE_URI
import com.example.instagramclone.util.Constants.POST
import com.example.instagramclone.util.Constants.POST_ID
import com.example.instagramclone.util.Constants.PROFILE
import com.example.instagramclone.util.Constants.SEARCH
import com.example.instagramclone.util.Constants.SIGN_UP
import com.example.instagramclone.util.Constants.SINGLE_POST
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstagramCloneTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colors.background
                ) {
                    InstagramApp()
                }
            }
        }
    }
}

sealed class DestinationScreen(val route: String) {
    object Signup : DestinationScreen(SIGN_UP)
    object Login : DestinationScreen(LOGIN)
    object Feed : DestinationScreen(FEED)
    object Search : DestinationScreen(SEARCH)
    object MyPosts : DestinationScreen(MY_POSTS)
    object Profile : DestinationScreen(PROFILE)
    object NewPost : DestinationScreen(NEW_POST_IMAGE_URI) {
        fun createRoute(uri: String) = "newpost/$uri"
    }

    object SinglePost : DestinationScreen(SINGLE_POST)
    object CommentsScreen : DestinationScreen(COMMENTS_POST_ID) {
        fun createRoute(postId: String) = "comments/$postId"
    }
}

@Composable
fun InstagramApp() {
    val viewModel = hiltViewModel<SharedViewModel>()
    val navController = rememberNavController()

    NotificationMessage(viewModel = viewModel)

    NavHost(navController = navController, startDestination = DestinationScreen.Signup.route) {
        composable(DestinationScreen.Signup.route) {
            SignupScreen(navController = navController, viewModel = viewModel)
        }

        composable(DestinationScreen.Login.route) {
            LoginScreen(navController = navController, viewModel = viewModel)
        }

        composable(DestinationScreen.Feed.route) {
            FeedScreen(navController = navController, viewModel = viewModel)
        }

        composable(DestinationScreen.Search.route) {
            SearchScreen(navController = navController, viewModel = viewModel)
        }

        composable(DestinationScreen.MyPosts.route) {
            MyPostsScreen(navController = navController, viewModel = viewModel)
        }

        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController = navController, viewModel = viewModel)
        }

        composable(DestinationScreen.NewPost.route) { navBackStackEntry ->
            val imageUri = navBackStackEntry.arguments?.getString(IMAGE_URI)
            imageUri?.let {
                NewPostScreen(navController = navController, viewModel = viewModel, encodingUri = it)
            }
        }

        composable(DestinationScreen.SinglePost.route) {
            val postData = navController
                .previousBackStackEntry
                ?.arguments
                ?.getParcelable<PostData>(POST)
            postData?.let {
                SinglePostScreen(
                    navController = navController,
                    viewModel = viewModel,
                    post = postData
                )
            }
        }

        composable(DestinationScreen.CommentsScreen.route) { navBackStackEntry ->
            val postId = navBackStackEntry.arguments?.getString(POST_ID)
            postId?.let {
                CommentsScreen(
                    navController = navController,
                    viewModel = viewModel,
                    postId = it
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    InstagramCloneTheme {
        InstagramApp()
    }
}