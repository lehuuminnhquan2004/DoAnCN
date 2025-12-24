package com.example.doancn

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    var senderId: String = "",
    var receiverId: String = "",
    var message: String = "",
    @ServerTimestamp var timestamp: Date? = null
)