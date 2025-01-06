package com.thedigialex.questlog.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.EditText
import com.thedigialex.questlog.R
import com.thedigialex.questlog.adapters.NoteAdapter
import com.thedigialex.questlog.controllers.UserController
import com.thedigialex.questlog.models.Note

class  NoteListFragment(private val userController: UserController) : Fragment() {

    private lateinit var noteListView: ListView
    private lateinit var newNoteButton: Button
    private lateinit var btnCloseNote: Button
    private lateinit var noteInputSection: View
    private lateinit var edtNoteDescription: EditText
    private lateinit var edtNoteTitle: EditText
    private lateinit var saveNoteButton: Button
    private lateinit var deleteNoteButton: Button
    private lateinit var notes: List<Note>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)
        noteListView = view.findViewById(R.id.noteListView)
        noteInputSection = view.findViewById(R.id.editSectionNote)
        edtNoteTitle = view.findViewById(R.id.edtNoteTitle)
        edtNoteDescription = view.findViewById(R.id.edtNoteDescription)
        deleteNoteButton = view.findViewById(R.id.btnDeleteNote)
        newNoteButton = view.findViewById(R.id.btnShowEditNote)
        btnCloseNote = view.findViewById(R.id.btnCloseNote)
        saveNoteButton = view.findViewById(R.id.btnSaveNote)

        loadNotes()
        newNoteButton.setOnClickListener { switchVisibilityOfEdit(Note(isNew = true)) }
        btnCloseNote.setOnClickListener { switchVisibilityOfEdit(Note(isNew = true)) }
        return view
    }

    private fun switchVisibilityOfEdit(note: Note) {
        val isInputSectionVisible = noteInputSection.visibility == View.VISIBLE
        noteInputSection.visibility = if (isInputSectionVisible) View.GONE else View.VISIBLE
        noteListView.visibility = if (isInputSectionVisible) View.VISIBLE else View.GONE
        newNoteButton.visibility = if (isInputSectionVisible) View.VISIBLE else View.GONE
        deleteNoteButton.visibility = View.GONE

        if (noteInputSection.visibility == View.VISIBLE) {
            setUpEditView(note)
        }
        if(!note.isNew) {
            deleteNoteButton.visibility = View.VISIBLE
            deleteNoteButton.setOnClickListener {
                switchVisibilityOfEdit(note)
                userController.dbHelper.deleteNote(note)
                loadNotes()
            }
        }
    }

    private fun loadNotes() {
        notes = userController.dbHelper.getNotes()
        if (notes.isEmpty()) {
            noteListView.adapter = null
        } else {
            val adapter = NoteAdapter(requireContext(), notes) { selectedNote ->
                switchVisibilityOfEdit(selectedNote)
            }
            noteListView.adapter = adapter
        }
    }

    private fun setUpEditView(note: Note) {
        edtNoteDescription.setText(note.description)
        edtNoteTitle.setText(note.title)

        saveNoteButton.setOnClickListener {
            note.description = edtNoteDescription.text.toString()
            note.title = edtNoteTitle.text.toString()
            note.sortOrder = 1

            userController.dbHelper.saveNote(note, note.isNew)
            switchVisibilityOfEdit(note)
            loadNotes()
        }
    }
}