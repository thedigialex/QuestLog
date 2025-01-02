package com.thedigialex.questlog.models

data class Note(
    var id: Int = 0,
    var title: String = "New Note",
    var description: String = "",
    var sortOrder: Int = 0,
    var isNew: Boolean = false,
)
