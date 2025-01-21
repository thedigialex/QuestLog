package com.thedigialex.questlog.models

data class Category(
    var id: Int = 0,
    var type: String = "Income",
    var name: String = "New Category",
    var details: String = "",
    var target_amount: Int = 0,
    var isNew: Boolean = false
)
