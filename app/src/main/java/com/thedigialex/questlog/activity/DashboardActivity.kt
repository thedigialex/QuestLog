package com.thedigialex.questlog.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.thedigialex.questlog.R
import com.thedigialex.questlog.adapters.DashboardPagerAdapter
import com.thedigialex.questlog.controllers.UserController
import com.thedigialex.questlog.models.Item

class DashboardActivity : AppCompatActivity() {

    private lateinit var userController: UserController
    private lateinit var btnPlayerSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        userController = UserController(this, findViewById<View?>(R.id.userLayout).findViewById(R.id.userLayout), findViewById(R.id.headerLayout))
        userController.dbHelper.checkLogDateForRepeating()
        btnPlayerSettings = findViewById(R.id.btnPlayerSettings)
        btnPlayerSettings.setOnClickListener { switchVisibilityOfPlayerSettings() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })
        createNewItems()
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val adapter = DashboardPagerAdapter(this, userController)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.ic_quest)
                1 -> tab.setIcon(R.drawable.ic_bank)
                2 -> tab.setIcon(R.drawable.ic_calendar)
                3 -> tab.setIcon(R.drawable.ic_store)
                4 -> tab.setIcon(R.drawable.ic_notes)
            }
        }.attach()
    }

    private fun switchVisibilityOfPlayerSettings() {
        val playerSettingsMenu = findViewById<View>(R.id.userLayout)
        if (playerSettingsMenu.visibility == View.VISIBLE) {
            playerSettingsMenu.visibility = View.GONE
            btnPlayerSettings.setBackgroundResource(R.drawable.menu_button)
        } else {
            userController.setUpLayoutValues()
            playerSettingsMenu.visibility = View.VISIBLE
            btnPlayerSettings.setBackgroundResource(R.drawable.close_button)
        }
    }

    private fun createNewItems() {
        val obtainedItems = userController.dbHelper.getItems(0)
        val notObtainedItems = userController.dbHelper.getItems(1)
        val avatarResources: IntArray = intArrayOf(
            R.drawable.avatar_one, R.drawable.avatar_two, R.drawable.avatar_three,
            R.drawable.avatar_four, R.drawable.avatar_five, R.drawable.avatar_six,
            R.drawable.avatar_seven, R.drawable.avatar_eight, R.drawable.avatar_nine,
            R.drawable.avatar_ten, R.drawable.avatar_eleven, R.drawable.avatar_twelve,
            R.drawable.avatar_thirteen, R.drawable.avatar_fourteen
        )
        val totalItems = listOf(
            Item(type = "Title", resource = "The Guy", cost = 50, levelRequired = 1),
            Item(type = "Title", resource = "First Flame", cost = 75, levelRequired = 2),
            Item(type = "Title", resource = "Starter Spark", cost = 75, levelRequired = 2),
            Item(type = "Title", resource = "Streak Seeker", cost = 100, levelRequired = 3),
            Item(type = "Title", resource = "Momentum Builder ", cost = 100, levelRequired = 3),
            Item(type = "Title", resource = "Pathfinder", cost = 100, levelRequired = 3),
            Item(type = "Title", resource = "Focus Finisher", cost = 150, levelRequired = 4),
            Item(type = "Title", resource = "Time Titan", cost = 150, levelRequired = 4),
            Item(type = "Title", resource = "The Conqueror", cost = 150, levelRequired = 4),
            Item(type = "Title", resource = "Daily Dreamer", cost = 150, levelRequired = 4),
            Item(type = "Title", resource = "Life Architect", cost = 200, levelRequired = 5),
            Item(type = "Title", resource = "Legacy Maker", cost = 200, levelRequired = 5),
            Item(type = "Title", resource = "Energy Alchemist", cost = 200, levelRequired = 5),
            Item(type = "Title", resource = "Phoenix Persevere", cost = 200, levelRequired = 5),
            Item(type = "Title", resource = "Procrastination Slayer", cost = 200, levelRequired = 5),
            Item(type = "Title", resource = "Master Strategist", cost = 1500, levelRequired = 10),

            Item(type = "Avatar", resource = "Dino One", imageValue = avatarResources[12], cost = 500, levelRequired = 1),
            Item(type = "Avatar", resource = "Dino Two", imageValue = avatarResources[13], cost = 500, levelRequired = 1),
            Item(type = "Avatar", resource = "Dino Three", imageValue = avatarResources[11], cost = 500, levelRequired = 1),
            Item(type = "Avatar", resource = "Dino Four", imageValue = avatarResources[0], cost = 1000, levelRequired = 2),
            Item(type = "Avatar", resource = "Dino Five", imageValue = avatarResources[1], cost = 1000, levelRequired = 2),
            Item(type = "Avatar", resource = "Dino Six", imageValue = avatarResources[2], cost = 1000, levelRequired = 2),
            Item(type = "Avatar", resource = "Dino Seven", imageValue = avatarResources[3], cost = 1500, levelRequired = 3),
            Item(type = "Avatar", resource = "Dino Eight", imageValue = avatarResources[4], cost = 1500, levelRequired = 3),
            Item(type = "Avatar", resource = "Dino Nine", imageValue = avatarResources[5], cost = 1500, levelRequired = 3),
            Item(type = "Avatar", resource = "Dino Ten", imageValue = avatarResources[6], cost = 1500, levelRequired = 3),
            Item(type = "Avatar", resource = "Dino Seven", imageValue = avatarResources[7], cost = 2000, levelRequired = 5),
            Item(type = "Avatar", resource = "Dino Eight", imageValue = avatarResources[8], cost = 2000, levelRequired = 5),
            Item(type = "Avatar", resource = "Dino Nine", imageValue = avatarResources[9], cost = 2000, levelRequired = 5),
            Item(type = "Avatar", resource = "Dino Ten", imageValue = avatarResources[10], cost = 2000, levelRequired = 5),
        )
        totalItems.forEach { item ->
            val isInObtained = obtainedItems.any { it.type == item.type && it.resource == item.resource }
            val isInNotObtained = notObtainedItems.any { it.type == item.type && it.resource == item.resource }

            if (!isInObtained && !isInNotObtained) {
                userController.dbHelper.saveItem(item, create = true)
            }
        }
    }
}
