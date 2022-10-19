package com.example.instagramclone.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.instagramclone.SharedViewModel

@Composable
fun NotificationMessage(viewModel: SharedViewModel) {
    val notificationState = viewModel.popupNotification.value
    val notificationMessage = notificationState?.getContentOrNull()
    if (notificationMessage != null) {
        Toast.makeText(LocalContext.current, notificationMessage, Toast.LENGTH_LONG).show()
    }
}