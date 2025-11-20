package com.example.doancn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    private lateinit var rvUsers: RecyclerView
    private lateinit var adapter: UsersAdapter
    private val userList = mutableListOf<User>()

    private val db= Firebase.firestore
    private val auth= FirebaseAuth.getInstance()
    private val currentUid: String? get()=auth.currentUser?.uid

    private var usersListener: ListenerRegistration?=null

    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvUsers=findViewById(R.id.rvUsers)
        adapter= UsersAdapter(userList){
            user -> openChatWith(user)
        }
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = adapter
        loadFriendsRealtime()
        clickbottom()

        toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.action_search ->{
                    startActivity(Intent(this, activity_search_friend::class.java))
                    true
                }
                else -> false
            }
        }

    }

    private fun loadFriendsRealtime() {
        val uid = currentUid ?: return


        db.collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MainActivity", "Friend listener failed", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val friendIds = mutableSetOf<String>()
                    for (doc in snapshot.documents) {
                        val uid1 = doc.getString("uid1")
                        val uid2 = doc.getString("uid2")
                        if (uid1 == currentUid) friendIds.add(uid2!!)
                        else if (uid2 == currentUid) friendIds.add(uid1!!)
                    }

                    if (friendIds.isEmpty()) {
                        adapter.updateList(emptyList())
                        return@addSnapshotListener
                    }

                    db.collection("users")
                        .whereIn("uid", friendIds.toList())
                        .addSnapshotListener { usersSnap, err ->
                            if (err != null) {
                                Log.e("MainActivity", "User load failed", err)
                                return@addSnapshotListener
                            }
                            if (usersSnap != null) {
                                val friends = usersSnap.toObjects(User::class.java)
                                adapter.updateList(friends)
                            }
                        }
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
        usersListener?.remove()
    }
    private fun clickbottom() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_requests -> {
                    startActivity(Intent(this, activity_friend_requests::class.java))
                    true
                }
                R.id.nav_chat ->{
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                else -> false

            }
        }
    }

}
