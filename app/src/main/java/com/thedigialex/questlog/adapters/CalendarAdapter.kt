package com.thedigialex.questlog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.models.TaskLog


class CalendarAdapter(private val days: List<DayItem>) :
    RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    data class DayItem(val day: Int?, val tasks: List<TaskLog>)

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)
        val taskCountTextView: TextView = itemView.findViewById(R.id.taskCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayItem = days[position]

        if (dayItem.day != null) {
            holder.dayTextView.text = dayItem.day.toString()
            holder.taskCountTextView.text = "${dayItem.tasks.size} Tasks"
            holder.itemView.visibility = View.VISIBLE
        } else {
            holder.itemView.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int = days.size
}
