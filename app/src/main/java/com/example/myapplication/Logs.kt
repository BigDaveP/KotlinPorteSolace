package com.example.myapplication

import com.beust.klaxon.Json


data class Logs(
    @Json(name = "_id")
    val id: String,
    @Json(name = "user")
    val username: String,
    @Json(name = "tag")
    val UID: String,
    @Json(name = "date")
    val date: String,
    @Json(name = "value")
    val serrure: String
)