package com.thedigialex.questlog.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.thedigialex.questlog.R
import com.thedigialex.questlog.models.Note

class NoteAdapter(
    private val context: android.content.Context,
    private val notes: List<Note>,
    private val onNoteClick: (Note) -> Unit,
) : android.widget.BaseAdapter() {

    override fun getCount(): Int = notes.size

    override fun getItem(position: Int): Note = notes[position]

    override fun getItemId(position: Int): Long = notes[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        val note = getItem(position)

        val tvTaskDescription = view.findViewById<TextView>(R.id.tvTaskDescription)

        tvTaskDescription.text = note.description

        view.setOnClickListener {
            onNoteClick(note)
        }
        return view
    }
}
