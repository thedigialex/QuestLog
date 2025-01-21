package com.thedigialex.questlog.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.adapters.CategoryAdapter
import com.thedigialex.questlog.adapters.TransactionAdapter
import com.thedigialex.questlog.controllers.UserController
import com.thedigialex.questlog.models.*
import java.util.Calendar

class TransactionListFragment(private val userController: UserController) : Fragment() {

    private lateinit var transactionListView: ListView
    private lateinit var categoryListView: ListView
    private lateinit var editCategorySection: View
    private lateinit var transactionSection: View
    private lateinit var edtCurrentBalance: EditText
    private lateinit var tvExpense: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvBalance: TextView

    private lateinit var btnShowEdit: Button
    private lateinit var btnEditClose: Button
    private lateinit var btnSettingSave: Button
    private lateinit var btnOpenSettings: Button
    private lateinit var btnAddCategory: Button
    private lateinit var editSection: View
    private lateinit var settingSection: View
    private lateinit var edtCategoryName: EditText
    private lateinit var edtCategoryTargetAmount: EditText
    private lateinit var edtTransactionAmount: EditText
    private lateinit var spinnerTransactionType: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerCategoryType: Spinner
    private lateinit var btnDeleteCategory: Button
    private lateinit var btnSaveCategory: Button
    private lateinit var btnSaveTransaction: Button
    private lateinit var btnDeleteTransaction: Button

    private val categoryTypes = listOf("Income", "Expense", "Borrow")

    private lateinit var incomeCategory: List<Category>
    private lateinit var expensesCategory: List<Category>

