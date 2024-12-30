package com.thedigialex.questlog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.models.Task

class TaskAdapter(
    private val context: android.content.Context,
    private val tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val logTask: (Task) -> Unit
) : android.widget.BaseAdapter() {

    override fun getCount(): Int = tasks.size

    override fun getItem(position: Int): Task = tasks[position]

    override fun getItemId(position: Int): Long = tasks[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        val task = getItem(position)

        val tvTaskDescription = view.findViewById<TextView>(R.id.tvTaskDescription)
        val tvTaskType = view.findViewById<TextView>(R.id.tvTaskType)
        val btnLogTask = view.findViewById<Button>(R.id.btnLogTask)

        if(task.isCompleted == 1) {
            btnLogTask.visibility = View.INVISIBLE
        }

        tvTaskDescription.text = task.description
        tvTaskType.text = task.type

        btnLogTask.setOnClickListener {
            logTask(task)
        }

        view.setOnClickListener {
            onTaskClick(task)
        }
        return view
    }
}
