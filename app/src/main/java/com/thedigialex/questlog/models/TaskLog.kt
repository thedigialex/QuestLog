package com.thedigialex.questlog.models

data class TaskLog(
    var id: Int,
    var taskId: Int,
    var loggedDate: String,
    var isCompleted: Int,
    var taskName: String = ""
)
