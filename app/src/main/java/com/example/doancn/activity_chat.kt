package com.example.doancn

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

class activity_chat : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var toolbar: MaterialToolbar

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private var chatId: String? = null
    private var otherUid: String? = null

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        addControls()

        chatId = intent.getStringExtra("chatId")
        otherUid = intent.getStringExtra("otherUid")
        val otherName = intent.getStringExtra("otherName")
        val otherAvatar = intent.getStringExtra("otherAvatar")

        findViewById<TextView>(R.id.txtName).text = otherName
        val imgAvatar = findViewById<ImageView>(R.id.imgAvatar)
        Glide.with(this)
            .load(otherAvatar)
            .placeholder(R.drawable.person_circle_sharp)
            .into(imgAvatar)

        adapter = MessageAdapter(messages, auth.currentUser!!.uid)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        // ✅ đảm bảo chat document có participants để Main/Friends query được
        ensureChatDocument()

        listenForMessages()

        btnSend.setOnClickListener { sendMessage() }
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun addControls() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerMessages)
        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSend)
    }

    private fun ensureChatDocument() {
        val uid = auth.currentUser?.uid ?: return
        val other = otherUid ?: return
        val cid = chatId ?: return

        val data = mapOf(
            "participants" to listOf(uid, other),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        db.collection("chats").document(cid)
            .set(data, SetOptions.merge())
    }

    private fun sendMessage() {
        val uid = auth.currentUser?.uid ?: return
        val other = otherUid ?: return
        val cid = chatId ?: return

        val text = edtMessage.text.toString().trim()
        if (text.isEmpty()) return

        val msg = Message(
            senderId = uid,
            receiverId = other,
            message = text,

        )

        val chatRef = db.collection("chats").document(cid)

        // ✅ ghi message + update lastMessage/updatedAt để list chat hiển thị đúng
        chatRef.collection("messages")
            .add(msg)
            .addOnSuccessListener {
                chatRef.set(
                    mapOf(
                        "participants" to listOf(uid, other),
                        "lastMessage" to text,
                        "lastSenderId" to uid,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
            }

        edtMessage.text.clear()
    }

    private fun listenForMessages(){
        db.collection("chats").document(chatId!!)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val newMessages = snapshot.toObjects(Message::class.java)
                messages.clear()
                messages.addAll(newMessages)
                adapter.notifyDataSetChanged()

                // ✅ CHỈ scroll khi có item
                if (messages.isNotEmpty()) {
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }
}
