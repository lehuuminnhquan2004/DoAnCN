package com.example.doancn

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class activity_forgot_password : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val btnSend = findViewById<Button>(R.id.btnSendReset)
        val progress = findViewById<ProgressBar>(R.id.progress)

        btnSend.setOnClickListener {
            val email = edtEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            btnSend.isEnabled = false

            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Đã gửi link đặt lại mật khẩu. Vui lòng kiểm tra email.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Gửi thất bại: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnCompleteListener {
                    progress.visibility = View.GONE
                    btnSend.isEnabled = true
                }
        }
    }
}
