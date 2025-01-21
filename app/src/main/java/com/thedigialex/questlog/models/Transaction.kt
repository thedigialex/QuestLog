package com.thedigialex.questlog.models

data class Transaction(
    var id: Int = 0,
    var type: String = "Income",
    var amount: Int = 0,
    var category: String = "",
    var timestamp: String = "",
    var isNew: Boolean = false
)
