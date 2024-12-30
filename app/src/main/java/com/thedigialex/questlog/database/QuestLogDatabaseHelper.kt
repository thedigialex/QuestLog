package com.thedigialex.questlog.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.thedigialex.questlog.models.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class QuestLogDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE Users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                level INTEGER DEFAULT 1,
                exp INTEGER DEFAULT 0,
                coins INTEGER DEFAULT 0,
                equippedAvatarId INTEGER DEFAULT 0,
                equippedBackgroundId INTEGER DEFAULT 0,
                equippedTitleId  INTEGER DEFAULT 0,
                equippedClassId  INTEGER DEFAULT 0
            );
            """
        )
        db.execSQL(
            """
            CREATE TABLE Tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                currentLogId INTEGER DEFAULT 0,
                type TEXT NOT NULL CHECK(type IN ('Daily', 'Weekly', 'Monthly')),
                description TEXT NOT NULL,
                repeat INTEGER NOT NULL DEFAULT 1
            );
            """
        )
        db.execSQL(
            """
            CREATE TABLE Titles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                obtained INTEGER NOT NULL DEFAULT 1
            );
            """
        )
        db.execSQL(
            """
            CREATE TABLE Classes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                class TEXT NOT NULL,
                obtained INTEGER NOT NULL DEFAULT 0
            );
            """
        )
        db.execSQL(
            """
        CREATE TABLE Avatars (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            resource TEXT NOT NULL,
            cost INTEGER NOT NULL,
            obtained INTEGER NOT NULL DEFAULT 0
        );
        """
        )
        db.execSQL(
            """
        CREATE TABLE Backgrounds (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            resource TEXT NOT NULL,
            cost INTEGER NOT NULL,
            obtained INTEGER NOT NULL DEFAULT 0
        );
        """
        )
        db.execSQL(
            """
            CREATE TABLE TaskLogs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                taskId INTEGER NOT NULL,
                loggedDate TEXT NOT NULL,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(taskId) REFERENCES Tasks(id)
            );
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Users")
        db.execSQL("DROP TABLE IF EXISTS Tasks")
        db.execSQL("DROP TABLE IF EXISTS Titles")
        db.execSQL("DROP TABLE IF EXISTS Classes")
        db.execSQL("DROP TABLE IF EXISTS Backgrounds")
        db.execSQL("DROP TABLE IF EXISTS Avatars")
        db.execSQL("DROP TABLE IF EXISTS TaskLogs")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "QuestLog.db"
        private const val DATABASE_VERSION = 1
    }

    fun getUser(): User {
        val cursor = readableDatabase.rawQuery("SELECT * FROM Users LIMIT 1", null)
        return cursor.use {
            if (it.moveToFirst()) {
                User(
                    id = it.getInt(it.getColumnIndexOrThrow("id")),
                    username = it.getString(it.getColumnIndexOrThrow("username")),
                    level = it.getInt(it.getColumnIndexOrThrow("level")),
                    exp = it.getInt(it.getColumnIndexOrThrow("exp")),
                    coins = it.getInt(it.getColumnIndexOrThrow("coins")),
                    equippedAvatarId = it.getInt(it.getColumnIndexOrThrow("equippedAvatarId")),
                    equippedBackgroundId = it.getInt(it.getColumnIndexOrThrow("equippedBackgroundId")),
                    equippedTitleId = it.getInt(it.getColumnIndexOrThrow("equippedTitleId")),
                    equippedClassId = it.getInt(it.getColumnIndexOrThrow("equippedClassId")),
                    avatarIds = getIdsFromTable("Avatars", 1),
                    backgroundIds = getIdsFromTable("Backgrounds", 1),
                    titleIds = getIdsFromTable("Titles", 1),
                    classIds = getIdsFromTable("Classes", 1)
                )
            } else {
                User(
                    username = "New User",
                    isNew = true
                )
            }
        }
    }

    fun getIdsFromTable(tableName: String, obtained: Int): List<Int> {
        val ids = mutableListOf<Int>()
        val cursor = readableDatabase.rawQuery("SELECT id FROM $tableName WHERE obtained = $obtained", null)
        cursor.use {
            while (it.moveToNext()) {
                ids.add(it.getInt(it.getColumnIndexOrThrow("id")))
            }
        }
        return ids
    }

    fun getTaskById(taskId: Int): Task {
        val query = "SELECT * FROM Tasks WHERE id = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(taskId.toString()))

        return cursor.use {
            if (it.moveToFirst()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val currentLogId = it.getInt(it.getColumnIndexOrThrow("currentLogId"))
                val type = it.getString(it.getColumnIndexOrThrow("type"))
                val description = it.getString(it.getColumnIndexOrThrow("description"))
                val repeat = it.getInt(it.getColumnIndexOrThrow("repeat"))

                Task(
                    id = id,
                    currentLogId = currentLogId,
                    type = type,
                    description = description,
                    repeat = repeat
                )
            } else {
                throw IllegalStateException("Task with id $taskId not found")
            }
        }
    }

    fun getTasks(isCompleted: Int): List<Task> {
        val tasks = mutableListOf<Task>()
        val queryTasks = "SELECT * FROM Tasks"
        val cursorTasks = readableDatabase.rawQuery(queryTasks, null)
        cursorTasks.use { it ->
            while (it.moveToNext()) {
                val taskId = it.getInt(it.getColumnIndexOrThrow("id"))
                val currentLogId = it.getInt(it.getColumnIndexOrThrow("currentLogId"))
                val type = it.getString(it.getColumnIndexOrThrow("type"))
                val description = it.getString(it.getColumnIndexOrThrow("description"))
                val repeat = it.getInt(it.getColumnIndexOrThrow("repeat"))
                val queryLogs = "SELECT * FROM TaskLogs WHERE taskId = $taskId AND id = $currentLogId"
                val cursorLogs = readableDatabase.rawQuery(queryLogs, null)

                cursorLogs.use {
                    if (it.moveToNext()) {
                        val logIsCompleted = it.getInt(it.getColumnIndexOrThrow("isCompleted"))
                        if (logIsCompleted == isCompleted) {
                            val task = Task(
                                id = taskId,
                                currentLogId = currentLogId,
                                type = type,
                                description = description,
                                repeat = repeat,
                                isCompleted = isCompleted
                            )
                            tasks.add(task)
                        }
                    }
                }
            }
        }
        tasks.sortBy {
            when (it.type) {
                "Daily" -> 1
                "Weekly" -> 2
                "Monthly" -> 3
                else -> 4
            }
        }
        return tasks
    }

    fun saveTask(task: Task, create: Boolean){
        val values = ContentValues().apply {
            put("type", task.type)
            put("currentLogId", task.currentLogId)
            put("description", task.description)
            put("repeat", task.repeat)
        }

        task.id = if (create) {
            writableDatabase.insert("Tasks", null, values).takeIf { it != -1L }?.toInt() ?: task.id
        } else {
            writableDatabase.update("Tasks", values, "id = ?", arrayOf(task.id.toString()))
        }
        ensureTaskLogValidity(task)
    }

    fun deleteTaskAndLogs(task: Task) {
        val deleteLogsQuery = "DELETE FROM TaskLogs WHERE taskId = ?"
        writableDatabase.execSQL(deleteLogsQuery, arrayOf(task.id.toString()))
        val deleteTaskQuery = "DELETE FROM Tasks WHERE id = ?"
        writableDatabase.execSQL(deleteTaskQuery, arrayOf(task.id.toString()))
    }

    private fun ensureTaskLogValidity(task: Task) {
        if (task.currentLogId == 0 || !isCurrentLogValid(task)) {
            task.currentLogId = logTask(task.id, true, 0)
            saveTask(task, false)
        }
    }

    private fun isCurrentLogValid(task: Task): Boolean {
        val query = "SELECT loggedDate FROM TaskLogs WHERE taskId = ? ORDER BY loggedDate DESC LIMIT 1"
        val cursor = writableDatabase.rawQuery(query, arrayOf(task.id.toString()))

        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex("loggedDate")

            if (columnIndex >= 0) {
                val loggedDateStr = cursor.getString(columnIndex)
                cursor.close()

                val loggedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(loggedDateStr)
                    ?: return false
                val today = Calendar.getInstance()

                return when (task.type) {
                    "Daily" -> isToday(loggedDate, today)
                    "Weekly" -> isWithinWeek(loggedDate, today)
                    "Monthly" -> isSameMonth(loggedDate, today)
                    else -> false
                }
            } else {
                cursor.close()
                return false
            }
        } else {
            cursor.close()
            return false
        }
    }

    private fun isToday(loggedDate: Date, today: Calendar): Boolean {
        val loggedCalendar = Calendar.getInstance().apply { time = loggedDate }
        return today.get(Calendar.YEAR) == loggedCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == loggedCalendar.get(Calendar.DAY_OF_YEAR)
    }

    private fun isWithinWeek(loggedDate: Date, today: Calendar): Boolean {
        val loggedCalendar = Calendar.getInstance().apply { time = loggedDate }
        val weekAfterLogged = Calendar.getInstance().apply {
            time = loggedDate
            add(Calendar.DAY_OF_YEAR, 7)
        }
        return today.time.after(loggedCalendar.time) && today.time.before(weekAfterLogged.time)
    }

    private fun isSameMonth(loggedDate: Date, today: Calendar): Boolean {
        val loggedCalendar = Calendar.getInstance().apply { time = loggedDate }
        return today.get(Calendar.YEAR) == loggedCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == loggedCalendar.get(Calendar.MONTH)
    }

    fun logTask(taskId: Int, create: Boolean, isCompleted: Int): Int {
        val values = ContentValues().apply {
            put("taskId", taskId)
            put("loggedDate", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            put("isCompleted", isCompleted)
        }

        return if (create) {
            writableDatabase.insert("TaskLogs", null, values).takeIf { it != -1L }?.toInt()
                ?: throw IllegalStateException("Failed to create a new log for task $taskId")
        } else {
            val rowsUpdated = writableDatabase.update(
                "TaskLogs",
                values,
                "taskId = ? AND strftime('%Y-%m-%d', loggedDate) = ?",
                arrayOf(taskId.toString(), SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            )
            if (rowsUpdated == 0) {
                throw IllegalStateException("Failed to update the log for task $taskId")
            }

            val task = getTaskById(taskId)
            val user = getUser()

            var expReward = 0
            var coinsReward = 0

            when (task.type) {
                "Daily" -> {
                    expReward = 1
                    coinsReward = 1
                }
                "Weekly" -> {
                    expReward = 10
                    coinsReward = 10
                }
                "Monthly" -> {
                    expReward = 100
                    coinsReward = 100
                }
            }

            val updatedExp = user.exp + expReward
            val updatedCoins = user.coins + coinsReward

            val userValues = ContentValues().apply {
                put("exp", updatedExp)
                put("coins", updatedCoins)
            }

            writableDatabase.update(
                "Users",
                userValues,
                "id = ?",
                arrayOf(user.id.toString())
            )

            if (updatedExp >= user.level * 25) {
                val updatedLevel = user.level + 1
                val levelValues = ContentValues().apply {
                    put("level", updatedLevel)
                }
                writableDatabase.update(
                    "Users",
                    levelValues,
                    "id = ?",
                    arrayOf(user.id.toString())
                )
            }
            taskId
        }
    }

    @SuppressLint("DefaultLocale")
    fun getTaskLogsForMonth(month: Int, year: Int): List<TaskLog> {
        val logs = mutableListOf<TaskLog>()
        val startOfMonth = String.format("%04d-%02d-01", year, month)
        val endOfMonth = String.format("%04d-%02d-%02d", year, month,
            Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }.get(Calendar.DAY_OF_MONTH)
        )

        val query = """
        SELECT * FROM TaskLogs 
        WHERE loggedDate BETWEEN ? AND ?
    """

        val cursor = readableDatabase.rawQuery(query, arrayOf(startOfMonth, endOfMonth))
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val taskId = it.getInt(it.getColumnIndexOrThrow("taskId"))
                val loggedDate = it.getString(it.getColumnIndexOrThrow("loggedDate"))
                val isCompleted = it.getInt(it.getColumnIndexOrThrow("isCompleted"))

                logs.add(TaskLog(
                    id = id,
                    taskId = taskId,
                    loggedDate = loggedDate,
                    isCompleted = isCompleted
                ))
            }
        }
        return logs
    }
}