    private lateinit var monthBasedTransaction: List<Transaction>
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
        editCategorySection = view.findViewById(R.id.editCategorySection)
        btnShowEdit = view.findViewById(R.id.btnShowEdit)
        btnEditClose = view.findViewById(R.id.btnEditClose)
        btnSettingSave = view.findViewById(R.id.btnSettingSave)
        editSection = view.findViewById(R.id.editSection)
        settingSection = view.findViewById(R.id.settingSection)
        btnOpenSettings = view.findViewById(R.id.btnOpenSettings)
        btnAddCategory = view.findViewById(R.id.btnAddCategory)
        edtTransactionAmount = view.findViewById(R.id.edtTransactionAmount)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        spinnerTransactionType = view.findViewById(R.id.spinnerTransactionType)
        spinnerCategoryType = view.findViewById(R.id.spinnerCategoryType)
        edtCategoryName = view.findViewById(R.id.edtCategoryName)
        edtCategoryTargetAmount = view.findViewById(R.id.edtCategoryTargetAmount)
        btnSaveCategory = view.findViewById(R.id.btnSaveCategory)
        btnDeleteCategory = view.findViewById(R.id.btnDeleteCategory)
        btnSaveTransaction = view.findViewById(R.id.btnSaveTransaction)
        btnDeleteTransaction = view.findViewById(R.id.btnDeleteTransaction)
        btnOpenSettings.setOnClickListener { switchVisibilityOfSetting() }
        getTransactions()
        return view
    }

    private fun getCategories() {
        incomeCategory = userController.dbHelper.getBankCategory("Income")
        expensesCategory = userController.dbHelper.getBankCategory("Expense")
        val combinedCategories = mutableListOf<Category>().apply {
            addAll(incomeCategory)
            addAll(expensesCategory)
        }
        if (combinedCategories.isEmpty()) {
            categoryListView.adapter = null
        } else {
            val adapter = CategoryAdapter(requireContext(), combinedCategories) { selectedCategory -> switchVisibilityOfCategoryEdit(selectedCategory) }
            categoryListView.adapter = adapter
        }
    }

    private fun getTransactions() {
        balance = userController.dbHelper.getBalance()
        tvBalance.text = balance.current_balance.toString()
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        monthBasedTransaction = userController.dbHelper.getTransactions(year, month)
        if (monthBasedTransaction.isEmpty()) {
            transactionListView.adapter = null
        } else {
            val adapter = TransactionAdapter(requireContext(), monthBasedTransaction) { selectedTransaction -> switchVisibilityOfTransactionEdit(selectedTransaction) }
            transactionListView.adapter = adapter
        }
        btnEditClose.setOnClickListener { switchVisibilityOfTransactionEdit(Transaction(isNew = true)) }
        btnShowEdit.setOnClickListener { switchVisibilityOfTransactionEdit(Transaction(isNew = true)) }
    }

    private fun switchVisibilityOfTransactionEdit(transaction: Transaction) {
       val adapter = ArrayAdapter(
           requireContext(),
           android.R.layout.simple_spinner_item,
           categoryTypes
       )
       adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTransactionType.adapter = adapter
        spinnerCategory.adapter = adapter
       val isSectionVisible = editSection.visibility == View.VISIBLE
       editSection.visibility = if (isSectionVisible) View.GONE else View.VISIBLE
       transactionSection.visibility = if (isSectionVisible) View.VISIBLE else View.GONE


        btnDeleteTransaction.visibility = if (transaction.isNew) View.GONE else View.VISIBLE
        edtTransactionAmount.setText(transaction.amount.toString())

        val position = categoryTypes.indexOf(transaction.type)
        if (position >= 0) {
            spinnerTransactionType.setSelection(position)
        }
        if (position >= 0) {
            spinnerCategory.setSelection(position)
        }
        btnSaveTransaction.setOnClickListener {
            transaction.amount = edtTransactionAmount.text.toString().toIntOrNull() ?: 0
            transaction.type = categoryTypes[spinnerTransactionType.selectedItemPosition]
            transaction.category = categoryTypes[spinnerCategory.selectedItemPosition]
            userController.dbHelper.updateTransaction(transaction)
            getTransactions()
            switchVisibilityOfTransactionEdit(Transaction(isNew = true))
        }
        btnDeleteTransaction.setOnClickListener {
            userController.dbHelper.deleteTransaction(transaction.id)
            getTransactions()
            switchVisibilityOfTransactionEdit(Transaction(isNew = true))
        }
    }

    private fun switchVisibilityOfSetting() {
        getCategories()
        val isSectionVisible = settingSection.visibility == View.VISIBLE
        btnOpenSettings.setBackgroundResource(
            if (isSectionVisible) R.drawable.menu_button else R.drawable.close_button
        )
        edtCurrentBalance.setText(balance.current_balance.toString())
        editCategorySection.visibility = View.GONE
        settingSection.visibility = if (isSectionVisible) View.GONE else View.VISIBLE
        editSection.visibility = View.GONE
        transactionSection.visibility = if (isSectionVisible) View.VISIBLE else View.GONE

        btnAddCategory.setOnClickListener { switchVisibilityOfCategoryEdit(Category(isNew = true)) }
        btnSettingSave.setOnClickListener { saveCurrentBalance() }
    }

    private fun switchVisibilityOfCategoryEdit(category: Category) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategoryType.adapter = adapter
        val isSectionVisible = editCategorySection.visibility == View.VISIBLE
        editCategorySection.visibility = if (isSectionVisible) View.GONE else View.VISIBLE
        settingSection.visibility = View.GONE
        btnDeleteCategory.visibility = if (category.isNew) View.GONE else View.VISIBLE
        edtCategoryTargetAmount.setText(category.target_amount.toString())
        edtCategoryName.setText(category.name)
        val position = categoryTypes.indexOf(category.type)
        if (position >= 0) {
            spinnerCategoryType.setSelection(position)
        }
        btnSaveCategory.setOnClickListener {
            category.name = edtCategoryName.text.toString()
            category.target_amount = edtCategoryTargetAmount.text.toString().toIntOrNull() ?: 0
            category.type = categoryTypes[spinnerCategoryType.selectedItemPosition]
            userController.dbHelper.updateCategory(category)
            switchVisibilityOfSetting()
        }
        btnDeleteCategory.setOnClickListener {
            userController.dbHelper.deleteCategory(category.id)
            switchVisibilityOfSetting()
        }
    }

    private fun saveCurrentBalance() {
        val currentBalanceText = edtCurrentBalance.text.toString()
        val currentBalance = currentBalanceText.toIntOrNull() ?: 0
        balance.current_balance = currentBalance
        userController.dbHelper.updateBalance(balance)
    }
}