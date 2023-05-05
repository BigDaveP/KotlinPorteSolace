package com.example.myapplication


import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.Klaxon
import com.example.myapplication.model.Logs
import com.example.myapplication.model.Serrures
import com.example.myapplication.mqtt.MqttClientHelper
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import io.github.cdimascio.dotenv.dotenv
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.math.log


class MainActivity : AppCompatActivity() {

    var value = "";
    var isScanned = false
    var serruresListID = ArrayList<String>()
    private var tag = ""
    private val client = OkHttpClient()
    private val mqttClient by lazy {
        MqttClientHelper(this)
    }
    private val klaxon = Klaxon()

    private val dotenv = dotenv {
        directory = "/assets"
        filename = "env" // instead of '.env', use 'env'
    }
    var token = dotenv["TOKEN"]

        // Redirection vers l'activité de l'historique

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textViewMsgPayload.movementMethod = ScrollingMovementMethod()

        setMqttCallBack()
        getSerrures()


        btnHistory.setOnClickListener {
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }

        Timer("SettingSub", false).schedule(2000) {
            if (mqttClient.isConnected()) {
                val topic = "porte_sub"
                Thread.sleep(1000)
                mqttClient.subscribe(topic)
            }
        }

        Timer("CheckMqttConnection", false).schedule(3000) {
            if (!mqttClient.isConnected()) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Connection au serveur MQTT perdue",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

    }

    override fun onBackPressed() {
        return
    }
    private fun setMqttCallBack() {
        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                val snackbarMsg = getString(R.string.mqtt_connect)
                Log.w("Debug", snackbarMsg)
                Snackbar.make(findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            override fun connectionLost(throwable: Throwable) {
                val snackbarMsg = getString(R.string.erreur_mqtt)
                Log.w("Debug", snackbarMsg)
                Snackbar.make(findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            @SuppressLint("SetTextI18n")
            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                tag = "";
                if (mqttMessage.toString() != "true" || mqttMessage.toString() != "false") {
                    tag = "$mqttMessage\n"
                    CompareParseValueToSub(tag)
                    textViewMsgPayload.text = tag
                }
                // Get all child of the linear layout and get the textview text
                val childCount = linearlayout.childCount
                for (i in 0 until childCount) {
                    val v = linearlayout.getChildAt(i)
                    if (v is LinearLayout) {
                        for (j in 0 until v.childCount) {
                            val v2 = v.getChildAt(j)
                            if (v2 is TextView) {
                                // Remove part of the string that contain true or false
                                var tagParse = tag.replace(" true", "")
                                tagParse = tagParse.replace(" false", "")
                                if (tagParse.trim() == v2.text.trim()) {
                                    //get the next element
                                    val v3 = v.getChildAt(j + 1)
                                    if (v3 is TextView) {
                                        if (mqttMessage.toString().contains("true")) {
                                            v3.text = getString(R.string.ouvert)
                                            v3.setBackgroundColor(Color.parseColor("#00FF00"))
                                        } else {
                                            v3.text = getString(R.string.ferme)
                                            v3.setBackgroundColor(Color.parseColor("#FF0000"))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                /*if (mqttMessage.toString().contains("C089 true")) {
                    Log.d("Debug", "Oui")
                    for (i in 0 until serruresListID.size) {
                        Log.d("Debug", "ID: ${serruresListID[i]}")
                    }
                    /*textViewMsgSerrure1Status.text = "Ouvert"
                    textViewMsgSerrure1Status.setBackgroundColor(Color.parseColor("#00FF00"))*/
                }
                if (mqttMessage.toString().contains("C089 false")) {
                    Log.d("Debug", "Non")
                    /*textViewMsgSerrure1Status.text = "Fermé"
                    textViewMsgSerrure1Status.setBackgroundColor(Color.parseColor("#FF0000"))*/
                }*/
            }

            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {
                Log.w("Debug", "Message published to host '$SOLACE_MQTT_HOST'")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml
        when (item.itemId) {
            R.id.action_settings -> startActivity(Intent(this@MainActivity, SettingActivity::class.java))

        }
        return true
    }

    override fun onDestroy() {
        mqttClient.destroy()
        super.onDestroy()
    }

    fun getSerrures() {
        val dotenv = dotenv {
            directory = "/assets"
            filename = "env" // instead of '.env', use 'env'
        }
        val token = dotenv["TOKEN"]
        val request = Request.Builder()
            .url("http://167.114.96.59:2223/api/getSerrures")
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @SuppressLint("RtlHardcoded")
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    value = response.body()!!.string()
                    val serrures = klaxon.parseArray<Serrures>(value) as ArrayList<Serrures>
                    // append in the scroll view
                    runOnUiThread {
                        if (serrures.size > 0) {
                            for (serrure in serrures) {
                                // Create a container for the text views
                                val container = LinearLayout(this@MainActivity)
                                container.orientation = LinearLayout.HORIZONTAL
                                container.setPadding(50, 0, 0, 10)
                                container.gravity = Gravity.CENTER
                                container.setBackgroundColor(Color.parseColor("#FFFFFF"))
                                container.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )

                                // Create a text view for the message
                                val textView = TextView(this@MainActivity)
                                textView.text = serrure.serrure
                                textView.textSize = 20f
                                textView.setTextColor(Color.parseColor("#000000"))
                                textView.setPadding(0, 10, 0, 20)
                                textView.gravity = Gravity.START
                                textView.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                container.addView(textView)

                                // Create a text view for the status
                                val textViewStatusColor = TextView(this@MainActivity)
                                //Create an id for the textview
                                val id = View.generateViewId()
                                serruresListID.add(textViewStatusColor.id.toString())
                                textViewStatusColor.text = getString(R.string.ferme)
                                textViewStatusColor.textSize = 20f
                                textViewStatusColor.setBackgroundColor(Color.parseColor("#FF0000"))
                                textViewStatusColor.setTextColor(Color.parseColor("#FFFFFF"))
                                textViewStatusColor.setPadding(0, 0, 0, 10)
                                textViewStatusColor.gravity = Gravity.CENTER
                                textViewStatusColor.layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    1f
                                )
                                container.addView(textViewStatusColor)

                                // Add the container to the parent view
                                linearlayout.addView(container)
                            }
                        }
                    }
                }
            }
        })
    }

    //Permet de récupérer la liste des utilisateurs et de les afficher dans la liste "userList"
    @SuppressLint("SetTextI18n")
    fun CompareParseValueToSub (tagScan: String){
        val request = Request.Builder()
            .url("http://167.114.96.59:2223/api/verifyTag/$tagScan")
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    value = response.body()!!.string()
                    if (mqttClient.isConnected()){
                        var isSend = true
                        if (isSend && value != "false"){
                            val topic = "porte_sub"
                            mqttClient.publish(topic, value)
                            isSend = false
                            saveToLog(tagScan, value)
                            Log.d("Debug", value)
                        }
                    }
                }

            }
        })
    }

    fun saveToLog(tagScan: String, value: String){
        val tagToSend = tagScan
        val user = "Un utilisateur"
        //Get DateTime
        val c = Calendar.getInstance().time
        //Url encode
        val url = "http://167.114.96.59:2223/api/saveToLogs/$user/$tagToSend/$value/$c"
        Log.d("DebugURL", url)
        val parseURL = url.replace(" ", "%20").replace("\n", "")
        Log.d("Debug", parseURL)
        //Send to server
        val request = Request.Builder()
            .url(parseURL)
            .header("Authorization", "Bearer $token")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    Log.d("SendToAPI", "$user $value $tagToSend $c")
                }
            }
        })
        }

    }



