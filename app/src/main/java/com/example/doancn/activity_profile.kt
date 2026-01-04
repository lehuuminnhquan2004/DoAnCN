package com.example.doancn

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
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
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView

    private var currentFullName: String = ""
    private var currentAvatarUrl: String = ""

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
        tvFullName = findViewById(R.id.tvFullName)
        tvEmail = findViewById(R.id.tvEmail)
        val tvUid = findViewById<TextView>(R.id.tvUid)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnEditInfo = findViewById<Button>(R.id.btnEditInfo)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)

        val currentUser = auth.currentUser
        if (currentUser == null) {
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

                currentFullName = fullName
                currentAvatarUrl = profileImage

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

        btnEditInfo.setOnClickListener { showEditInfoDialog() }

        btnChangePassword.setOnClickListener {
            val email = auth.currentUser?.email
            if (email.isNullOrBlank()) {
                Toast.makeText(this, "Không lấy được email tài khoản", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Đã gửi email đổi mật khẩu tới $email", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gửi email thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                }
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
        db.collection("users").document(uid)
            .set(mapOf("profileImage" to url), SetOptions.merge())
            .addOnSuccessListener {
                progressAvatar.visibility = View.GONE
                currentAvatarUrl = url

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

    private fun showEditInfoDialog() {
        val uid = auth.currentUser?.uid ?: return
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null, false)
        val edtName = dialogView.findViewById<TextInputEditText>(R.id.edtName)
        val edtAvatarLink = dialogView.findViewById<TextInputEditText>(R.id.edtAvatarLink)
        edtName.setText(currentFullName)
        edtAvatarLink.setText(currentAvatarUrl)

        MaterialAlertDialogBuilder(this)
            .setTitle("Sửa thông tin")
            .setView(dialogView)
            .setPositiveButton("Lưu") { d, _ ->
                val newName = edtName.text?.toString()?.trim().orEmpty()
                val newAvatar = edtAvatarLink.text?.toString()?.trim().orEmpty()

                val updates = mutableMapOf<String, Any>()
                if (newName.isNotEmpty() && newName != currentFullName) updates["fullName"] = newName
                if (newAvatar.isNotEmpty() && newAvatar != currentAvatarUrl) updates["profileImage"] = newAvatar

                if (updates.isEmpty()) {
                    Toast.makeText(this, "Không có thay đổi", Toast.LENGTH_SHORT).show()
                    d.dismiss()
                    return@setPositiveButton
                }

                db.collection("users").document(uid)
                    .set(updates, SetOptions.merge())
                    .addOnSuccessListener {
                        if (newName.isNotEmpty()) {
                            currentFullName = newName
                            tvFullName.text = newName
                        }
                        if (newAvatar.isNotEmpty()) {
                            currentAvatarUrl = newAvatar
                            Glide.with(this)
                                .load(newAvatar)
                                .placeholder(R.drawable.person_circle_sharp)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(imgAvatar)
                        }
                        Toast.makeText(this, "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Cập nhật thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .show()
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
