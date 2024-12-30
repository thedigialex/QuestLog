package com.thedigialex.questlog.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.thedigialex.questlog.controllers.UserController
import com.thedigialex.questlog.fragments.CalendarFragment
import com.thedigialex.questlog.fragments.TaskListFragment

class DashboardPagerAdapter(activity: AppCompatActivity, private val userController: UserController) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TaskListFragment(userController)
            1 -> CalendarFragment(userController)
            2 -> TaskListFragment(userController)
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}