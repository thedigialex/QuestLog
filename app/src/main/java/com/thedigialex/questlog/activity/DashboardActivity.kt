package com.thedigialex.questlog.activity

import android.os.Bundle
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        userController = UserController(this, findViewById<View?>(R.id.userLayout).findViewById(R.id.userLayout), findViewById(R.id.headerLayout))
        userController.dbHelper.checkLogDateForRepeating()
        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val adapter = DashboardPagerAdapter(this, userController)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Tasks"
                1 -> tab.text = "Calendar"
                2 -> tab.text = "Shop"
                3 -> tab.text = "Notes"
            }
        }.attach()
        createNewItems()
    }

    fun switchVisibilityOfPlayerSettings(view: View) {
        val playerSettingsMenu = findViewById<View>(R.id.userLayout)
        if (playerSettingsMenu.visibility == View.VISIBLE) {
            playerSettingsMenu.visibility = View.GONE
        } else {
            playerSettingsMenu.visibility = View.VISIBLE
        }
    }

    private fun createNewItems() {

        val obtainedItems = userController.dbHelper.getItems(0)
        val notObtainedItems = userController.dbHelper.getItems(1)

        val avatarImageResourceId: Int = R.drawable.avaterimagetest
        val totalItems = listOf(
            Item(type = "Avatar", resource = "Dino One", imageValue = avatarImageResourceId, cost = 500, levelRequired = 5),
            Item(type = "Title", resource = "The Guy", cost = 50, levelRequired = 1)
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
