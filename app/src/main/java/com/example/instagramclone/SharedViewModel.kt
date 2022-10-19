package com.example.instagramclone

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.instagramclone.data.CommentData
import com.example.instagramclone.data.Event
import com.example.instagramclone.data.PostData
import com.example.instagramclone.data.UserData
import com.example.instagramclone.di.StringResourcesProvider
import com.example.instagramclone.util.Constants.COMMENTS
import com.example.instagramclone.util.Constants.FOLLOWING
import com.example.instagramclone.util.Constants.LIKES
import com.example.instagramclone.util.Constants.POSTS
import com.example.instagramclone.util.Constants.POST_ID
import com.example.instagramclone.util.Constants.SEARCH_TERMS
import com.example.instagramclone.util.Constants.TIME
import com.example.instagramclone.util.Constants.USERS
import com.example.instagramclone.util.Constants.USER_ID
import com.example.instagramclone.util.Constants.USER_IMAGE
import com.example.instagramclone.util.Constants.USER_NAME
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage,
    val stringResourcesProvider: StringResourcesProvider
) : ViewModel() {

    val signedIn = mutableStateOf(false)
    val inProgress = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val popupNotification = mutableStateOf<Event<String>?>(null)

    val refreshPostsProgress = mutableStateOf(false)
    val posts = mutableStateOf<List<PostData>>(listOf())

    val searchedPosts = mutableStateOf<List<PostData>>(listOf())
    val searchedPostsProgress = mutableStateOf(false)

    val postsFeed = mutableStateOf<List<PostData>>(listOf())
    val postsFeedProgress = mutableStateOf(false)

    val comments = mutableStateOf<List<CommentData>>(listOf())
    val commentsProgress = mutableStateOf(false)

    val followers = mutableStateOf(0)

    init {
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }
    }

    fun onSignup(username: String, email: String, password: String) {
        if (username.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = stringResourcesProvider.getString(R.string.fill_in_all_fields))
            return
        }

        inProgress.value = true

        db.collection(USERS).whereEqualTo(USER_NAME, username).get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {
                    handleException(customMessage = stringResourcesProvider.getString(R.string.username_already_exists))
                    inProgress.value = false
                } else {
                    auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signedIn.value = true
                                createOrUpdateProfile(username = username)
                            } else {
                                handleException(
                                    task.exception, stringResourcesProvider.getString(R.string.login_failed)
                                )
                            }
                            inProgress.value = false
                        }
                }
            }
            .addOnFailureListener { }
    }

    fun onLogin(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = stringResourcesProvider.getString(R.string.fill_in_all_fields))
            return
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let { uid ->
                        // handleException(customMessage = LOGIN_SUCCESS)
                        getUserData(uid)
                    }
                } else {
                    handleException(
                        task.exception, stringResourcesProvider.getString(R.string.login_failed)
                    )
                    inProgress.value = false
                }
            }
            .addOnFailureListener { exception ->
                handleException(exception, stringResourcesProvider.getString(R.string.login_failed))
                inProgress.value = false
            }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        username: String? = null,
        bio: String? = null,
        imageUrl: String? = null
    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            username = username ?: userData.value?.username,
            bio = bio ?: userData.value?.bio,
            imageUrl = imageUrl ?: userData.value?.imageUrl,
            following = userData.value?.following
        )

        uid?.let { uid ->
            inProgress.value = true
            db.collection(USERS).document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener {
                                this.userData.value = userData
                                inProgress.value = false
                            }
                            .addOnFailureListener { exception ->
                                handleException(
                                    exception, stringResourcesProvider.getString(R.string.cannot_update_user))
                                inProgress.value = false
                            }
                    } else {
                        db.collection(USERS).document(uid).set(userData)
                        getUserData(uid)
                        inProgress.value = false
                    }
                }
                .addOnFailureListener { exception ->
                    handleException(
                        exception, stringResourcesProvider.getString(R.string.cannot_create_user)
                    )
                    inProgress.value = false
                }
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(USERS).document(uid).get()
            .addOnSuccessListener {
                val user = it.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                refreshPosts()
                getPersonalizedFeed()
                getFollowers(user?.userId)
            }
            .addOnFailureListener { exception ->
                handleException(
                    exception, stringResourcesProvider.getString(R.string.cannot_retrieve_user_data)
                )
                inProgress.value = false
            }
    }


    private fun handleException(exception: Exception? = null, customMessage: String = "") {
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else "$customMessage: $errorMsg"
        popupNotification.value = Event(message)
    }

    fun updateProfileData(name: String, username: String, bio: String) {
        createOrUpdateProfile(name, username, bio)
    }

    private fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true

        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccess)
            }
            .addOnFailureListener { exc ->
                handleException(exc)
                inProgress.value = false
            }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
            updatePostUserImageData(it.toString())
        }
    }

    //userImage 를 update 한 경우 user 가 올린 post 에 대해 모든 update 가 필요
    private fun updatePostUserImageData(imageUrl: String) {
        val currentUid = auth.currentUser?.uid
        db.collection(POSTS).whereEqualTo(USER_ID, currentUid).get()
            .addOnSuccessListener {
                val posts = mutableStateOf<List<PostData>>(arrayListOf())
                convertPosts(it, posts)
                val refs = arrayListOf<DocumentReference>()
                for (post in posts.value) {
                    post.postId?.let { id ->
                        refs.add(db.collection(POSTS).document(id))
                    }
                }
                if (refs.isNotEmpty()) {
                    //일괄 '쓰기'
                    db.runBatch { batch ->
                        for (ref in refs) {
                            batch.update(ref, USER_IMAGE, imageUrl)
                        }
                    }
                        .addOnSuccessListener {
                            refreshPosts()
                        }
                }
            }
    }

    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event(stringResourcesProvider.getString(R.string.logged_out))
        searchedPosts.value = listOf()
        postsFeed.value = listOf()
        comments.value = listOf()
    }

    fun onNewPost(uri: Uri, description: String, onPostSuccess: () -> Unit) {
        uploadImage(uri) {
            onCreatePost(it, description, onPostSuccess)
        }
    }

    private fun onCreatePost(imageUrl: Uri, description: String, onPostSuccess: () -> Unit) {
        inProgress.value = true
        val currentUid = auth.currentUser?.uid
        val currentUsername = userData.value?.username
        val currentUserImage = userData.value?.imageUrl

        if (currentUid != null) {

            val postUuid = UUID.randomUUID().toString()

            val fillerWords = listOf("the", "be", "to", "is", "of", "and", "or", "a", "in", "it")
            val searchTerms = description
                .split(" ", ".", ",", "?", "!", "#")
                .map { it.lowercase() }
                .filter { it.isNotEmpty() and !fillerWords.contains(it) }

            val post = PostData(
                postId = postUuid,
                userId = currentUid,
                username = currentUsername,
                userImage = currentUserImage,
                postImage = imageUrl.toString(),
                postDescription = description,
                time = System.currentTimeMillis(),
                likes = listOf<String>(),
                searchTerms = searchTerms
            )

            db.collection(POSTS).document(postUuid).set(post)
                .addOnSuccessListener {
                    popupNotification.value = Event(stringResourcesProvider.getString(R.string.post_create))
                    inProgress.value = false
                    refreshPosts()
                    onPostSuccess.invoke()
                }
                .addOnFailureListener { exception ->
                    handleException(exception, stringResourcesProvider.getString(R.string.post_create_fail))
                    inProgress.value = false
                }

        } else {
            handleException(customMessage = stringResourcesProvider.getString(R.string.error_username_unavailable))
            onLogout()
            inProgress.value = false
        }
    }

    private fun refreshPosts() {
        val currentUid = auth.currentUser?.uid
        if (currentUid != null) {
            refreshPostsProgress.value = true
            db.collection(POSTS).whereEqualTo(USER_ID, currentUid).get()
                .addOnSuccessListener { documents ->
                    convertPosts(documents, posts)
                    refreshPostsProgress.value = false
                }
                .addOnFailureListener { exception ->
                    handleException(exception, stringResourcesProvider.getString(R.string.posts_fetch_faill))
                    refreshPostsProgress.value = false
                }
        } else {
            handleException(customMessage = stringResourcesProvider.getString(R.string.error_username_unavailable))
            onLogout()
        }
    }

    private fun convertPosts(documents: QuerySnapshot, outState: MutableState<List<PostData>>) {
        val newPosts = mutableListOf<PostData>()
        documents.forEach { doc ->
            val post = doc.toObject<PostData>()
            newPosts.add(post)
        }
        val sortedPosts = newPosts.sortedByDescending { it.time }
        outState.value = sortedPosts
    }

    fun searchPosts(searchTerm: String) {
        if (searchTerm.isNotEmpty()) {
            searchedPostsProgress.value = true
            db.collection(POSTS)
                .whereArrayContains(SEARCH_TERMS, searchTerm.trim().lowercase())
                .get()
                .addOnSuccessListener {
                    convertPosts(it, searchedPosts)
                    searchedPostsProgress.value = false
                }
                .addOnFailureListener { exception ->
                    handleException(exception, stringResourcesProvider.getString(R.string.posts_search_fail))
                    searchedPostsProgress.value = false
                }
        }
    }

    fun onFollowClick(userId: String) {
        auth.currentUser?.uid?.let { currentUser ->
            val following = arrayListOf<String>()
            userData.value?.following?.let {
                following.addAll(it)
            }
            if (following.contains(userId)) {
                following.remove(userId)
            } else {
                following.add(userId)
            }
            db.collection(USERS).document(currentUser).update(FOLLOWING, following)
                .addOnSuccessListener {
                    getUserData(currentUser)
                }
        }
    }

    private fun getPersonalizedFeed() {
        val following = userData.value?.following
        if (!following.isNullOrEmpty()) {
            postsFeedProgress.value = true
            db.collection(POSTS).whereIn(USER_ID, following).get()
                .addOnSuccessListener {
                    convertPosts(documents = it, outState = postsFeed)
                    if (postsFeed.value.isEmpty()) {
                        getGeneralFeed()
                    } else {
                        postsFeedProgress.value = false
                    }
                }
                .addOnFailureListener { exception ->
                    handleException(exception, stringResourcesProvider.getString(R.string.personalized_feed_get_fail))
                    postsFeedProgress.value = false
                }
        } else {
            getGeneralFeed()
        }
    }

    private fun getGeneralFeed() {
        postsFeedProgress.value = true
        val currentTime = System.currentTimeMillis()
        val difference = MILLIS_OF_DAY // 1 day in millis

        db.collection(POSTS)
            .whereGreaterThan(TIME, currentTime - difference)
            .get()
            .addOnSuccessListener {
                convertPosts(documents = it, outState = postsFeed)
                postsFeedProgress.value = false
            }
            .addOnFailureListener { exception ->
                handleException(exception, stringResourcesProvider.getString(R.string.feed_get_fail))
                postsFeedProgress.value = false
            }
    }

    fun onLikePost(postData: PostData) {
        auth.currentUser?.uid?.let { userId ->
            postData.likes?.let { likes ->
                val newLikes = arrayListOf<String>()
                if (likes.contains(userId)) {
                    newLikes.addAll(likes.filter { userId != it })
                } else {
                    newLikes.addAll(likes)
                    newLikes.add(userId)
                }
                postData.postId?.let { postId ->
                    db.collection(POSTS).document(postId).update(LIKES, newLikes)
                        .addOnSuccessListener {
                            postData.likes = newLikes
                        }
                        .addOnFailureListener {
                            handleException(it, stringResourcesProvider.getString(R.string.post_like_fail))
                        }
                }
            }
        }
    }

    fun createComment(postId: String, text: String) {
        commentsProgress.value = true
        userData.value?.username?.let { username ->
            // random UUID 를 생성하는 방법
            val commentId = UUID.randomUUID().toString()
            val comment = CommentData(
                commentId = commentId,
                postId = postId,
                username = username,
                text = text,
                timestamp = System.currentTimeMillis()
            )
            db.collection(COMMENTS).document(commentId).set(comment)
                .addOnSuccessListener {
                    // get existing comments
                    getComments(postId)
                }
                .addOnFailureListener { exception ->
                    handleException(exception, stringResourcesProvider.getString(R.string.comment_create_fail))
                    commentsProgress.value = false
                }
        }
    }

    fun getComments(postId: String?) {
        commentsProgress.value = true
        db.collection(COMMENTS).whereEqualTo(POST_ID, postId).get()
            .addOnSuccessListener { documents ->
                val newComments = mutableListOf<CommentData>()
                documents.forEach { document ->
                    val comment = document.toObject<CommentData>()
                    newComments.add(comment)
                }
                val sortedComments = newComments.sortedByDescending { it.timestamp }
                comments.value = sortedComments
                commentsProgress.value = false
            }
            .addOnFailureListener { exception ->
                handleException(exception, stringResourcesProvider.getString(R.string.comment_retrieve_fail))
                commentsProgress.value = false
            }
    }

    private fun getFollowers(uid: String?) {
        db.collection(USERS).whereArrayContains(FOLLOWING, uid ?: "").get()
            .addOnSuccessListener { documents ->
                followers.value = documents.size()
            }
    }

    companion object {
        const val MILLIS_OF_DAY = 24 * 60 * 60 * 1000
    }
}