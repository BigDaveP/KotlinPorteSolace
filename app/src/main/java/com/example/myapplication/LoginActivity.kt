package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.*

class LoginActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)
        button.setOnClickListener {
            val user = editTextTextPersonName.text.toString()
            val pass = editTextTextPassword.text.toString()

            Log.d("user", user)
            Log.d("pass", pass)

            /*val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)*/
        }



    }

}