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

        parametre.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

    }

    fun verifyConnection(user: String, pass: String) {
        val dotenv = dotenv {
            directory = "/assets"
            filename = "env" // instead of '.env', use 'env'
        }
        val token = dotenv["TOKEN"]

        // find the url in shared preferences
        val sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        var url = sharedPreferences.getString("URL", null)
        url = if (url == null) "http://167.114.96.59:2223/api/authenticate/$user/$pass"
        else "$url/api/authenticate/$user/$pass"

        // Verify if the url is valid
        val request = token?.let {
            try {
                Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $token")
                    .build()
            } catch (e: IllegalArgumentException) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Connection impossible... Vérifiez l'adresse de votre l'API dans les paramètres",
                    Snackbar.LENGTH_LONG
                ).show()
                return
            }
        }
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Adresse invalide",
                    Snackbar.LENGTH_LONG
                ).show()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Adresse invalide",
                            Snackbar.LENGTH_LONG
                        ).show()
                        throw IOException("Unexpected code $response")
                    }
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Adresse valide",
                        Snackbar.LENGTH_LONG
                    ).show()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        })

    }

}