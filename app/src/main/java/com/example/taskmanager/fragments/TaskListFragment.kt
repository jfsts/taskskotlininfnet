package com.example.taskmanager.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskmanager.R
import com.example.taskmanager.adapters.TaskAdapter
import com.example.taskmanager.data.TaskDao
import com.example.taskmanager.databinding.FragmentTaskListBinding
import com.example.taskmanager.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import com.example.taskmanager.LoginActivity
import com.example.taskmanager.TaskActivity

class TaskListFragment : Fragment() {
    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskDao: TaskDao
    private lateinit var tasksListener: ValueEventListener
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            // Inicialize o taskDao AQUI
            taskDao = TaskDao()
            
            // Verificar se o usuário está autenticado
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.e("TaskListFragment", "Usuário não autenticado")
                // Redirecionar para login
                activity?.let {
                    val intent = Intent(it, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    return
                }
            } else {
                // Aqui sabemos que currentUser não é nulo
                Log.d("TaskListFragment", "Usuário autenticado: ${currentUser.uid}")
                
                // Configure o RecyclerView e seus cliques
                setupRecyclerView()
                
                // Configure o adapter com os listeners de clique
                setupTaskAdapter(
                    onTaskClick = { task ->
                        // Este é o evento de clique na tarefa para edição
                        Log.d("TaskListFragment", "Clique para editar tarefa: ${task.title}")
                        val intent = Intent(requireContext(), TaskActivity::class.java)
                        intent.putExtra("TASK_ID", task.id)
                        intent.putExtra("TASK_TITLE", task.title)
                        intent.putExtra("TASK_DESCRIPTION", task.description)
                        intent.putExtra("TASK_DATE", task.date)
                        intent.putExtra("TASK_TIME", task.time)
                        startActivity(intent)
                    },
                    onTaskCompleteClick = { task ->
                        // Evento para marcar como concluída
                        Log.d("TaskListFragment", "Marcando tarefa como concluída: ${task.title}")
                        task.completed = !task.completed
                        taskDao.updateTask(task) { success, error ->
                            if (success) {
                                Log.d("TaskListFragment", "Tarefa atualizada com sucesso")
                            } else {
                                Log.e("TaskListFragment", "Erro ao atualizar tarefa: $error")
                                Toast.makeText(requireContext(), "Erro ao atualizar tarefa: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onTaskDeleteClick = { task ->
                        // Evento para excluir
                        Log.d("TaskListFragment", "Excluindo tarefa: ${task.title}")
                        taskDao.deleteTask(task.id) { success, error ->
                            if (success) {
                                Log.d("TaskListFragment", "Tarefa excluída com sucesso")
                            } else {
                                Log.e("TaskListFragment", "Erro ao excluir tarefa: $error")
                                Toast.makeText(requireContext(), "Erro ao excluir tarefa: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
                
                // Carregue as tarefas
                loadTasks()
            }
        } catch (e: Exception) {
            Log.e("TaskListFragment", "Erro no onViewCreated: ${e.message}")
            e.printStackTrace()
        }
        
        // Testar leitura direta
        testDirectDatabaseRead()
    }
    
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
                // Abrir detalhes da tarefa
            },
            onTaskCompleteClick = { task ->
                // Marcar tarefa como completa
                task.completed = !task.completed
                updateTask(task)
            },
            onTaskDeleteClick = { task ->
                // Excluir tarefa
                deleteTask(task.id)
            }
        )
        
        binding.recyclerTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }
    }
    
    private fun updateTask(task: Task) {
        taskDao.updateTask(task) { success, errorMessage ->
            if (!success) {
                Toast.makeText(context, "Erro ao atualizar tarefa: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun deleteTask(taskId: String) {
        taskDao.deleteTask(taskId) { success, errorMessage ->
            if (!success) {
                Toast.makeText(context, "Erro ao excluir tarefa: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    fun setupTaskAdapter(
        onTaskClick: (Task) -> Unit,
        onTaskCompleteClick: (Task) -> Unit,
        onTaskDeleteClick: (Task) -> Unit
    ) {
        try {
            Log.d("TaskListFragment", "setupTaskAdapter iniciado")
            
            // Inicializar taskDao se necessário
            if (!::taskDao.isInitialized) {
                taskDao = TaskDao()
            }
            
            taskAdapter = TaskAdapter(onTaskClick, onTaskCompleteClick, onTaskDeleteClick)
            binding.recyclerTasks.adapter = taskAdapter
            Log.d("TaskListFragment", "Adapter configurado")
            
            // Agora que o adapter está configurado, podemos carregar tarefas
            loadTasks()
        } catch (e: Exception) {
            Log.e("TaskListFragment", "Erro ao configurar adapter: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun loadTasks() {
        try {
            // Inicializar o tasksListener antes de usá-lo
            tasksListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        Log.d("TaskListFragment", "Dados recebidos. Número de tarefas: ${snapshot.childrenCount}")
                        
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        if (currentUser == null) {
                            Log.e("TaskListFragment", "Usuário não autenticado ao processar tarefas")
                            return
                        }
                        
                        val taskList = mutableListOf<Task>()
                        
                        for (taskSnapshot in snapshot.children) {
                            Log.d("TaskListFragment", "Processando tarefa: ${taskSnapshot.key}")
                            val task = taskSnapshot.getValue(Task::class.java)
                            val taskUserId = taskSnapshot.child("userId").getValue(String::class.java)
                            
                            Log.d("TaskListFragment", "Tarefa: ${taskSnapshot.key}, userId: $taskUserId, usuário atual: ${currentUser.uid}")
                            
                            // Incluir apenas tarefas do usuário atual
                            if (taskUserId == currentUser.uid) {
                                task?.let {
                                    it.id = taskSnapshot.key ?: ""
                                    taskList.add(it)
                                    Log.d("TaskListFragment", "Tarefa adicionada à lista: ${it.title}")
                                }
                            }
                        }
                        
                        // Atualizar a UI
                        taskAdapter.submitList(taskList)
                        
                        if (taskList.isEmpty()) {
                            Log.d("TaskListFragment", "Lista de tarefas vazia para o usuário")
                            binding.textEmptyList.visibility = View.VISIBLE
                        } else {
                            Log.d("TaskListFragment", "Lista de tarefas não vazia: ${taskList.size} itens")
                            binding.textEmptyList.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        Log.e("TaskListFragment", "Erro no onDataChange: ${e.message}")
                        e.printStackTrace()
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e("TaskListFragment", "Operação cancelada: ${error.message}")
                }
            }
            
            // Obter as tarefas
            Log.d("TaskListFragment", "Obtendo tarefas do usuário...")
            taskDao.getUserTasks(tasksListener)
            
        } catch (e: Exception) {
            Log.e("TaskListFragment", "Erro ao carregar tarefas: ${e.message}")
            e.printStackTrace()
        }
    }
    
    fun updateTasks(tasks: List<Task>) {
        taskAdapter.submitList(tasks)
        
        if (tasks.isEmpty()) {
            binding.textEmptyList.visibility = View.VISIBLE
        } else {
            binding.textEmptyList.visibility = View.GONE
        }
    }
    
    private fun testDirectDatabaseRead() {
        FirebaseDatabase.getInstance().reference.child("tasks")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("TaskListFragment", "Leitura direta - Tarefas encontradas: ${snapshot.childrenCount}")
                    for (taskSnapshot in snapshot.children) {
                        val taskId = taskSnapshot.key
                        val userId = taskSnapshot.child("userId").getValue(String::class.java)
                        val title = taskSnapshot.child("title").getValue(String::class.java)
                        Log.d("TaskListFragment", "Tarefa: $taskId, UserId: $userId, Título: $title")
                    }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Log.e("TaskListFragment", "Erro na leitura direta: ${error.message}")
                }
            })
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // Adicione uma verificação para evitar erro se tasksListener não for inicializada
        if (::tasksListener.isInitialized) {
            taskDao.removeTaskListener(tasksListener)
        }
        
        _binding = null
    }
} 