package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.content.res.Configuration
import com.example.taskmanager.databinding.ActivityHomeBinding
import com.example.taskmanager.fragments.TaskListFragment
import android.util.Log
import java.util.*
import com.example.taskmanager.model.Task
import android.widget.Toast
import com.example.taskmanager.data.TaskDao
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var taskListFragment: TaskListFragment? = null
    private lateinit var taskDao: TaskDao

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Log.d("HomeActivity", "onCreate iniciado")
            super.onCreate(savedInstanceState)
            
            // Inicializar o TaskDao
            taskDao = TaskDao()
            
            binding = ActivityHomeBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d("HomeActivity", "Layout inflado")

            setSupportActionBar(binding.toolbar)
            Log.d("HomeActivity", "Toolbar configurada")

            // Verificar se o WeatherFragment está presente
            val weatherFragment = supportFragmentManager.findFragmentById(R.id.weatherFragment)
            Log.d("HomeActivity", "WeatherFragment encontrado: ${weatherFragment != null}")

            // Recuperar a referência ao fragment de tarefas
            val fragment = supportFragmentManager.findFragmentById(R.id.taskListFragment)
            Log.d("HomeActivity", "TaskListFragment encontrado: ${fragment != null}")
            
            if (fragment is TaskListFragment) {
                taskListFragment = fragment
                
                // Configure o adapter
                Log.d("HomeActivity", "Configurando adapter")
                try {
                    fragment.setupTaskAdapter(
                        onTaskClick = { task: Task ->
                            // Lógica para clique na tarefa
                            Log.d("HomeActivity", "Clique na tarefa: ${task.id}")
                            editTask(task)
                        },
                        onTaskCompleteClick = { task: Task ->
                            // Lógica para marcar como completa
                            Log.d("HomeActivity", "Marcar como completa: ${task.id}")
                            toggleTaskComplete(task)
                        },
                        onTaskDeleteClick = { task: Task ->
                            // Lógica para excluir tarefa
                            Log.d("HomeActivity", "Excluir tarefa: ${task.id}")
                            deleteTask(task)
                        }
                    )
                    Log.d("HomeActivity", "Adapter configurado")
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Erro ao configurar adapter: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                Log.e("HomeActivity", "Fragment não é do tipo TaskListFragment")
                Toast.makeText(this, "Erro ao carregar lista de tarefas", Toast.LENGTH_SHORT).show()
            }
            
            setupClickListeners()
            Log.d("HomeActivity", "Click listeners configurados")

        } catch (e: Exception) {
            Log.e("HomeActivity", "Erro crítico no onCreate: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Erro ao iniciar aplicativo", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }

        binding.fabAddTask.setOnLongClickListener {
            createTestTask()
            true
        }
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
                logout()
                true
            }
            R.id.action_language -> {
                LanguageActivity.start(this)
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

    override fun onResume() {
        super.onResume()
        // Ao invés de carregar tarefas de exemplo, apenas garanta que o fragment carregará do Firebase
        taskListFragment?.loadTasks()
    }

    private fun editTask(task: Task) {
        val intent = Intent(this, TaskActivity::class.java)
        intent.putExtra("task_id", task.id)
        intent.putExtra("task_title", task.title)
        intent.putExtra("task_description", task.description)
        intent.putExtra("task_date", task.date)
        intent.putExtra("task_time", task.time)
        intent.putExtra("task_completed", task.completed)
        startActivity(intent)
    }

    private fun toggleTaskComplete(task: Task) {
        task.completed = !task.completed
        
        // Atualizar no Firebase
        taskDao.updateTask(task) { success, errorMessage ->
            if (success) {
                // A atualização será refletida automaticamente pelo listener
                Log.d("HomeActivity", "Tarefa atualizada com sucesso")
            } else {
                Log.e("HomeActivity", "Erro ao atualizar tarefa: $errorMessage")
                Toast.makeText(this, "Erro ao atualizar tarefa: $errorMessage", Toast.LENGTH_SHORT).show()
                // Reverter a alteração localmente
                task.completed = !task.completed
            }
        }
    }

    private fun deleteTask(task: Task) {
        // Excluir do Firebase
        taskDao.deleteTask(task.id) { success, errorMessage ->
            if (success) {
                Log.d("HomeActivity", "Tarefa excluída com sucesso")
                // A exclusão será refletida automaticamente pelo listener
            } else {
                Log.e("HomeActivity", "Erro ao excluir tarefa: $errorMessage")
                Toast.makeText(this, "Erro ao excluir tarefa: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createTestTask() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.e("HomeActivity", "Usuário não autenticado")
            Toast.makeText(this, "Faça login para criar tarefas", Toast.LENGTH_SHORT).show()
            return
        }
        
        val task = Task(
            id = "",  // Será gerado pelo Firebase
            title = "Tarefa de Teste ${System.currentTimeMillis() / 1000}",
            description = "Descrição da tarefa de teste",
            date = "30/04/2024",
            time = "14:30",
            userId = currentUser.uid,
            completed = false
        )
        
        taskDao.addTask(task) { success, errorMessage ->
            if (success) {
                Log.d("HomeActivity", "Tarefa de teste criada com sucesso")
                Toast.makeText(this, "Tarefa criada com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("HomeActivity", "Erro ao criar tarefa: $errorMessage")
                Toast.makeText(this, "Erro ao criar tarefa: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        try {
            Log.d("HomeActivity", "Iniciando processo de logout")
            
            // Fazer logout no Firebase
            FirebaseAuth.getInstance().signOut()
            Log.d("HomeActivity", "Firebase signOut executado")
            
            // Redirecionar para a tela de login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            Log.d("HomeActivity", "Redirecionando para LoginActivity")
            startActivity(intent)
            
            // Finalizar a atividade atual
            finish()
        } catch (e: Exception) {
            Log.e("HomeActivity", "Erro durante logout: ${e.message}")
            Toast.makeText(this, "Erro ao fazer logout: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
} 