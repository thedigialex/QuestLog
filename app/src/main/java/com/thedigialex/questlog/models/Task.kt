package com.thedigialex.questlog.models

data class Task(
    var id: Int = 0,
    var currentLogId: Int = 0,
    var type: String = "Daily",
    var description: String = "New Quest",
    var repeat: Int = 0,
    var isNew: Boolean = false,
    var isCompleted: Int = 0
)

