package com.thedigialex.questlog.models

data class Item(
    var id: Int = 0,
    val type: String,
    val resource: String,
    val imageValue: Int = 0,
    val cost: Int,
    val levelRequired: Int,
    var obtained: Int = 0
)
