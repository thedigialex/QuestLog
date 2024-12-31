package com.thedigialex.questlog.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.thedigialex.questlog.R
import com.thedigialex.questlog.controllers.UserController

class MainActivity : AppCompatActivity() {

    private lateinit var userController: UserController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val playerSettingsMenu = findViewById<View>(R.id.userLayout)
        userController =  UserController(this, playerSettingsMenu.findViewById(R.id.userLayout), null)
        if (!userController.currentUser.isNew) {
            userController.navigateToDashboard()
        }
        else{
            playerSettingsMenu.visibility = View.VISIBLE
        }
    }
}
