package com.thedigialex.questlog.controllers

import android.content.Intent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.thedigialex.questlog.R
import com.thedigialex.questlog.activity.DashboardActivity
import com.thedigialex.questlog.models.User
import com.thedigialex.questlog.database.QuestLogDatabaseHelper
import com.thedigialex.questlog.models.Item

class UserController(private val activity: AppCompatActivity, private val layoutView: View, private val headerLayout: LinearLayout?) {

    var dbHelper: QuestLogDatabaseHelper = QuestLogDatabaseHelper(activity)
    var currentUser: User = dbHelper.getUser()

    init {
        setUpLayoutValues()
        setUpHeaderValues()
    }

    private fun setUpHeaderValues() {
        if(headerLayout != null) {
            val tvUserName = headerLayout.findViewById<TextView>(R.id.tvUsername)
            tvUserName?.text = currentUser.username
            val tvTitle = headerLayout.findViewById<TextView>(R.id.tvTitle)
            if (currentUser.equippedTitleId != 0) {
                val title = currentUser.titleObtained.find { it.id == currentUser.equippedTitleId }
                tvTitle.text = title?.resource ?: ""
            } else {
                tvTitle?.text = ""
            }
            val ivAvatar = headerLayout.findViewById<ImageView>(R.id.ivAvater)

            if (currentUser.equippedAvatarId != 0) {
                val avatarItem = currentUser.avatarObtained.find { it.id == currentUser.equippedAvatarId }

                if (avatarItem != null) {
                    ivAvatar.setImageResource(avatarItem.imageValue)
                }
            } else {
                ivAvatar.setImageResource(R.drawable.avatar_zero)
            }
        }
    }

    fun setUpLayoutValues() {
        var visibility = View.GONE
        if (!currentUser.isNew) {
            visibility = View.VISIBLE
            pullInItems()
        }

        layoutView.findViewById<View>(R.id.imageLayout).visibility = visibility
        layoutView.findViewById<View>(R.id.titleLayout).visibility = visibility
        val spinnerClass: Spinner = layoutView.findViewById(R.id.spinnerClass)
        spinnerClass.visibility = View.GONE
        val editUsername: EditText = layoutView.findViewById(R.id.editUsername)
        editUsername.setText(currentUser.username)
        val spinnerAvatar: Spinner = layoutView.findViewById(R.id.spinnerAvatar)
        val imageView: ImageView = layoutView.findViewById(R.id.imageView)
        val avatarNames = listOf("Default") + currentUser.avatarObtained.map { it.resource }
        val avatarImages = listOf(0) + currentUser.avatarObtained.map { it.imageValue }

        val avatarAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, avatarNames)
        avatarAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAvatar.adapter = avatarAdapter
        val avatarPosition = if (currentUser.equippedAvatarId == 0) {
            0
        } else {
            currentUser.avatarObtained.indexOfFirst { it.id == currentUser.equippedAvatarId } + 1
        }
        spinnerAvatar.setSelection(avatarPosition)
        spinnerAvatar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position == 0) {
                    imageView.setImageResource(R.drawable.avatar_zero)
                } else {
                val selectedImageResId = avatarImages[position]
                imageView.setImageResource(selectedImageResId)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                imageView.setImageResource(R.drawable.avatar_zero)
            }
        }
        val spinnerTitle: Spinner = layoutView.findViewById(R.id.spinnerTitle)
        val titleNames = listOf("Select a title") + currentUser.titleObtained.map { it.resource }
        val titleAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, titleNames)
        titleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTitle.adapter = titleAdapter
        val titlePosition = if (currentUser.equippedTitleId == 0) {
            0
        } else {
            currentUser.titleObtained.indexOfFirst { it.id == currentUser.equippedTitleId } + 1
        }
        spinnerTitle.setSelection(titlePosition)

        val classNames = listOf("") + currentUser.classObtained.map { it.resource }
        val classAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, classNames)
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClass.adapter = classAdapter
        val classPosition = if (currentUser.equippedClassId == 0) {
            0
        } else {
            currentUser.classObtained.indexOfFirst { it.id == currentUser.equippedClassId } + 1
        }
        spinnerClass.setSelection(classPosition)

        val spinnerBackground: Spinner = layoutView.findViewById(R.id.spinnerBackground)
        val backgroundNames = listOf("") + currentUser.backgroundObtained.map { it.resource }
        val backgroundAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, backgroundNames)
        backgroundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBackground.adapter = backgroundAdapter
        val backgroundPosition = if (currentUser.equippedBackgroundId == 0) {
            0
        } else {
            currentUser.backgroundObtained.indexOfFirst { it.id == currentUser.equippedBackgroundId } + 1
        }
        spinnerBackground.setSelection(backgroundPosition)

        val saveButton: Button = layoutView.findViewById(R.id.btnSaveChanges)
        saveButton.setOnClickListener { saveUserData() }
    }

    private fun saveUserData() {
        currentUser.username = layoutView.findViewById<EditText>(R.id.editUsername).text.toString().trim()
        if (currentUser.username.isBlank()) {
            Toast.makeText(activity, "Please fill in all required fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val spinnerAvatar: Spinner = layoutView.findViewById(R.id.spinnerAvatar)
        val selectedAvatarName = spinnerAvatar.selectedItem as String
        if (selectedAvatarName != "Default") {
            currentUser.equippedAvatarId = currentUser.avatarObtained.first { it.resource == selectedAvatarName }.id
        } else {
            currentUser.equippedAvatarId = 0
        }

        val spinnerTitle: Spinner = layoutView.findViewById(R.id.spinnerTitle)
        val selectedTitleName = spinnerTitle.selectedItem as String
        if (selectedTitleName != "Select a title") {
            currentUser.equippedTitleId = currentUser.titleObtained.first { it.resource == selectedTitleName }.id
        } else {
            currentUser.equippedTitleId = 0
        }

        val spinnerClass: Spinner = layoutView.findViewById(R.id.spinnerClass)
        val selectedClassName = spinnerClass.selectedItem as String
        if (selectedClassName.isNotEmpty()) {
            currentUser.equippedClassId = currentUser.classObtained.first { it.resource == selectedClassName }.id
        } else {
            currentUser.equippedClassId = 0
        }

        val spinnerBackground: Spinner = layoutView.findViewById(R.id.spinnerBackground)
        val selectedBackgroundName = spinnerBackground.selectedItem as String
        if (selectedBackgroundName.isNotEmpty()) {
            currentUser.equippedBackgroundId = currentUser.backgroundObtained.first { it.resource == selectedBackgroundName }.id
        } else {
            currentUser.equippedBackgroundId = 0
        }

        setUpHeaderValues()
        layoutView.visibility = View.GONE
        if (dbHelper.saveUser(currentUser)) {
            navigateToDashboard()
        }
    }

    private fun pullInItems() {
        val obtainedItems = dbHelper.getItems(1)
        val avatars = mutableListOf<Item>()
        val backgrounds = mutableListOf<Item>()
        val titles = mutableListOf<Item>()
        val classes = mutableListOf<Item>()

        for (item in obtainedItems) {
            when (item.type) {
                "Avatar" -> avatars.add(item)
                "Background" -> backgrounds.add(item)
                "Title" -> titles.add(item)
                "Class" -> classes.add(item)
            }
        }
        currentUser.avatarObtained = avatars
        currentUser.backgroundObtained = backgrounds
        currentUser.classObtained = classes
        currentUser.titleObtained = titles
    }

    fun navigateToDashboard() {
        val intent = Intent(activity, DashboardActivity::class.java)
        activity.startActivity(intent)
        activity.finish()
    }
}
