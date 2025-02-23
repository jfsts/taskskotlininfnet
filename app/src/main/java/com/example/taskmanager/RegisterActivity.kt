package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanager.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.buttonRegister.setOnClickListener {
            if (validateForm()) {
                // Após registro bem-sucedido, vai para a tela principal
                startActivity(Intent(this, HomeActivity::class.java))
                finish() // Fecha a tela de registro
            }
        }

        binding.textLogin.setOnClickListener {
            finish() // Volta para a tela de login
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        
        val email = binding.editEmail.text.toString()
        val password = binding.editPassword.text.toString()
        val confirmPassword = binding.editConfirmPassword.text.toString()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = "E-mail inválido"
            isValid = false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.editPassword.error = "Senha deve ter pelo menos 6 caracteres"
            isValid = false
        }

        if (confirmPassword != password) {
            binding.editConfirmPassword.error = "As senhas não coincidem"
            isValid = false
        }

        return isValid
    }
} 