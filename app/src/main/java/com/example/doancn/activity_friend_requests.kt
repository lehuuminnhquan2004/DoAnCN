package com.example.doancn

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class activity_friend_requests : Menubottom() {
    private var listFriendRequests = mutableListOf<User>()
    private lateinit var adapter: FriendRequestsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var currentUid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_requests)
        setupBottomNav(R.id.nav_requests)


        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()



        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }
        recyclerView = findViewById(R.id.rvRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)//QUAN TRỌNG

        adapter = FriendRequestsAdapter(listFriendRequests, ::onAcceptClick, ::onDeclineClick)
        recyclerView.adapter = adapter

        loadRequests();

    }

    private fun loadRequests() {
        currentUid = auth.currentUser?.uid ?: return

        db.collection("friend_requests")
            .whereEqualTo("toUid", currentUid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents ?: emptyList()
                val fromUids = requests.mapNotNull { it.getString("fromUid") }

                if (fromUids.isEmpty()) {
                    listFriendRequests.clear()
                    adapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }

                db.collection("users")
                    .whereIn("uid", fromUids)
                    .get()
                    .addOnSuccessListener { usersSnap ->
                        val users = usersSnap.toObjects(User::class.java)
                        listFriendRequests.clear()
                        listFriendRequests.addAll(users)
                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                        // Xử lý lỗi nếu cần
                    }
            }
    }



    private fun onAcceptClick(user: User) {
        val requestId="${user.uid}_$currentUid"

        db.collection("friend_requests")
            .document(requestId)
            .update("status", "accepted")
        val friendId="${currentUid}_${user.uid}"

        val dataFriend= hashMapOf(
            "uid1" to currentUid,
            "uid2" to user.uid,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("friends")
            .document(friendId)
            .set(dataFriend)
            .addOnSuccessListener {
                listFriendRequests.remove(user)
                adapter.notifyDataSetChanged()
            }
    }
    private fun onDeclineClick(user: User) {
        val requestId = "${user.uid}_$currentUid"
        db.collection("friend_requests").document(requestId).delete()
    }



}

