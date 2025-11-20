package com.example.doancn

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class activity_search_friend : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsersAdapterSearchFriend
    private lateinit var etSearch: EditText

    private val users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_friend)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerViewUsers)
        etSearch = findViewById(R.id.etSearch)

        adapter = UsersAdapterSearchFriend(users) { selectedUser ->
            sendFriendRequest(selectedUser)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchUser(query)
                } else {
                    users.clear()
                    adapter.notifyDataSetChanged()
                }
            }
        })
    }

    private fun searchUser(query: String) {
        db.collection("users")
            .orderBy("fullName")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { docs ->
                users.clear()
                if (docs.isEmpty) {
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                for (doc in docs) {
                    val user = doc.toObject(User::class.java)
                    if (user.uid != auth.currentUser?.uid) users.add(user)
                    adapter.notifyDataSetChanged()
                }

            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi khi tìm kiếm: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendFriendRequest(targetUser: User) {

        val currentUid = auth.uid ?: return
        val targetUid = targetUser.uid
        val friendRequestId = "${currentUid}_${targetUid}";
        val request = hashMapOf(
            "fromUid" to currentUid,
            "toUid" to targetUser.uid,
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("friend_requests").document(friendRequestId)
            .set(request)
            .addOnSuccessListener {
                Toast.makeText(this, "Đã gửi lời mời kết bạn đến ${targetUser.fullName}", Toast.LENGTH_SHORT).show()
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gửi lời mời thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun onAcceptClick(user: User) {
        val currentUid = auth.uid ?: return
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
                adapter.notifyDataSetChanged()
            }
    }
}
