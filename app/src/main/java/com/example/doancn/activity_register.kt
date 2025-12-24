package com.example.doancn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class activity_register : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPassword2: EditText
    private lateinit var etOtp: EditText
    private lateinit var btnSendOtp: Button
    private lateinit var btnLogin: Button
    private lateinit var btnVerifyOtp: Button
    private lateinit var otpLayout: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var generatedOtp: String? = null
    private var emailInput = ""
    private var passwordInput = ""
    private var nameInput = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Ánh xạ view
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etPassword2 = findViewById(R.id.etPassword2)
        etOtp = findViewById(R.id.etOtp)
        btnSendOtp = findViewById(R.id.btnSendOtp)
        btnLogin= findViewById(R.id.btnLogin)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)
        otpLayout = findViewById(R.id.otpLayout)
        progressBar = findViewById(R.id.progressBar)

        btnSendOtp.setOnClickListener {
            nameInput = etFullName.text.toString().trim()
            emailInput = etEmail.text.toString().trim()
            passwordInput = etPassword.text.toString().trim()
            val password2 = etPassword2.text.toString().trim()

            if (nameInput.isEmpty() || emailInput.isEmpty() || passwordInput.isEmpty() || password2.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordInput != password2) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendOtp(emailInput)
        }
        btnLogin.setOnClickListener {
            startActivity(Intent(this, activity_login::class.java))
            finish()
        }


        btnVerifyOtp.setOnClickListener {
            verifyOtp()
        }
    }

    private fun sendOtp(email: String) {
        progressBar.visibility = View.VISIBLE
        generatedOtp = (100000..999999).random().toString()

        Thread {
            val success = JavaMailSender.sendEmail(
                toEmail = email,
                subject = "Mã xác thực đăng ký ChatApp",
                message = """
                    Xin chào $nameInput,
                    
                    Đây là mã xác thực của bạn: $generatedOtp
                    
                    Vui lòng nhập mã này vào ứng dụng để hoàn tất đăng ký.
                    
                    Trân trọng,
                    Đội ngũ ChatApp
                """.trimIndent()
            )

            runOnUiThread {
                progressBar.visibility = View.GONE
                if (success) {
                    showOtpLayout()
                    Toast.makeText(this, "Đã gửi mã xác thực đến $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Không gửi được email. Kiểm tra kết nối!", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showOtpLayout() {
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 400
        otpLayout.visibility = View.VISIBLE
        otpLayout.startAnimation(fadeIn)
    }

    private fun verifyOtp() {
        val inputOtp = etOtp.text.toString().trim()
        if (inputOtp.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã xác thực", Toast.LENGTH_SHORT).show()
            return
        }

        if (inputOtp == generatedOtp) {
            createAccount()
        } else {
            Toast.makeText(this, "Mã xác thực không đúng!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAccount() {
        progressBar.visibility = View.VISIBLE
        auth.createUserWithEmailAndPassword(emailInput, passwordInput)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val user = mapOf(
                    "uid" to uid,
                    "fullName" to nameInput,
                    "email" to emailInput,
                    "profileImage" to "",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).set(user)
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, activity_register::class.java))
                finish()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Đăng ký thất bại: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
