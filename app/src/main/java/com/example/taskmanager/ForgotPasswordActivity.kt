package com.example.taskmanager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.taskmanager.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configurar botão de enviar link
        binding.buttonSendLink.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            
            if (email.isEmpty()) {
                binding.editEmail.error = getString(R.string.email_required)
                return@setOnClickListener
            }
            
            sendPasswordResetEmail(email)
        }
        
        // Configurar botão de voltar para login
        binding.buttonBackToLogin.setOnClickListener {
            finish() // Simplesmente fecha esta atividade e volta para a anterior (Login)
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        // Mostrar ProgressBar
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonSendLink.isEnabled = false
        binding.buttonBackToLogin.isEnabled = false

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                // Esconder ProgressBar
                binding.progressBar.visibility = View.GONE
                binding.buttonSendLink.isEnabled = true
                binding.buttonBackToLogin.isEnabled = true

                if (task.isSuccessful) {
                    Log.d("ForgotPassword", "Email enviado para: $email")
                    Toast.makeText(this, getString(R.string.reset_email_sent), Toast.LENGTH_LONG).show()
                } else {
                    Log.e("ForgotPassword", "Erro ao enviar email: ${task.exception?.message}")
                    Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
} 