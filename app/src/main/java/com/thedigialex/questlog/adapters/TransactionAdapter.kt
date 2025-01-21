package com.thedigialex.questlog.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.models.Transaction

class TransactionAdapter(
    private val context: android.content.Context,
    private val transactions: List<Transaction>,
    private val onClick: (Transaction) -> Unit,
) : android.widget.BaseAdapter() {

    override fun getCount(): Int = transactions.size

    override fun getItem(position: Int): Transaction = transactions[position]

    override fun getItemId(position: Int): Long = transactions[position].id.toLong()

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false)
        val transaction = getItem(position)
        val tvTransactionAmount = view.findViewById<TextView>(R.id.tvTransactionAmount)
        val tvTransactionDetails = view.findViewById<TextView>(R.id.tvTransactionDetails)
        tvTransactionAmount.text = transaction.amount.toString()
        tvTransactionDetails.text = "Type: " + transaction.type
        view.setOnClickListener {
            onClick(transaction)
        }
        return view
    }
}
