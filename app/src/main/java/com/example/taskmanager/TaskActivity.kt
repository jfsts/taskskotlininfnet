package com.example.taskmanager

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.taskmanager.databinding.ActivityTaskBinding
import com.example.taskmanager.data.TaskDao
import com.example.taskmanager.model.Task
import java.text.SimpleDateFormat
import java.util.*

class TaskActivity : BaseActivity() {
    private lateinit var binding: ActivityTaskBinding
    private lateinit var taskDao: TaskDao
    private var taskId: String? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Inicializar o TaskDao
        taskDao = TaskDao()

        // Verificar se estamos editando uma tarefa existente
        taskId = intent.getStringExtra("TASK_ID")
        isEditMode = !taskId.isNullOrEmpty()
        
        if (isEditMode) {
            supportActionBar?.title = getString(R.string.edit_task)
            // Preencher os campos com os dados da tarefa
            binding.editTitle.setText(intent.getStringExtra("TASK_TITLE") ?: "")
            binding.editDescription.setText(intent.getStringExtra("TASK_DESCRIPTION") ?: "")
            binding.editDate.setText(intent.getStringExtra("TASK_DATE") ?: "")
            binding.editTime.setText(intent.getStringExtra("TASK_TIME") ?: "")
        } else {
            supportActionBar?.title = getString(R.string.add_task)
        }

        setupDatePicker()
        setupTimePicker()
        setupSaveButton()
    }

    private fun setupDatePicker() {
        binding.editDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    binding.editDate.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            
            datePickerDialog.show()
        }
    }

    private fun setupTimePicker() {
        binding.editTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                    binding.editTime.setText(selectedTime)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            
            timePickerDialog.show()
        }
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            val title = binding.editTitle.text.toString().trim()
            val description = binding.editDescription.text.toString().trim()
            val date = binding.editDate.text.toString().trim()
            val time = binding.editTime.text.toString().trim()
            
            if (title.isEmpty()) {
                binding.editTitle.error = getString(R.string.error_empty_title)
                return@setOnClickListener
            }
            
            val task = Task(
                id = taskId ?: "",
                title = title,
                description = description,
                date = date,
                time = time,
                completed = false
            )
            
            if (isEditMode) {
                updateTask(task)
            } else {
                createTask(task)
            }
        }
    }

    private fun createTask(task: Task) {
        taskDao.addTask(task) { success, errorMessage ->
            if (success) {
                Toast.makeText(this, getString(R.string.task_added), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_adding_task) + ": $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTask(task: Task) {
        taskDao.updateTask(task) { success, errorMessage ->
            if (success) {
                Toast.makeText(this, getString(R.string.task_updated), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.error_updating_task) + ": $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 