package com.example.taskmanager

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanager.databinding.ItemTaskBinding

class TaskAdapter(private val onItemClick: (Task) -> Unit) : 
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tasks = mutableListOf<Task>()

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : 
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.textTitle.text = task.title
            binding.textDescription.text = task.description
            binding.textDate.text = task.date
            
            // Define a cor e o texto do status
            val (statusColor, statusText) = when (task.status) {
                TaskStatus.PENDING -> Pair(R.color.env_warning, "Pendente")
                TaskStatus.COMPLETED -> Pair(R.color.env_success, "Concluída")
                TaskStatus.OVERDUE -> Pair(R.color.env_error, "Atrasada")
                TaskStatus.IN_PROGRESS -> Pair(R.color.env_info, "Em Andamento")
            }
            
            binding.textStatus.apply {
                text = statusText
                setBackgroundColor(ContextCompat.getColor(itemView.context, statusColor))
            }
            
            itemView.setOnClickListener { onItemClick(task) }
        }
    }
} 