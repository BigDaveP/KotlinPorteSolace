package com.example.myapplication.model

import com.beust.klaxon.Json


data class Serrures(
    @Json(name = "_id")
    val id: String,
    @Json(name = "serrure")
    val serrure: String
)