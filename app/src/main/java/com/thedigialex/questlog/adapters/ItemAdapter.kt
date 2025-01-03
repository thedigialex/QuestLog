package com.thedigialex.questlog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.models.Item

class ItemAdapter(
    private val context: android.content.Context,
    private val items: List<Item>,
    private val purchaseItem: (Item) -> Unit
) : android.widget.BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): Item = items[position]

    override fun getItemId(position: Int): Long = items[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        val item = getItem(position)

        val tvTaskDescription = view.findViewById<TextView>(R.id.tvTaskDescription)
        val tvTaskType = view.findViewById<TextView>(R.id.tvTaskType)
        val btnLogTask = view.findViewById<Button>(R.id.btnLogTask)

        tvTaskDescription.text = item.resource
        tvTaskType.text = item.type

        btnLogTask.setOnClickListener {
            purchaseItem(item)
        }

        return view
    }
}
