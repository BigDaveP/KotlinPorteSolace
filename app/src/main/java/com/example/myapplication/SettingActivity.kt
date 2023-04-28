package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.github.cdimascio.dotenv.dotenv
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_setting.*
import okhttp3.*
import java.io.IOException

class SettingActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        enregistrerSetting.setOnClickListener {
            changeApiUrl()
        }
    }

    // Permet de changer l'adresse de l'API
    fun changeApiUrl() {
        val sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val url = editTextTextAPIAdresse.text.toString()
        val dotenv = dotenv {
            directory = "/assets"
            filename = "env" // instead of '.env', use 'env'
        }
        val token = dotenv["TOKEN"]


        // Vérifie si le format de l'adresse est correct (http://xxx.xxx.xxx.xxx:xxxx)
        if (!url.matches(Regex("^(http://)?[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+:[0-9]+"))){
            Snackbar.make(
                findViewById(android.R.id.content),
                "Le format de l'adresse est invalide",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        else{

            editor.putString("URL", url)
            editor.apply()

            // fait un appel à l'API pour vérifier si l'adresse est valide
            val request = Request.Builder()
                .url("$url/api/getSerrures")
                .header("Authorization", "Bearer $token")
                .build()
            val client = OkHttpClient()
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
                            "Adresse enregistrée",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                    finish()
                }
            })
        }


    }



}