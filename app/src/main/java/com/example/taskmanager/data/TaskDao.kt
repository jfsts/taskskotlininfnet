package com.example.taskmanager.data

import com.example.taskmanager.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.util.Log

class TaskDao {
    private val database = FirebaseDatabase.getInstance().reference
    private val tasksRef = database.child("tasks")
    private val auth = FirebaseAuth.getInstance()

    fun addTask(task: Task, callback: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "Usuário não autenticado")
            return
        }

        try {
            task.userId = currentUser.uid
            Log.d("TaskDao", "Adicionando tarefa para usuário: ${currentUser.uid}")
            
            val taskRef = tasksRef.push()
            task.id = taskRef.key ?: ""
            
            taskRef.setValue(task)
                .addOnSuccessListener {
                    Log.d("TaskDao", "Tarefa adicionada com sucesso: ${task.id}")
                    callback(true, null)
                }
                .addOnFailureListener { e ->
                    Log.e("TaskDao", "Erro ao adicionar tarefa: ${e.message}")
                    callback(false, e.message)
                }
        } catch (e: Exception) {
            Log.e("TaskDao", "Exceção ao adicionar tarefa: ${e.message}")
            callback(false, e.message)
        }
    }

    fun updateTask(task: Task, callback: (Boolean, String?) -> Unit) {
        if (task.id.isEmpty()) {
            callback(false, "ID da tarefa inválido")
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            callback(false, "Usuário não autenticado")
            return
        }

        val taskMap = HashMap<String, Any>()
        taskMap["title"] = task.title
        taskMap["description"] = task.description
        taskMap["date"] = task.date
        taskMap["time"] = task.time
        taskMap["completed"] = task.completed

        tasksRef.child(task.id).updateChildren(taskMap)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    fun deleteTask(taskId: String, callback: (Boolean, String?) -> Unit) {
        if (taskId.isEmpty()) {
            callback(false, "ID da tarefa inválido")
            return
        }

        tasksRef.child(taskId).removeValue()
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    fun getUserTasks(listener: ValueEventListener) {
        try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Log.d("TaskDao", "Obtendo tarefas para usuário: ${currentUser.uid}")
                
                // Simplesmente obter todas as tarefas
                tasksRef.addValueEventListener(listener)
                
                Log.d("TaskDao", "Listener para tarefas registrado")
            } else {
                Log.e("TaskDao", "Usuário não está autenticado")
            }
        } catch (e: Exception) {
            Log.e("TaskDao", "Erro ao buscar tarefas: ${e.message}")
            e.printStackTrace()
        }
    }

    fun removeTaskListener(listener: ValueEventListener) {
        try {
            // Evite erros se o listener já foi removido
            tasksRef.removeEventListener(listener)
        } catch (e: Exception) {
            Log.e("TaskDao", "Erro ao remover listener: ${e.message}")
        }
    }
} 