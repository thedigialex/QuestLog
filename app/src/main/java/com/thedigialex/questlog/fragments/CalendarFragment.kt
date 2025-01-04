package com.thedigialex.questlog.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.adapters.CalendarAdapter
import com.thedigialex.questlog.controllers.UserController
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment(private val userController: UserController) : Fragment() {

    private lateinit var calendar: Calendar
    private lateinit var dayDetailLayout: LinearLayout
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var monthTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        monthTextView = view.findViewById(R.id.tvMonth)
        dayDetailLayout = view.findViewById(R.id.dayDetailLayout)
        calendar = Calendar.getInstance()

        view.findViewById<Button>(R.id.btnDecreaseMonth).setOnClickListener { changeMonth(-1) }
        view.findViewById<Button>(R.id.btnIncreaseMonth).setOnClickListener { changeMonth(1) }
        view.findViewById<Button>(R.id.btnCloseDayDetail).setOnClickListener { closeDayDetails() }
        return view
    }

    override fun onResume() {
        super.onResume()
        updateCalendar()
    }

    private fun updateCalendar() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        calendar.set(year, month, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        monthTextView.text = SimpleDateFormat("MMMM\nyyyy", Locale.getDefault()).format(calendar.time)

        val taskLogs = userController.dbHelper.getTaskLogsForMonth(month + 1, year)
        val groupedLogs = taskLogs.groupBy { it.loggedDate }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val days = mutableListOf<CalendarAdapter.DayItem>()

        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        val todayMonth = today.get(Calendar.MONTH)
        val todayYear = today.get(Calendar.YEAR)

        repeat(firstDayOfWeek) {
            days.add(CalendarAdapter.DayItem(null, emptyList(), false))
        }

        for (day in 1..daysInMonth) {
            val date = String.format("%04d-%02d-%02d", year, month + 1, day)
            val isToday = day == todayDay && month == todayMonth && year == todayYear
            days.add(CalendarAdapter.DayItem(day, groupedLogs[date] ?: emptyList(), isToday))
        }

        calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarRecyclerView.adapter = CalendarAdapter(days) { dayItem ->
            openDayDetails(dayItem)
        }
    }

    private fun openDayDetails(dayItem: CalendarAdapter.DayItem) {
        calendarRecyclerView.visibility = View.GONE
        dayDetailLayout.visibility = View.VISIBLE
        val tvDayTitle = dayDetailLayout.findViewById<TextView>(R.id.tvDayTitle)
        val tvDayContent = dayDetailLayout.findViewById<TextView>(R.id.tvDayContent)
        if (dayItem.day != null) {
            tvDayTitle.text = "Day ${dayItem.day}"

            val taskDescriptions = if (dayItem.taskLogs.isNotEmpty()) {
                dayItem.taskLogs.joinToString(separator = "\n\n") { taskLog ->
                    "${if (taskLog.isCompleted == 1) "[X]" else "[_]"} ${taskLog.taskName}"
                }
            } else {
                "No tasks for this day."
            }
            tvDayContent.text = taskDescriptions
        }
    }

    private fun changeMonth(offset: Int) {
        calendar.add(Calendar.MONTH, offset)
        updateCalendar()
    }

    private fun closeDayDetails(){
        dayDetailLayout.visibility = View.GONE
        calendarRecyclerView.visibility = View.VISIBLE
    }
}