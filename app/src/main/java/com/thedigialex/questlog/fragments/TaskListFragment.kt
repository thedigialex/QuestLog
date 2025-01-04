package com.thedigialex.questlog.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import com.thedigialex.questlog.R
import com.thedigialex.questlog.adapters.TaskAdapter
import com.thedigialex.questlog.controllers.UserController
import com.thedigialex.questlog.models.Task

class TaskListFragment(private val userController: UserController) : Fragment() {

    private lateinit var taskListView: ListView
    private lateinit var newTaskButton: Button
    private lateinit var btnTaskClose: Button
    private lateinit var taskInputSection: View
    private lateinit var taskDescriptionInput: EditText
    private lateinit var taskTypeSpinner: Spinner
    private lateinit var taskRepeatInput: CheckBox
    private lateinit var saveTaskButton: Button
    private lateinit var deleteTaskButton: Button
    private lateinit var btnComplete: RadioButton
    private lateinit var tasks: List<Task>
    private val taskTypes = listOf("Daily", "Weekly", "Monthly")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task_list, container, false)

        taskListView = view.findViewById(R.id.taskListView)
        newTaskButton = view.findViewById(R.id.btnShowEdit)
        btnTaskClose = view.findViewById(R.id.btnTaskClose)
        taskInputSection = view.findViewById(R.id.editSection)
        taskDescriptionInput = view.findViewById(R.id.edtTaskDescription)
        taskTypeSpinner = view.findViewById(R.id.spinnerTaskType)
        taskRepeatInput = view.findViewById(R.id.repeatBox)
        saveTaskButton = view.findViewById(R.id.btnSaveTask)
        deleteTaskButton = view.findViewById(R.id.btnDeleteTask)
        btnComplete = view.findViewById(R.id.btnComplete)
        val btnNotComplete = view.findViewById<RadioButton>(R.id.btnNotComplete)
        setTaskCompletionListener(btnComplete, 1)
        setTaskCompletionListener(btnNotComplete, 0)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            taskTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taskTypeSpinner.adapter = adapter

        loadTasks()

        newTaskButton.setOnClickListener { switchVisibilityOfEdit(Task(isNew = true)) }
        btnTaskClose.setOnClickListener { switchVisibilityOfEdit(Task(isNew = true)) }
        return view
    }

   private fun setTaskCompletionListener(button: RadioButton, completionStatus: Int) {
        button.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                loadTasks(isCompleted = completionStatus)
            }
        }
    }

    private fun loadTasks(isCompleted: Int = 0) {
        tasks = userController.dbHelper.getTasks(isCompleted)

        if (tasks.isEmpty()) {
            Toast.makeText(requireContext(), "No tasks with the selected status.", Toast.LENGTH_SHORT).show()
            taskListView.adapter = null
        } else {
            val adapter = TaskAdapter(requireContext(), tasks, { selectedTask ->
                switchVisibilityOfEdit(selectedTask)
            }, { task ->
                logTask(task)
            })
            taskListView.adapter = adapter
        }
    }

    private fun logTask(task: Task) {
        userController.dbHelper.logTask(task, false)
        loadTasks()
    }

    private fun switchVisibilityOfEdit(task: Task) {
        val isInputSectionVisible = taskInputSection.visibility == View.VISIBLE
        taskInputSection.visibility = if (isInputSectionVisible) View.GONE else View.VISIBLE
        taskListView.visibility = if (isInputSectionVisible) View.VISIBLE else View.GONE
        newTaskButton.visibility = if (isInputSectionVisible) View.VISIBLE else View.GONE
        deleteTaskButton.visibility = View.GONE
        if (taskInputSection.visibility == View.VISIBLE) {
            setUpEditView(task)
        }
        if(!task.isNew) {
            deleteTaskButton.visibility = View.VISIBLE
            deleteTaskButton.setOnClickListener {
                switchVisibilityOfEdit(task)
                userController.dbHelper.deleteTaskAndLogs(task)
                loadTasks(if (btnComplete.isChecked) 1 else 0)
            }
        }
    }

    private fun setUpEditView(task: Task) {
        taskDescriptionInput.setText(task.description)
        val index = taskTypes.indexOf(task.type)
        if (index != -1) {
            taskTypeSpinner.setSelection(index)
        }
        taskRepeatInput.isChecked = (task.repeat == 1)

        saveTaskButton.setOnClickListener {
            task.description = taskDescriptionInput.text.toString()
            task.type = taskTypeSpinner.selectedItem.toString()
            task.repeat = if (taskRepeatInput.isChecked) 1 else 0

            if (task.description.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a quest description", Toast.LENGTH_SHORT).show()
            } else {
                userController.dbHelper.saveTask(task, task.isNew)
                switchVisibilityOfEdit(task)
                loadTasks()
            }
        }
    }
}