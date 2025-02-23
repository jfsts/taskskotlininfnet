package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanager.databinding.ActivityHomeBinding
import android.content.res.Configuration

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupClickListeners()
        
        // Carrega tarefas de teste
        loadSampleTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter { task: Task ->
            // Ao clicar em uma tarefa, abre a tela de edição
            val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra("task", task)
            startActivity(intent)
        }

        binding.recyclerTasks.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = taskAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }
    }

    private fun loadSampleTasks() {
        val sampleTasks = listOf(
            Task(
                id = 1,
                title = "Reunião com equipe",
                description = "Discussão sobre o novo projeto",
                date = "26/02/2024"
            ),
            Task(
                id = 2,
                title = "Comprar mantimentos",
                description = "Leite, pão, frutas, verduras",
                date = "27/02/2024"
            ),
            Task(
                id = 3,
                title = "Academia",
                description = "Treino de musculação",
                date = "26/02/2024"
            ),
            Task(
                id = 4,
                title = "Estudar Kotlin",
                description = "Revisar coroutines e flows",
                date = "28/02/2024"
            ),
            Task(
                id = 5,
                title = "Dentista",
                description = "Consulta de rotina",
                date = "01/03/2024"
            ),
            Task(
                id = 6,
                title = "Pagar contas",
                description = "Luz, água, internet",
                date = "05/03/2024"
            )
        )

        taskAdapter.updateTasks(sampleTasks)
        
        // Atualiza a visibilidade da mensagem de lista vazia
        binding.textNoTasks.visibility = if (sampleTasks.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_theme -> {
                toggleTheme()
                true
            }
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_logout -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleTheme() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
} 