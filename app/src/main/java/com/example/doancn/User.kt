package com.example.doancn

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val profileImage: String = "",
    val online: Boolean = false,
    @ServerTimestamp val lastSeen: Date? = null
)
