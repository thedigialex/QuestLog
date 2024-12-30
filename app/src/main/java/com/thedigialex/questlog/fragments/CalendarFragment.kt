package com.thedigialex.questlog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.adapters.CalendarAdapter
import com.thedigialex.questlog.controllers.UserController
import com.thedigialex.questlog.models.TaskLog
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment(private val userController: UserController) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        val calendarRecyclerView: RecyclerView = view.findViewById(R.id.calendarRecyclerView)
        val monthTextView: TextView = view.findViewById(R.id.monthTextView)

        val taskLogs = userController.dbHelper.getTaskLogsForMonth(12, 2024)
        val groupedLogs = taskLogs.groupBy { it.loggedDate }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, 11) // December (0-based index)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        monthTextView.text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-based index for GridLayout
        val totalCells = daysInMonth + firstDayOfWeek

        val days = mutableListOf<CalendarAdapter.DayItem>()

        // Add empty days for padding
        repeat(firstDayOfWeek) {
            days.add(CalendarAdapter.DayItem(null, emptyList()))
        }

        // Add actual days
        for (day in 1..daysInMonth) {
            val date = String.format("%04d-%02d-%02d", 2024, 12, day)
            days.add(CalendarAdapter.DayItem(day, groupedLogs[date] ?: emptyList()))
        }

        calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7) // 7 columns for a week
        calendarRecyclerView.adapter = CalendarAdapter(days)

        return view
    }
}

