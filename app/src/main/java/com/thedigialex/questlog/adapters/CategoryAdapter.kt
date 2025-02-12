package com.thedigialex.questlog.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.thedigialex.questlog.R
import com.thedigialex.questlog.models.Category

class CategoryAdapter(
    private val context: android.content.Context,
    private val categories: List<Category>,
    private val onTaskClick: (Category) -> Unit,
) : android.widget.BaseAdapter() {

    override fun getCount(): Int = categories.size

    override fun getItem(position: Int): Category = categories[position]

    override fun getItemId(position: Int): Long = categories[position].id.toLong()

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
        val category = getItem(position)
        val tvCategoryName = view.findViewById<TextView>(R.id.tvCategoryName)
        val tvCategoryDetails = view.findViewById<TextView>(R.id.tvCategoryDetails)
        when (category.type) {
            "Income" -> {
                tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.green))
            }
            "Expense" -> {
                tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
            "Borrow" -> {
                tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.accent))
            }
        }
        tvCategoryName.text = category.name
        tvCategoryDetails.text = "Target Amount: " + category.targetAmount
        view.setOnClickListener {
            onTaskClick(category)
        }
        return view
    }
}
