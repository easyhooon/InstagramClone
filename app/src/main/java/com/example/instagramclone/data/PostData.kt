package com.example.instagramclone.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// 파이어스토어를 백엔드로 이용할 경우 서로 다른 테이블을 조인하는게 상당히 까다로운 작업이므로
// 편의상 유저의 데이터도 포스트에 포함시킴
// 따라서 유저의 정보(프로필 사진)이 변경된다면 모든 포스트에 대한 변경 작업을 해주어야 함
// 다른 관계형 데이터베이스를 사용할 경우 조인을 통해 해당 문제를 해결할 수 있기 때문에 고려해주지 않아도 된다.
@Parcelize
data class PostData(
    val postId: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val userImage: String? = null,
    val postImage: String? = null,
    val postDescription: String? = null,
    val time: Long? = null,
    var likes : List<String>? = null, // list of userId
    val searchTerms: List<String>? = null
) : Parcelable
