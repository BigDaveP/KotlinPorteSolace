package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_login.*
import com.google.android.material.snackbar.Snackbar
import io.github.cdimascio.dotenv.dotenv
import okhttp3.*
import java.io.IOException

class LoginActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)

        button.setOnClickListener {
            val user = editTextTextPersonName.text.toString()
            val pass = editTextTextPassword.text.toString()
            verifyConnection(user, pass)
            Log.d("user", user)
            Log.d("pass", pass)
        }


    }

    fun verifyConnection(user: String, pass: String) {
        val dotenv = dotenv {
            directory = "/assets"
            filename = "env" // instead of '.env', use 'env'
        }
        val token = dotenv["TOKEN"]


        val url = "http://167.114.96.59:2223/api/authenticate/$user/$pass";
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    if (response.body()!!.string() == "true") {
                        Log.d("response", "true")
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d("response", "false")
                        runOnUiThread {
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Mauvais identifiants",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        })
    }

}