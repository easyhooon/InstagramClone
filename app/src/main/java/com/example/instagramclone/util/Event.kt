package com.example.instagramclone.util

// 출력되는 메세지가 오직 한번만 화면에 띄워지도록
// TODO sharedFlow 쓰면 이거 안써도되는데 근데 sharedFlow 를 쓰는 Compose Project 를 아직 접해보지 않음
class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    fun getContentOrNull(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}