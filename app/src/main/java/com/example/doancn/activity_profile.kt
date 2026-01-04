package com.example.doancn

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class activity_profile : Menubottom() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var imgAvatar: ImageView
    private lateinit var progressAvatar: ProgressBar

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { uploadAvatarAndSaveUrl(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setupBottomNav(R.id.nav_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        imgAvatar = findViewById(R.id.imgAvatar)
        progressAvatar = findViewById(R.id.progressAvatar)

        val tvFullName = findViewById<TextView>(R.id.tvFullName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val tvUid = findViewById<TextView>(R.id.tvUid)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Nếu chưa đăng nhập thì đưa về màn login
            startActivity(Intent(this, activity_login::class.java))
            finish()
            return
        }

        tvEmail.text = currentUser.email ?: ""
        tvUid.text = currentUser.uid

        // Load thông tin user
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                val fullName = doc.getString("fullName").orEmpty()
                val profileImage = doc.getString("profileImage").orEmpty()

                tvFullName.text = if (fullName.isNotBlank()) fullName else "Người dùng"

                if (profileImage.isNotBlank()) {
                    Glide.with(this)
                        .load(profileImage)
                        .placeholder(R.drawable.person_circle_sharp)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imgAvatar)
                } else {
                    imgAvatar.setImageResource(R.drawable.person_circle_sharp)
                }
            }
            .addOnFailureListener {
                tvFullName.text = "Người dùng"
                Toast.makeText(this, "Không tải được thông tin", Toast.LENGTH_SHORT).show()
            }

        imgAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnLogout.setOnClickListener {
            setOnlineStatus(false)
            auth.signOut()
            startActivity(Intent(this, activity_login::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    private fun uploadAvatarAndSaveUrl(imageUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        progressAvatar.visibility = View.VISIBLE

        val ref = storage.reference
            .child("avatars/$uid/${System.currentTimeMillis()}.jpg")

        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl
                    .addOnSuccessListener { uri ->
                        saveAvatarUrl(uid, uri.toString())
                    }
                    .addOnFailureListener {
                        progressAvatar.visibility = View.GONE
                        Toast.makeText(this, "Lấy link ảnh thất bại", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                progressAvatar.visibility = View.GONE
                Toast.makeText(this, "Upload thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveAvatarUrl(uid: String, url: String) {
        // Dùng set + merge để không bị fail nếu document chưa tồn tại
        db.collection("users").document(uid)
            .set(mapOf("profileImage" to url), SetOptions.merge())
            .addOnSuccessListener {
                progressAvatar.visibility = View.GONE

                Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.person_circle_sharp)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imgAvatar)

                Toast.makeText(this, "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressAvatar.visibility = View.GONE
                Toast.makeText(this, "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setOnlineStatus(isOnline: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .set(
                mapOf(
                    "online" to isOnline,
                    "lastSeen" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
    }
}
