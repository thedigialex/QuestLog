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

class UserController(private val activity: AppCompatActivity, private val layoutView: View, private val tvUserName: TextView?) {

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

        val editUsername: EditText = layoutView.findViewById(R.id.editUsername)
        editUsername.setText(currentUser.username)
        if(tvUserName != null) {
            tvUserName.setText(currentUser.username)
        }

        // Populate the avatar spinner
        val spinnerAvatar: Spinner = layoutView.findViewById(R.id.spinnerAvatar)
        val avatarAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, currentUser.avatarIds.map { "Avatar $it" })
        avatarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAvatar.adapter = avatarAdapter
        val avatarPosition = currentUser.avatarIds.indexOf(currentUser.equippedAvatarId)
        if (avatarPosition >= 0) {
            spinnerAvatar.setSelection(avatarPosition)
        }

        // Populate the title spinner
        val spinnerTitle: Spinner = layoutView.findViewById(R.id.spinnerTitle)
        val titleAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, currentUser.titleIds.map { "Title $it" })
        titleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTitle.adapter = titleAdapter
        val titlePosition = currentUser.titleIds.indexOf(currentUser.equippedTitleId)
        if (titlePosition >= 0) {
            spinnerTitle.setSelection(titlePosition)
        }

        // Populate the class spinner
        val spinnerClass: Spinner = layoutView.findViewById(R.id.spinnerClass)
        val classAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, currentUser.classIds.map { "Class $it" })
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClass.adapter = classAdapter
        val classPosition = currentUser.classIds.indexOf(currentUser.equippedClassId)
        if (classPosition >= 0) {
            spinnerClass.setSelection(classPosition)
        }

        // Populate the background spinner
        val spinnerBackground: Spinner = layoutView.findViewById(R.id.spinnerBackground)
        val backgroundAdapter = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, currentUser.backgroundIds.map { "Background $it" })
        backgroundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBackground.adapter = backgroundAdapter
        val backgroundPosition = currentUser.backgroundIds.indexOf(currentUser.equippedBackgroundId)
        if (backgroundPosition >= 0) {
            spinnerBackground.setSelection(backgroundPosition)
        }

        // Set the click listener for the Save button
        val saveButton: Button = layoutView.findViewById(R.id.btnSaveChanges)
        saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun saveUserData() {
        currentUser.username = layoutView.findViewById<EditText>(R.id.editUsername).text.toString().trim()
        if (currentUser.username.isBlank()) {
            Toast.makeText(activity, "Please fill in all required fields!", Toast.LENGTH_SHORT).show()
            return
        }
        tvUserName?.setText(currentUser.username)
        updateUserRow()
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
