package com.thedigialex.questlog.models

data class Category(
    var id: Int = 0,
    val type: String,
    val name: String,
    val target_amount: Int = 0,
)
