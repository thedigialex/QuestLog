package com.thedigialex.questlog.controllers

import android.content.ContentValues
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.thedigialex.questlog.R
import com.thedigialex.questlog.activity.DashboardActivity
import com.thedigialex.questlog.models.User
import com.thedigialex.questlog.database.QuestLogDatabaseHelper

class UserController(private val activity: AppCompatActivity, private val layoutView: View) {

    var dbHelper: QuestLogDatabaseHelper = QuestLogDatabaseHelper(activity)
    var currentUser: User = dbHelper.getUser()

    init {
        setUpLayoutValues()
    }

    private fun setUpLayoutValues() {
        val visibility = if (!currentUser.isNew) View.VISIBLE else View.GONE
        layoutView.findViewById<View>(R.id.imageLayout).visibility = visibility
        layoutView.findViewById<View>(R.id.titleLayout).visibility = visibility
        layoutView.findViewById<View>(R.id.spinnerBackground).visibility = visibility
        val saveButton: Button = layoutView.findViewById(R.id.btnSaveChanges)
        saveButton.setOnClickListener{
            saveUserData()
        }
    }

    private fun saveUserData() {
        currentUser.username = layoutView.findViewById<EditText>(R.id.editUsername).text.toString().trim()

        if (currentUser.username.isBlank()) {
            Toast.makeText(activity, "Please fill in all required fields!", Toast.LENGTH_SHORT).show()
            return
        }

        updateUserRow()
        Toast.makeText(activity, "User data saved successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun updateUserRow() {
        val db = dbHelper.writableDatabase
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
            val result = db.insert("Users", null, values)
            if (result != -1L) {
                navigateToDashboard()
            }
        } else {
            db.update("Users", values, "id = ?", arrayOf(currentUser.id.toString()))
        }
    }

    fun navigateToDashboard() {
        val intent = Intent(activity, DashboardActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}
