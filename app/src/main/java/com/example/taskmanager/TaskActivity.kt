package com.example.taskmanager

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanager.databinding.ActivityTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupDatePicker()
        setupSaveButton()
        
        // Carrega a tarefa se estiver editando
        intent.parcelable<Task>("task")?.let { task ->
            binding.editTitle.setText(task.title)
            binding.editDescription.setText(task.description)
            binding.editDate.setText(task.date)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupDatePicker() {
        val dateFormat = SimpleDateFormat(
            resources.getString(R.string.env_date_format), 
            Locale.getDefault()
        )

        binding.editDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.editDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSaveButton() {
        binding.fabSave.setOnClickListener {
            if (validateForm()) {
                // Aqui será implementada a lógica de salvar
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val maxTitleLength = resources.getString(R.string.env_max_task_title_length).toInt()

        val title = binding.editTitle.text.toString()
        if (title.isEmpty()) {
            binding.editTitle.error = "Título é obrigatório"
            isValid = false
        } else if (title.length > maxTitleLength) {
            binding.editTitle.error = "Título não pode ter mais que $maxTitleLength caracteres"
            isValid = false
        }

        val date = binding.editDate.text.toString()
        if (date.isEmpty()) {
            binding.editDate.error = "Data é obrigatória"
            isValid = false
        }

        return isValid
    }
}

// Função de extensão para simplificar a obtenção de Parcelables
inline fun <reified T : Parcelable> android.content.Intent.parcelable(key: String): T? {
    return when {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU -> {
            getParcelableExtra(key, T::class.java)
        }
        else -> {
            @Suppress("DEPRECATION")
            getParcelableExtra(key) as? T
        }
    }
} 