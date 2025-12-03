package com.example.doancn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

open class Menubottom : AppCompatActivity() {

    protected fun setupBottomNav(selectedItemId: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = selectedItemId

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_requests -> {
                    if (selectedItemId != R.id.nav_requests)
                        startActivity(Intent(this, activity_friend_requests::class.java))
                    true
                }

                R.id.nav_chat -> {
                    if (selectedItemId != R.id.nav_chat)
                        startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.nav_profile -> {
                    if (selectedItemId != R.id.nav_profile)
                        startActivity(Intent(this, activity_profile::class.java))
                    true
                }
                R.id.nav_friends ->{
                    if(selectedItemId != R.id.nav_friends)
                        startActivity(Intent(this, activity_friends::class.java))
                    true
                }

                else -> false
            }
        }
    }
}
