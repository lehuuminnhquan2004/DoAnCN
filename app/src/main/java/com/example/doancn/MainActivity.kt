package com.example.doancn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

class MainActivity : Menubottom() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var adapter: UsersAdapter
    private val userList = mutableListOf<User>()

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val currentUid: String? get() = auth.currentUser?.uid

    private var chatsListener: ListenerRegistration? = null

    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBottomNav(R.id.nav_chat)

        rvUsers = findViewById(R.id.rvUsers)
        adapter = UsersAdapter(userList) { user -> openChatWith(user) }
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter

        toolbar = findViewById(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_search -> {
                    startActivity(Intent(this, activity_search_friend::class.java))
                    true
                }
                else -> false
            }
        }

        loadChattedFriends()
    }

    private fun loadChattedFriends() {
        val uid = currentUid ?: return

        chatsListener?.remove()

        chatsListener = db.collection("chats")
            .whereArrayContains("participants", uid)
            // nếu bạn có updatedAt thì sort chat mới nhất lên đầu:
            // .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MainActivity", "Chat listener failed", error)
                    adapter.updateList(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    adapter.updateList(emptyList())
                    return@addSnapshotListener
                }

                // Lấy otherUid từ docId: uidA_uidB
                val otherIds = snapshot.documents.mapNotNull { doc ->
                    val parts = doc.id.split("_")
                    if (parts.size != 2) return@mapNotNull null
                    val (a, b) = parts
                    when (uid) {
                        a -> b
                        b -> a
                        else -> null
                    }
                }.distinct()

                if (otherIds.isEmpty()) {
                    adapter.updateList(emptyList())
                    return@addSnapshotListener
                }

                loadUsersByDocId(otherIds)
            }
    }

    private fun loadUsersByDocId(uids: List<String>) {
        val all = mutableListOf<User>()
        val chunks = uids.distinct().chunked(10) // whereIn tối đa 10
        var done = 0

        chunks.forEach { batch ->
            db.collection("users")
                .whereIn(FieldPath.documentId(), batch) // users/{uid}
                .get()
                .addOnSuccessListener { snap ->
                    all.addAll(snap.toObjects(User::class.java))
                    done++
                    if (done == chunks.size) {
                        adapter.updateList(all.distinctBy { it.uid })
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Load users failed", e)
                }
        }
    }

    private fun openChatWith(user: User) {
        val uid = currentUid ?: return
        val chatId = if (uid < user.uid) "${uid}_${user.uid}" else "${user.uid}_$uid"

        val intent = Intent(this, activity_chat::class.java).apply {
            putExtra("chatId", chatId)
            putExtra("otherUid", user.uid)
            putExtra("otherName", user.fullName)
            putExtra("otherAvatar", user.profileImage)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        chatsListener?.remove()
    }
}
