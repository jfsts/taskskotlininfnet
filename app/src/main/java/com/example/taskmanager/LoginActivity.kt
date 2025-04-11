package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.taskmanager.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.widget.EditText
import android.util.Log

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            Log.d("LoginActivity", "onCreate iniciado")
            super.onCreate(savedInstanceState)
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d("LoginActivity", "Layout inflado")

            // Inicializar Firebase Auth
            try {
                auth = FirebaseAuth.getInstance()
                Log.d("LoginActivity", "Firebase Auth inicializado")
            } catch (e: Exception) {
                Log.e("LoginActivity", "Erro ao inicializar Firebase Auth: ${e.message}")
                e.printStackTrace()
                // Continue sem Firebase para testes
            }

            // Botão de login
            binding.buttonLogin.setOnClickListener {
                val emailFragment = supportFragmentManager.findFragmentById(R.id.emailFragment)
                val passwordFragment = supportFragmentManager.findFragmentById(R.id.passwordFragment)
                
                val email = emailFragment?.view?.findViewById<EditText>(R.id.editEmail)?.text.toString() ?: ""
                val password = passwordFragment?.view?.findViewById<EditText>(R.id.editPassword)?.text.toString() ?: ""

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    binding.progressBar.visibility = View.VISIBLE
                    loginUser(email, password)
                } else {
                    Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                }
            }

            // Link para tela de registro
            binding.textRegister.setOnClickListener {
                startActivity(Intent(this, RegisterActivity::class.java))
            }

            // Link para recuperação de senha
            binding.textForgotPassword.setOnClickListener {
                val intent = Intent(this, ForgotPasswordActivity::class.java)
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Erro crítico no onCreate: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Erro ao iniciar aplicativo", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Login bem-sucedido
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // Login falhou
                    Toast.makeText(baseContext, "Falha na autenticação: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Ir para a tela principal
            val intent = Intent(this, HomeActivity::class.java)
            // Limpe a pilha de atividades para que o usuário não possa voltar 
            // para a tela de login pressionando o botão voltar
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        // Verificar se o usuário já está logado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUI(currentUser)
        }
    }
} 