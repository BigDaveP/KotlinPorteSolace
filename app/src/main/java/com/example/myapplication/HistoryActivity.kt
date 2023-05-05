package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import com.beust.klaxon.Klaxon
import com.example.myapplication.model.Logs
import io.github.cdimascio.dotenv.dotenv
import kotlinx.android.synthetic.main.activity_history.*
import okhttp3.*
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class HistoryActivity : AppCompatActivity() {
    private val klaxon = Klaxon()
    private var logs = ArrayList<Logs>()
    private var isCalled = false
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        btnRetour.setOnClickListener {
            finish()
        }
        Timer("CheckApiStatus", false).schedule(2000){
            getHistory()
        }

        Thread(Runnable {
            while (!isCalled) {
                Thread.sleep(1000)
            }
            runOnUiThread {
                progressBar2.setVisibility(View.INVISIBLE)
                textViewAPI.text = ""
                for (log in logs) {
                    textViewAPI.text = textViewAPI.text.toString() + "Tag ID : " +log.UID + "\n Date : " + log.date + "\n Utilisateur : " + log.username + "\n Serrure : " + log.serrure + "\n\n"
                    textViewAPI.text = textViewAPI.text.toString() + "--------------------------------------------\n\n\n"
                }
            }
        }).start()
    }


    @SuppressLint("SetTextI18n")
    private fun getHistory() {
        val dotenv = dotenv {
            directory = "/assets"
            filename = "env" // instead of '.env', use 'env'
        }
        val token = dotenv["TOKEN"]
        val url = "http://167.114.96.59:2223/api/getLogs"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                isCalled = false
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()!!.string()
                logs = klaxon.parseArray<Logs>(body) as ArrayList<Logs>
                isCalled = true

            }
        })

    }
}