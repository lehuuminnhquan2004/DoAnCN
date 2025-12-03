package com.example.doancn

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class activity_profile : Menubottom() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNav(R.id.nav_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val imgAvatar = findViewById<ImageView>(R.id.imgAvatar)
        val tvFullName = findViewById<TextView>(R.id.tvFullName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvUid = findViewById<TextView>(R.id.tvUid)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val currentUser = auth.currentUser ?: return

        tvEmail.text = currentUser.email
        tvUid.text = currentUser.uid

        // Lấy thông tin từ Firestore: users/{uid}
        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val fullName = doc.getString("fullName") ?: ""
                    val profileImage = doc.getString("profileImage") ?: ""

                    tvFullName.text = fullName

                    if (profileImage.isNotEmpty()) {
                        Glide.with(this)
                            .load(profileImage)
                            .placeholder(R.drawable.person_circle_sharp)
                            .into(imgAvatar)
                    } else {
                        imgAvatar.setImageResource(R.drawable.person_circle_sharp)
                    }
                }
            }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, activity_login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
