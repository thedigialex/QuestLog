package com.thedigialex.questlog.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.thedigialex.questlog.models.Category
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetTextI18n")
class CalendarFragment(private val userController: UserController) : Fragment() {
    private lateinit var calendar: Calendar
    private lateinit var dayDetailLayout: LinearLayout
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var calendarLayoutContainer: LinearLayout
    private lateinit var monthTextView: TextView
    private lateinit var tvCalendarTransactionList: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        monthTextView = view.findViewById(R.id.tvMonth)
        tvCalendarTransactionList = view.findViewById(R.id.tvCalendarTransactionList)
        dayDetailLayout = view.findViewById(R.id.dayDetailLayout)
        calendarLayoutContainer = view.findViewById(R.id.calendarLayoutContainer)
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
        val groupedTaskLogs = taskLogs.groupBy { it.loggedDate }

        val transactions = userController.dbHelper.getTransactions(month + 1, year)
        val groupedTransactions = transactions.groupBy { it.timestamp }

        val incomeCategory = userController.dbHelper.getBankCategory("Income")
        val expenseCategory = userController.dbHelper.getBankCategory("Expense").toMutableList()
        expenseCategory.add(
            Category(
                id = 0,
                type = "Expense",
                name = "Borrow Payment",
                details = "Place holder",
                target_amount = 0,
                isNew = false
            )
        )
        val allCategories = incomeCategory + expenseCategory

        val groupedByCategory = transactions.groupBy { transaction ->
            when {
                transaction.type == "Borrow" -> "Borrowed"
                allCategories.find { category -> category.name == transaction.category } != null -> {
                    allCategories.find { category -> category.name == transaction.category }?.name ?: "Uncategorized"
                }
                else -> "Uncategorized"
            }
        }
        val totalAmountByCategory = groupedByCategory.mapValues { (categoryName, transactions) ->
            val totalAmount = transactions.sumOf { it.amount }
            val targetAmount = if (categoryName == "Borrowed") 0 else {
                val category = allCategories.find { it.name == categoryName }
                category?.target_amount ?: 0
            }
            Pair(totalAmount, targetAmount)
        }
        var transactionList = ""
        totalAmountByCategory.forEach { (category, amounts) ->
            if (category != "Uncategorized" && category != "Borrowed" && category != "Borrow Payment") {
                val (totalAmount, targetAmount) = amounts
                transactionList += "$category: $totalAmount / $targetAmount\n"
            }
            if (category == "Borrowed") {
                val borrowPayments = transactions.filter { it.category == "Borrow Payment" }
                val borrowPaymentTotal = borrowPayments.sumOf { it.amount }
                val borrowedTransactions = groupedByCategory["Borrowed"] ?: emptyList()
                val totalBorrowed = borrowedTransactions.sumOf { it.amount }
                val netBorrowed = totalBorrowed - borrowPaymentTotal
                transactionList += "Net Borrowed: $netBorrowed\n"
            }
        }
        tvCalendarTransactionList.text = transactionList

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val days = mutableListOf<CalendarAdapter.DayItem>()

        val today = Calendar.getInstance()
        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        val todayMonth = today.get(Calendar.MONTH)
        val todayYear = today.get(Calendar.YEAR)

        repeat(firstDayOfWeek) {
            days.add(CalendarAdapter.DayItem(null, emptyList(), emptyList(), false))
        }

        for (day in 1..daysInMonth) {
            val date = String.format("%04d-%02d-%02d", year, month + 1, day)
            val isToday = day == todayDay && month == todayMonth && year == todayYear
            days.add(
                CalendarAdapter.DayItem(
                    day,
                    groupedTaskLogs[date] ?: emptyList(),
                    groupedTransactions[date] ?: emptyList(),
                    isToday
                )
            )
        }

        calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
        calendarRecyclerView.adapter = CalendarAdapter(days) { dayItem ->
            openDayDetails(dayItem)
        }
    }

    private fun openDayDetails(dayItem: CalendarAdapter.DayItem) {
        calendarLayoutContainer.visibility = View.GONE
        dayDetailLayout.visibility = View.VISIBLE
        val tvDayTitle = dayDetailLayout.findViewById<TextView>(R.id.tvDayTitle)
        val tvDayContent = dayDetailLayout.findViewById<TextView>(R.id.tvDayContent)

        if (dayItem.day != null) {
            tvDayTitle.text = "Day ${dayItem.day}"

            val taskDescriptions = if (dayItem.taskLogs.isNotEmpty()) {
                dayItem.taskLogs.joinToString(separator = "\n\n") { taskLog ->
                    "${when (taskLog.isCompleted) {
                        1 -> "[X]"
                        0 -> "[_]"
                        2 -> "[O]"
                        else -> "[?]"
                    }} ${taskLog.taskName}"
                }
            } else {
                "No tasks for this day."
            }

            val transactionDescriptions = if (dayItem.transactions.isNotEmpty()) {
                dayItem.transactions.joinToString(separator = "\n") { transaction ->
                    "${when (transaction.type) {
                        "Income" -> "+"
                        "Expense" -> "-"
                        "Borrow" -> ""
                        else -> "[?]"
                    }} ${transaction.amount} (${transaction.category})"
                }
            } else {
                "No transactions for this day."
            }

            tvDayContent.text = taskDescriptions + "\n\n" + transactionDescriptions
        }
    }

    private fun changeMonth(offset: Int) {
        calendar.add(Calendar.MONTH, offset)
        updateCalendar()
    }

    private fun closeDayDetails(){
        dayDetailLayout.visibility = View.GONE
        calendarLayoutContainer.visibility = View.VISIBLE
    }
}