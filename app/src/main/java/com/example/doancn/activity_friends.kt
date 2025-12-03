package com.example.doancn

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

class activity_friends : Menubottom() {

    private lateinit var rvFriends: RecyclerView
    private lateinit var adapter: UsersAdapter
    private val friendList = mutableListOf<User>()

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUid: String? get() = auth.currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        // Nếu muốn có bottom nav luôn:
        // setupBottomNav(R.id.nav_chat) hoặc tạo thêm item nav_friends

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvFriends = findViewById(R.id.rvFriends)
        adapter = UsersAdapter(friendList) { user ->
            // Khi bấm vào bạn bè trong danh bạ cũng mở chat
            openChatWith(user)
        }
        rvFriends.layoutManager = LinearLayoutManager(this)
        rvFriends.adapter = adapter

        loadAllFriends()
    }

    private fun loadAllFriends() {
        val uid = currentUid ?: return

        db.collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FriendsActivity", "Friend listener failed", error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    adapter.updateList(emptyList())
                    return@addSnapshotListener
                }

                val friendIds = mutableSetOf<String>()
                for (doc in snapshot.documents) {
                    val uid1 = doc.getString("uid1")
                    val uid2 = doc.getString("uid2")
                    if (uid1 == uid) friendIds.add(uid2!!)
                    else if (uid2 == uid) friendIds.add(uid1!!)
                }

                if (friendIds.isEmpty()) {
                    adapter.updateList(emptyList())
                    return@addSnapshotListener
                }

                db.collection("users")
                    .whereIn("uid", friendIds.toList())
                    .addSnapshotListener { usersSnap, err ->
                        if (err != null) {
                            Log.e("FriendsActivity", "User load failed", err)
                            return@addSnapshotListener
                        }
                        if (usersSnap != null) {
                            val users = usersSnap.toObjects(User::class.java)
                            adapter.updateList(users)
                        }
                    }
            }
    }

    private fun openChatWith(user: User) {
        val uid = currentUid ?: return
        val chatId = if (uid < user.uid) "${uid}_${user.uid}" else "${user.uid}_$uid"

        val intent = android.content.Intent(this, activity_chat::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("otherUid", user.uid)
            putExtra("otherName", user.fullName)
            putExtra("otherAvatar", user.profileImage)
        }
        startActivity(intent)
    }
}
