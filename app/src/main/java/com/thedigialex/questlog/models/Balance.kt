package com.thedigialex.questlog.models

data class Balance(
    var id: Int,
    var current_balance: Int,
    val borrowed_balance: Int,
)
