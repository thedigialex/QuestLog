package com.thedigialex.questlog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.models.Category

class CategoryAdapter(
    private val context: android.content.Context,
    private val expensesCategory: List<Category>,
    private val onTaskClick: (Category) -> Unit,
) : android.widget.BaseAdapter() {

    override fun getCount(): Int = expensesCategory.size

    override fun getItem(position: Int): Category = expensesCategory[position]

    override fun getItemId(position: Int): Long = expensesCategory[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)

        val task = getItem(position)

        val tvTaskDescription = view.findViewById<TextView>(R.id.tvTaskDescription)
        val tvTaskType = view.findViewById<TextView>(R.id.tvTaskType)
        val btnLogTask = view.findViewById<Button>(R.id.btnLogTask)

        tvTaskDescription.text = task.name
        tvTaskType.text = task.type

        view.setOnClickListener {
            onTaskClick(task)
        }
        return view
    }
}
