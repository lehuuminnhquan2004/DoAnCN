package com.example.doancn

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class activity_login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var btnRegister: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        //chuyen den main neu da dang nhap
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        btnLogin = findViewById(R.id.btnLogin)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if(validateInputs(email, password)){
                loginUser(email, password)
            }
        }
        btnRegister=findViewById(R.id.btnRegister)
        btnRegister.setOnClickListener {
            startActivity(Intent(this, activity_register::class.java))
        }

    }
    private fun validateInputs(email: String, password: String): Boolean {
        if(email.isEmpty()){
            etEmail.error = "Email không được để trống"
            return false
        }
        if(password.isEmpty()){
            etPassword.error = "Mật khẩu không được để trống"
            return false
        }
        if(password.length < 6){
            etPassword.error = "Mật khẩu phải có ít nhất 6 ký tự"
            return false
        }
        return true
    }

    private fun loginUser(email: String, password: String) {
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)

            .addOnCompleteListener(this) { task ->

                progressBar.visibility = View.GONE
                btnLogin.isEnabled=true
                if(task.isSuccessful){
                    Toast.makeText(this, "Dang nhap thanh cong", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }else{
                    Toast.makeText(this, "Dang nhap that bai", Toast.LENGTH_SHORT).show()

                }
            }

    }

}