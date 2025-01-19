package com.thedigialex.questlog.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.adapters.CategoryAdapter
import com.thedigialex.questlog.controllers.UserController
import com.thedigialex.questlog.models.Balance
import com.thedigialex.questlog.models.Category
import com.thedigialex.questlog.models.Task

class TransactionListFragment(private val userController: UserController) : Fragment() {

    private lateinit var transactionListView: ListView
    private lateinit var categoryListView: ListView
    private lateinit var transactionSection: View
    private lateinit var edtCurrentBalance: EditText
    private lateinit var tvExpense: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvBalance: TextView

    private lateinit var newTaskButton: Button
    private lateinit var btnEditClose: Button
    private lateinit var btnSettingSave: Button
    private lateinit var btnOpenSettings: Button
    private lateinit var btnSaveSetting: Button
    private lateinit var editSection: View
    private lateinit var settingSection: View
    private lateinit var taskDescriptionInput: EditText
    private lateinit var taskTypeSpinner: Spinner
    private lateinit var taskRepeatInput: CheckBox
    private lateinit var saveTaskButton: Button
    private lateinit var deleteTaskButton: Button

    private val taskTypes = listOf("Daily", "Weekly", "Monthly")

    private lateinit var incomeCategory: List<Category>
    private lateinit var expensesCategory: List<Category>
    private lateinit var balance: Balance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_list, container, false)

        tvExpense = view.findViewById(R.id.tvExpense)
        tvIncome = view.findViewById(R.id.tvIncome)
        tvBalance = view.findViewById(R.id.tvBalance)
        transactionListView = view.findViewById(R.id.transactionListView)
        categoryListView = view.findViewById(R.id.categoryListView)
        edtCurrentBalance = view.findViewById(R.id.edtCurrentBalance)
        transactionSection = view.findViewById(R.id.transactionSection)
        newTaskButton = view.findViewById(R.id.btnShowEdit)
        btnEditClose = view.findViewById(R.id.btnEditClose)
        btnSettingSave = view.findViewById(R.id.btnSettingSave)
        editSection = view.findViewById(R.id.editSection)
        settingSection = view.findViewById(R.id.settingSection)
        btnOpenSettings = view.findViewById(R.id.btnOpenSettings)
        btnSaveSetting = view.findViewById(R.id.btnSaveSetting)
        taskDescriptionInput = view.findViewById(R.id.edtTaskDescription)
        taskTypeSpinner = view.findViewById(R.id.spinnerTaskType)
        taskRepeatInput = view.findViewById(R.id.repeatBox)
        saveTaskButton = view.findViewById(R.id.btnSaveTask)
        deleteTaskButton = view.findViewById(R.id.btnDeleteTask)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            taskTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taskTypeSpinner.adapter = adapter

        getCategories()
        tvExpense.text = "0"
        tvIncome.text = "0"
        tvBalance.text = balance.current_balance.toString()

        newTaskButton.setOnClickListener { switchVisibilityOfEdit(Task(isNew = true)) }
        btnEditClose.setOnClickListener { switchVisibilityOfEdit(Task(isNew = true)) }
        btnOpenSettings.setOnClickListener { switchVisibilityOfSetting() }
        btnSettingSave.setOnClickListener { saveCurrentBalance() }
        //btnSaveSetting.setOnClickListener { addNewCategory(Category()) }

        return view
    }

    private fun getCategories() {
        incomeCategory = userController.dbHelper.getBankCategory("Income")
        expensesCategory = userController.dbHelper.getBankCategory("Expense")
        balance = userController.dbHelper.getBalance()
        val combinedCategories = mutableListOf<Category>().apply {
            addAll(incomeCategory)
            addAll(expensesCategory)
        }
        if (combinedCategories.isEmpty()) {
            categoryListView.adapter = null
        } else {
            val adapter = CategoryAdapter(requireContext(), combinedCategories) { selectedCategory -> addNewCategory(selectedCategory) }
            categoryListView.adapter = adapter
        }
    }

    private fun switchVisibilityOfEdit(task: Task) {
        val isSectionVisible = editSection.visibility == View.VISIBLE
        editSection.visibility = if (isSectionVisible) View.GONE else View.VISIBLE
        settingSection.visibility = View.GONE
        transactionSection.visibility = if (isSectionVisible) View.VISIBLE else View.GONE
        newTaskButton.visibility = if (isSectionVisible) View.VISIBLE else View.GONE
        deleteTaskButton.visibility = View.GONE
        if (editSection.visibility == View.VISIBLE) {
            setUpEditView(task)
        }
        if(!task.isNew) {
            deleteTaskButton.visibility = View.VISIBLE
            deleteTaskButton.setOnClickListener {
                switchVisibilityOfEdit(task)
                userController.dbHelper.deleteTaskAndLogs(task)
            }
        }
    }

    private fun switchVisibilityOfSetting() {
        val isSectionVisible = settingSection.visibility == View.VISIBLE
        btnOpenSettings.setBackgroundResource(
            if (isSectionVisible) R.drawable.select_button else R.drawable.close_button
        )
        edtCurrentBalance.setText(balance.current_balance.toString())

        settingSection.visibility = if (isSectionVisible) View.GONE else View.VISIBLE
        editSection.visibility = View.GONE
        transactionSection.visibility = if (isSectionVisible) View.VISIBLE else View.GONE
        newTaskButton.visibility = if (isSectionVisible) View.VISIBLE else View.GONE
        deleteTaskButton.visibility = View.GONE
    }

    private fun setUpEditView(task: Task) {
        taskDescriptionInput.setText(task.description)
        val index = taskTypes.indexOf(task.type)
        if (index != -1) {
            taskTypeSpinner.setSelection(index)
        }
        taskRepeatInput.isChecked = (task.repeat == 1)

        saveTaskButton.setOnClickListener {
            task.description = taskDescriptionInput.text.toString()
            task.type = taskTypeSpinner.selectedItem.toString()
            task.repeat = if (taskRepeatInput.isChecked) 1 else 0

            if (task.description.isNotEmpty()) {
                userController.dbHelper.saveTask(task, task.isNew)
                switchVisibilityOfEdit(task)
            }
        }
    }

    private fun saveCurrentBalance() {
        val currentBalanceText = edtCurrentBalance.text.toString()
        val currentBalance = currentBalanceText.toIntOrNull() ?: 0
        balance.current_balance = currentBalance
        userController.dbHelper.updateBalance(balance)
    }

    private fun addNewCategory(category: Category) {

    }
}