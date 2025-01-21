package com.thedigialex.questlog.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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
    private lateinit var tvBank: TextView
    private lateinit var tvBorrow: TextView

    private lateinit var btnShowEdit: Button
    private lateinit var btnEditClose: Button
    private lateinit var btnSettingSave: Button
    private lateinit var btnOpenSettings: Button
    private lateinit var btnAddCategory: Button
    private lateinit var editSection: View
    private lateinit var settingSection: View
    private lateinit var edtCategoryName: EditText
    private lateinit var edtCategoryTargetAmount: EditText
    private lateinit var edtCategoryDetails: EditText
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
        tvBank = view.findViewById(R.id.tvBank)
        tvBorrow = view.findViewById(R.id.tvBorrow)
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
        edtCategoryDetails = view.findViewById(R.id.edtCategoryDetails)
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
        getCategories()
        balance = userController.dbHelper.getBalance()
        tvBank.text = balance.current_balance.toString()
        tvBorrow.text = "Borrow\n" + balance.borrowed_balance.toString()
        var incomeTotal = 0
        var expenseTotal = 0
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        monthBasedTransaction = userController.dbHelper.getTransactions(year, month)
        if (monthBasedTransaction.isEmpty()) {
            transactionListView.adapter = null
        } else {
            for (i in monthBasedTransaction.indices) {
                if(monthBasedTransaction[i].type == "Income"){ incomeTotal += monthBasedTransaction[i].amount }
                if(monthBasedTransaction[i].type == "Expense"){ expenseTotal += monthBasedTransaction[i].amount }
            }
            val adapter = TransactionAdapter(requireContext(), monthBasedTransaction) { selectedTransaction -> switchVisibilityOfTransactionEdit(selectedTransaction) }
            transactionListView.adapter = adapter
        }
        tvIncome.text = "Income\n" + incomeTotal.toString()
        tvExpense.text = "Expense\n" + expenseTotal.toString()
        btnEditClose.setOnClickListener { switchVisibilityOfTransactionEdit(Transaction(isNew = true)) }
        btnShowEdit.setOnClickListener { switchVisibilityOfTransactionEdit(Transaction(isNew = true)) }
    }

    private fun switchVisibilityOfTransactionEdit(transaction: Transaction) {
        getTransactions()
        val incomeCategories = mutableListOf("Salary")
        val expenseCategories = mutableListOf("Borrow Payment") //ToDo if remove this from the borrowed list
        incomeCategory.forEach { category ->
            incomeCategories.add(category.name)
        }
        for (i in expensesCategory.indices) {
            expenseCategories.add(expensesCategory[i].name)
        }
        val transactionTypeCategories = mapOf(
            "Income" to incomeCategories,
            "Expense" to expenseCategories,
            "Borrow" to expenseCategories
        )
        val transactionTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            transactionTypeCategories.keys.toList()
        )
        transactionTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTransactionType.adapter = transactionTypeAdapter
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf<String>()
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
        spinnerTransactionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = spinnerTransactionType.selectedItem.toString()
                val categories = transactionTypeCategories[selectedType] ?: emptyList()
                categoryAdapter.clear()
                categoryAdapter.addAll(categories)
                categoryAdapter.notifyDataSetChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerTransactionType.setSelection(categoryTypes.indexOf(transaction.type))
        spinnerCategory.setSelection(0) //ToDo fix defaulting to the transaction value

       val isSectionVisible = editSection.visibility == View.VISIBLE
       editSection.visibility = if (isSectionVisible) View.GONE else View.VISIBLE
       transactionSection.visibility = if (isSectionVisible) View.VISIBLE else View.GONE
       btnDeleteTransaction.visibility = if (transaction.isNew) View.GONE else View.VISIBLE
       edtTransactionAmount.setText(transaction.amount.toString())

       btnSaveTransaction.setOnClickListener {
           transaction.amount = edtTransactionAmount.text.toString().toIntOrNull() ?: 0
           transaction.type = spinnerTransactionType.selectedItem.toString()
           transaction.category = spinnerCategory.selectedItem.toString()
           if(transaction.type == "Borrow") {
               balance.borrowed_balance += transaction.amount

           }
           else {
               if(transaction.type == "Income") {
                   balance.current_balance += transaction.amount
               }
               else {
                   balance.current_balance -= transaction.amount
                   if(transaction.category == "Borrow Payment") {
                       balance.borrowed_balance -= transaction.amount
                   }
               }
           }
           userController.dbHelper.updateBalance(balance)
           userController.dbHelper.updateTransaction(transaction)
           switchVisibilityOfTransactionEdit(Transaction(isNew = true))
       }
       btnDeleteTransaction.setOnClickListener {
           userController.dbHelper.deleteTransaction(transaction.id)
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
        edtCategoryDetails.setText(category.details)
        val position = categoryTypes.indexOf(category.type)
        if (position >= 0) {
            spinnerCategoryType.setSelection(position)
        }
        btnSaveCategory.setOnClickListener {
            category.name = edtCategoryName.text.toString()
            category.target_amount = edtCategoryTargetAmount.text.toString().toIntOrNull() ?: 0
            category.type = categoryTypes[spinnerCategoryType.selectedItemPosition]
            category.details = edtCategoryDetails.text.toString()
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