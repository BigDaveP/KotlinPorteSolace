package com.example.myapplication


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.mqtt.MqttClientHelper
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import okhttp3.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.io.IOException
import java.lang.reflect.Type
import java.net.URLEncoder
import java.util.*
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity() {

    var value = "";
    var isScanned = false
    private var tag = ""
    private val client = OkHttpClient()
    private val mqttClient by lazy {
        MqttClientHelper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewMsgPayload.movementMethod = ScrollingMovementMethod()

        setMqttCallBack()

        // initialize 'num msgs received' field in the view
        textViewNumMsgs.text = "0"

        // pub button
        btnPub.setOnClickListener { view ->
            var snackbarMsg : String
            val topic = "porte_sub"
            snackbarMsg = "Cannot publish to empty topic!"
            if (topic.isNotEmpty()) {
                snackbarMsg = try {
                    mqttClient.publish(topic, value)
                    "Published to topic '$topic'"
                } catch (ex: MqttException) {
                    "Error publishing to topic: $topic"
                }
            }
            Snackbar.make(view, snackbarMsg, 300)
                .setAction("Action", null).show()

        }

        // sub button
        btnSub.setOnClickListener { view ->
            var snackbarMsg : String
            val topic = "porte_sub"
            snackbarMsg = "Cannot subscribe to empty topic!"
            if (topic.isNotEmpty()) {
                snackbarMsg = try {
                    mqttClient.subscribe(topic)
                    "Subscribed to topic '$topic'"
                    // Publier sur le topic "porte_sub" pour que le serveur envoie un message
                } catch (ex: MqttException) {
                    "Error subscribing to topic: $topic"
                }
            }
            Snackbar.make(view, snackbarMsg, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
        }

        // Redirection vers l'activité de l'historique
        btnHistory.setOnClickListener {
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }


        Timer("CheckMqttConnection", false).schedule(3000) {
            if (!mqttClient.isConnected()) {
                Snackbar.make(textViewNumMsgs, "Impossible de se connecter à l'adresse suivante: '$SOLACE_MQTT_HOST', vérifier votre connexion internet", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Action", null).show()
            }
        }

    }

    private fun setMqttCallBack() {
        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                val snackbarMsg = "Connected to host:\n'$SOLACE_MQTT_HOST'."
                Log.w("Debug", snackbarMsg)
                Snackbar.make(findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            override fun connectionLost(throwable: Throwable) {
                val snackbarMsg = "Connection to host lost:\n'$SOLACE_MQTT_HOST'"
                Log.w("Debug", snackbarMsg)
                Snackbar.make(findViewById(android.R.id.content), snackbarMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            @Throws(Exception::class)
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                Log.w("Debug", "Message received from host '$SOLACE_MQTT_HOST': $mqttMessage")
                textViewNumMsgs.text = ("${textViewNumMsgs.text.toString().toInt() + 1}")
                tag = "";
                tag = "$mqttMessage\n"
                CompareParseValueToSub(tag)
                textViewMsgPayload.text = tag
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
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        mqttClient.destroy()
        super.onDestroy()
    }


    //Permet de récupérer la liste des utilisateurs et de les afficher dans la liste "userList"
    @SuppressLint("SetTextI18n")
    fun CompareParseValueToSub (tagScan: String){
        val request = Request.Builder()
            .url("http://167.114.96.59:2223/api/verifyTag/$tagScan")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    value = response.body()!!.string()
                    saveToLog(tagScan, value)
                    Log.d("Debug", value)
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
        val parseURL = url.replace(" ", "%20").replace("\n", "")
        Log.d("Debug", parseURL)
        //Send to server
        val request = Request.Builder()
            .url(parseURL)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    Log.d("Debug", value)
                }
            }
        })
        }

    }



