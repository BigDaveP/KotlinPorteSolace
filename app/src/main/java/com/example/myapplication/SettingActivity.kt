package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.mqtt.MqttClientHelper
import com.google.android.material.snackbar.Snackbar
import io.github.cdimascio.dotenv.dotenv
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_setting.*
import okhttp3.*
import java.io.IOException
import java.util.*

class SettingActivity : AppCompatActivity() {
    private val mqttClient by lazy {
        MqttClientHelper(this)
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        spinner.adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Français", "English", "Россия", "Espagnol", "Deutsch", "Italiano", "Português", "中文", "日本語", "한국어")
        )
        spinner.setSelection(0)
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                // Nothing to do
            }

            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val intent = intent
                when (position) {
                    0 -> {
                        updateLocale("fr")
                        snackbarMessage()
                    }
                    1 -> {
                        updateLocale("en")
                        snackbarMessage()
                    }
                    2 -> {
                        updateLocale("ru")
                    }
                    3 -> {
                        updateLocale("es")
                    }
                    4 -> {
                        updateLocale("de")
                    }
                }
            }
        }
        enregistrerSetting.setOnClickListener {
            saveSetting()
        }
        editTextTextMQTTAdresse.setText(SOLACE_MQTT_HOST)


    }

    @SuppressLint("StringFormatInvalid")
    private fun snackbarMessage() {
        Snackbar.make(
            findViewById(android.R.id.content),
            getString(R.string.langue) + " " + spinner.selectedItem.toString(),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        if (locale == resources.configuration.locale) {
            // Si la locale est la même, ne rien faire
            return
        }
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        // Save data to shared preferences
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPref.edit()
        editor.putString("language", languageCode)
        editor.apply()
        recreate()

    }

    // Permet de changer l'adresse de l'API
    fun saveSetting() {
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
                "Le format de l'adresse IP est invalide",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        else{
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
                        "Adresse IP invalide",
                        Snackbar.LENGTH_LONG
                    ).show()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            Snackbar.make(
                                findViewById(android.R.id.content),
                                "Adresse IP invalide",
                                Snackbar.LENGTH_LONG
                            ).show()
                            throw IOException("Unexpected code $response")
                        }
                        editor.putString("URL", url)
                        editor.apply()
                    }
                }
            })
        }

        // Vérifie si l'adresse MQTT est valide
        val urlMQTT = editTextTextMQTTAdresse.text.toString()

        // Vérifie si le format de l'adresse est correct (tcp://xxx.xxx.xxx.xxx:xxxx)
        if (!urlMQTT.matches(Regex("^(tcp://)?[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+:[0-9]+"))){
            Snackbar.make(
                findViewById(android.R.id.content),
                "Le format de l'adresse MQTT est invalide",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        else {
            // Vérifie si l'adresse MQTT est valide
            SOLACE_MQTT_HOST = urlMQTT
            if (!mqttClient.isConnected()) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Impossible de se connecter au broker MQTT",
                    Snackbar.LENGTH_LONG
                ).show()
            }
            else{
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Adresse MQTT enregistrée",
                    Snackbar.LENGTH_LONG
                ).show()
                editor.putString("URLMQTT", urlMQTT)
                editor.apply()
            }
        }




    }
}