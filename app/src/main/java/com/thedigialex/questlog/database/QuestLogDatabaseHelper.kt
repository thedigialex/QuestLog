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
                CREATE TABLE Items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    resource TEXT NOT NULL,
                    imageValue INTEGER,
                    cost INTEGER NOT NULL,
                    levelRequired INTEGER NOT NULL,
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
        db.execSQL(
            """
        CREATE TABLE Notes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT,
            description TEXT,
            sortOrder INTEGER NOT NULL DEFAULT 0
        );
        """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Users")
        db.execSQL("DROP TABLE IF EXISTS Tasks")
        db.execSQL("DROP TABLE IF EXISTS Items")
        db.execSQL("DROP TABLE IF EXISTS TaskLogs")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "QuestLog.db"
        private const val DATABASE_VERSION = 1
    }

    fun saveUser(currentUser: User): Boolean {
        val values = ContentValues().apply {
            put("username", currentUser.username)
            put("level", currentUser.level)
            put("exp", currentUser.exp)
            put("coins", currentUser.coins)
            put("equippedAvatarId", currentUser.equippedAvatarId)
            put("equippedBackgroundId", currentUser.equippedBackgroundId)
            put("equippedTitleId", currentUser.equippedTitleId)
            put("equippedClassId", currentUser.equippedClassId)
        }

        if (currentUser.id == 0) {
            writableDatabase.insert("Users", null, values)
            return true
        } else {
            writableDatabase.update("Users", values, "id = ?", arrayOf(currentUser.id.toString()))
            return false
        }
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
                )
            } else {
                User(
                    username = "New User",
                    isNew = true
                )
            }
        }
    }

    fun getTasks(isCompleted: Int, getRepeat: Int = 0): List<Task> {
        val tasks = mutableListOf<Task>()

        val queryTasks = "SELECT * FROM Tasks"
        val cursorTasks = readableDatabase.rawQuery(queryTasks, null)

        cursorTasks.use { cursor ->
            while (cursor.moveToNext()) {
                val taskId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val currentLogId = cursor.getInt(cursor.getColumnIndexOrThrow("currentLogId"))
                val type = cursor.getString(cursor.getColumnIndexOrThrow("type"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val repeat = cursor.getInt(cursor.getColumnIndexOrThrow("repeat"))

                val queryLogs = """
                SELECT * FROM TaskLogs 
                WHERE taskId = ? AND id = ?
            """
                val cursorLogs = readableDatabase.rawQuery(queryLogs, arrayOf(taskId.toString(), currentLogId.toString()))

                cursorLogs.use { logCursor ->
                    if (logCursor.moveToNext()) {
                        val logIsCompleted = logCursor.getInt(logCursor.getColumnIndexOrThrow("isCompleted"))

                        if ((getRepeat == 1 && repeat == getRepeat) ||
                            (getRepeat != 1 && logIsCompleted == isCompleted)) {

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

        if (create) {
            val newId = writableDatabase.insert("Tasks", null, values).takeIf { it != -1L }?.toInt() ?: task.id
            task.id = newId
            ensureTaskLogValidity(task)
        } else {
            writableDatabase.update("Tasks", values, "id = ?", arrayOf(task.id.toString()))
        }
    }

    fun deleteTaskAndLogs(task: Task) {
        val deleteLogsQuery = "DELETE FROM TaskLogs WHERE taskId = ?"
        writableDatabase.execSQL(deleteLogsQuery, arrayOf(task.id.toString()))
        val deleteTaskQuery = "DELETE FROM Tasks WHERE id = ?"
        writableDatabase.execSQL(deleteTaskQuery, arrayOf(task.id.toString()))
    }

    fun logTask(task: Task, create: Boolean) {
        if (create) {
            val values = ContentValues().apply {
                put("taskId", task.id)
                put("loggedDate", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                put("isCompleted", 0)
            }
            task.currentLogId = writableDatabase.insert("TaskLogs", null, values).takeIf { it != -1L }?.toInt() ?: 0
            saveTask(task, false)
        } else {
            val taskLog = getTaskLogById(task.currentLogId)
            val values = ContentValues().apply {
                put("id", taskLog.id)
                put("loggedDate", taskLog.loggedDate)
                put("isCompleted", 1)
            }
            writableDatabase.update(
                "TaskLogs",
                values,
                "id = ? AND strftime('%Y-%m-%d', loggedDate) = ?",
                arrayOf(taskLog.id.toString(), SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
            )

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
            //To Do fix for monthly exp gained
            //Need to add a loop

            user.coins += coinsReward
            user.exp += expReward
            if (user.exp >= user.level * 25) {
                user.level +=  1
                user.exp -= user.level * 25

                if (user.exp < 0) {
                    user.exp = 0
                }
            }
            saveUser(user)
        }
    }

    fun checkLogDateForRepeating() {
        val tasks = getTasks(0, 1)
        for (task in tasks) {
            ensureTaskLogValidity(task)
        }
    }

    fun getNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM Notes ORDER BY `sortOrder` ASC", null)
        cursor.use {
            while (it.moveToNext()) {
                notes.add(
                    Note(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        description = it.getString(it.getColumnIndexOrThrow("description")),
                        sortOrder = it.getInt(it.getColumnIndexOrThrow("sortOrder"))
                    )
                )
            }
        }
        return notes
    }

    fun saveNote(note: Note, create: Boolean) {
        val values = ContentValues().apply {
            put("title", note.title)
            put("description", note.description)
            put("sortOrder", note.sortOrder)
        }

        if (create) {
            val newId = writableDatabase.insert("Notes", null, values).takeIf { it != -1L }?.toInt() ?: note.id
            note.id = newId
        } else {
            writableDatabase.update("Notes", values, "id = ?", arrayOf(note.id.toString()))
        }
    }

    fun deleteNote(note: Note) {
        writableDatabase.delete("Notes", "id = ?", arrayOf(note.id.toString()))
    }

    fun getItems(obtained: Int): List<Item> {
        val items = mutableListOf<Item>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM Items WHERE obtained = ?",
            arrayOf(obtained.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                items.add(
                    Item(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        type = it.getString(it.getColumnIndexOrThrow("type")),
                        resource = it.getString(it.getColumnIndexOrThrow("resource")),
                        imageValue = it.getInt(it.getColumnIndexOrThrow("imageValue")),
                        cost = it.getInt(it.getColumnIndexOrThrow("cost")),
                        levelRequired = it.getInt(it.getColumnIndexOrThrow("levelRequired")),
                        obtained = it.getInt(it.getColumnIndexOrThrow("obtained"))
                    )
                )
            }
        }
        return items
    }

    fun saveItem(item: Item, create: Boolean) {
        val values = ContentValues().apply {
            put("type", item.type)
            put("resource", item.resource)
            put("imageValue", item.imageValue)
            put("cost", item.cost)
            put("levelRequired", item.levelRequired)
            put("obtained", if (create) 0 else 1)
        }

        if (create) {
            val newId = writableDatabase.insert("Items", null, values).takeIf { it != -1L }?.toInt() ?: item.id
            item.id = newId
        } else {
            val user = getUser()
            user.coins -= item.cost
            saveUser(user)
            writableDatabase.update("Items", values, "id = ?", arrayOf(item.id.toString()))
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
        SELECT TaskLogs.id, TaskLogs.taskId, TaskLogs.loggedDate, TaskLogs.isCompleted, 
               Tasks.description AS taskDescription
        FROM TaskLogs
        LEFT JOIN Tasks ON TaskLogs.taskId = Tasks.id
        WHERE loggedDate BETWEEN ? AND ?
    """
        val cursor = readableDatabase.rawQuery(query, arrayOf(startOfMonth, endOfMonth))
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getInt(it.getColumnIndexOrThrow("id"))
                val taskId = it.getInt(it.getColumnIndexOrThrow("taskId"))
                val loggedDate = it.getString(it.getColumnIndexOrThrow("loggedDate"))
                val isCompleted = it.getInt(it.getColumnIndexOrThrow("isCompleted"))
                val taskDescription = it.getString(it.getColumnIndexOrThrow("taskDescription")) ?: "No Description"

                logs.add(TaskLog(
                    id = id,
                    taskId = taskId,
                    loggedDate = loggedDate,
                    isCompleted = isCompleted,
                    taskName = taskDescription
                ))
            }
        }
        return logs
    }

    private fun getIdsFromTable(tableName: String, obtained: Int): List<Int> {
        val ids = mutableListOf<Int>()
        val cursor = readableDatabase.rawQuery("SELECT id FROM $tableName WHERE obtained = $obtained", null)
        cursor.use {
            while (it.moveToNext()) {
                ids.add(it.getInt(it.getColumnIndexOrThrow("id")))
            }
        }
        return ids
    }

    private fun ensureTaskLogValidity(task: Task) {
        if (task.currentLogId == 0 || !isCurrentLogValid(task)) {
            logTask(task, true)
        }
    }

    @SuppressLint("SimpleDateFormat")
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
                val today = Calendar.getInstance().time
                return when (task.type) {
                    "Daily" -> isTodayOrMonth(loggedDate, today, SimpleDateFormat("yyyyMMdd"))
                    "Weekly" -> isWithinWeek(loggedDate, today)
                    "Monthly" -> isTodayOrMonth(loggedDate, today, SimpleDateFormat("yyyyMM"))
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

    private fun isTodayOrMonth(loggedDate: Date, today: Date, formatter: SimpleDateFormat): Boolean {
        return formatter.format(loggedDate) == formatter.format(today)
    }

    @SuppressLint("SimpleDateFormat")
    private fun isWithinWeek(loggedDate: Date, today: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = loggedDate
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val sevenDaysLater = calendar.time
        return today.after(loggedDate) && today.before(sevenDaysLater) || isTodayOrMonth(loggedDate, today,SimpleDateFormat("yyyyMMdd"))
    }

    private fun getTaskLogById(taskLogId: Int): TaskLog {
        val query = "SELECT * FROM TaskLogs WHERE id = ?"
        val cursor = readableDatabase.rawQuery(query, arrayOf(taskLogId.toString()))

        cursor.moveToFirst()
        val id = cursor.getColumnIndex("id")
        val taskIdIndex = cursor.getColumnIndex("taskId")
        val loggedDateIndex = cursor.getColumnIndex("loggedDate")
        val isCompletedIndex = cursor.getColumnIndex("isCompleted")

        val taskLog = TaskLog(
            id = cursor.getInt(id),
            taskId = cursor.getInt(taskIdIndex),
            loggedDate = cursor.getString(loggedDateIndex),
            isCompleted = cursor.getInt(isCompletedIndex)
        )
        cursor.close()
        return taskLog
    }
}
