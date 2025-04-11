package com.example.taskmanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.ItemTaskBinding
import com.example.taskmanager.model.Task

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskCompleteClick: (Task) -> Unit,
    private val onTaskDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var taskList = mutableListOf<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = taskList[position]
        holder.bind(currentTask)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    fun submitList(list: List<Task>) {
        taskList.clear()
        taskList.addAll(list)
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.textTaskTitle.text = task.title
            
            // A descrição e hora não estão no layout, então vamos mostrar a data formatada diferente
            binding.textTaskDate.text = "${task.date} ${task.time}"
            
            // Se o usuário tiver descrição, podemos mostrar no textStatus
            if (task.description.isNotEmpty()) {
                binding.textStatus.visibility = android.view.View.VISIBLE
                binding.textStatus.text = task.description
                // Define uma cor de fundo para o status baseada no estado de conclusão
                binding.textStatus.setBackgroundColor(
                    if (task.completed) 
                        android.graphics.Color.parseColor("#4CAF50") // Verde
                    else 
                        android.graphics.Color.parseColor("#2196F3") // Azul
                )
            } else {
                binding.textStatus.visibility = android.view.View.GONE
            }
            
            binding.checkboxComplete.isChecked = task.completed

            itemView.setOnClickListener {
                onTaskClick(task)
            }

            binding.checkboxComplete.setOnClickListener {
                onTaskCompleteClick(task)
            }

            binding.buttonDelete.setOnClickListener {
                onTaskDeleteClick(task)
            }
        }
    }
} 