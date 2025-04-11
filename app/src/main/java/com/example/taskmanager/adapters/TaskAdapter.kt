package com.example.taskmanager.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.R
import com.example.taskmanager.model.Task

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCompleteClick: (Task) -> Unit,
    private val onTaskDeleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.textTaskTitle)
        private val textDate: TextView = itemView.findViewById(R.id.textTaskDate)
        private val checkboxComplete: CheckBox = itemView.findViewById(R.id.checkboxComplete)
        private val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(task: Task) {
            textTitle.text = task.title
            textDate.text = "${task.date} ${task.time}"
            checkboxComplete.isChecked = task.completed

            itemView.setOnClickListener { onTaskClick(task) }
            checkboxComplete.setOnClickListener { onTaskCompleteClick(task) }
            buttonDelete.setOnClickListener { onTaskDeleteClick(task) }
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
} 