package com.example.doancn

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class activity_chat : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var btnSend: ImageButton

    private val db= Firebase.firestore
    private val auth= Firebase.auth
    private var chatId: String?=null
    private var otherUid: String?=null

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter
    private lateinit var toolbar: MaterialToolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)


        addControls()
        chatId=intent.getStringExtra("chatId")
        otherUid=intent.getStringExtra("otherUid")
        val otherName=intent.getStringExtra("otherName")
        val otherAvatar=intent.getStringExtra("otherAvatar")

        findViewById<TextView>(R.id.txtName).text=otherName
        val imgAvatar=findViewById<ImageView>(R.id.imgAvatar)
        Glide.with(this)
            .load(otherAvatar)
            .placeholder(R.drawable.person_circle_sharp)
            .into(imgAvatar)

        adapter= MessageAdapter(messages,auth.currentUser!!.uid)
        recyclerView.layoutManager= LinearLayoutManager(this)
        recyclerView.adapter=adapter

        listenForMessages()

        btnSend.setOnClickListener { sendMessage() }
        toolbar.setNavigationOnClickListener { finish() }

    }

    private fun addControls(){
        toolbar=findViewById(R.id.toolbar)

        recyclerView = findViewById(R.id.recyclerMessages)
        edtMessage = findViewById(R.id.edtMessage)
        btnSend = findViewById(R.id.btnSend)
    }

    private fun sendMessage(){
        val text=edtMessage.text.toString().trim()
        if(text.isEmpty())return

        val msg=Message(
            senderId = auth.currentUser!!.uid,
            receiverId = otherUid!!,
            message = text
        )

        db.collection("chats").document(chatId!!)
            .collection("messages")
            .add(msg)

        edtMessage.text.clear()
    }
    private fun listenForMessages(){
        db.collection("chats").document(chatId!!)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener{snapshot,error ->
                if(error!=null||snapshot==null){
                    return@addSnapshotListener
                }
                val newMessages=snapshot.toObjects(Message::class.java)
                messages.clear()
                messages.addAll(newMessages)
                adapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size-1)
            }

    }
}