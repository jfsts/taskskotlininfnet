package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.taskmanager.databinding.ActivityLoginBinding
import android.view.animation.AnimationUtils

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Removendo setSupportActionBar
        
        // Inicia as animações
        startAnimations()

        binding.buttonLogin.setOnClickListener {
            Log.d("LoginActivity", "Clicou em Login")
            if (validateForm()) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        }

        binding.textRegister.setOnClickListener {
            Log.d("LoginActivity", "Clicou em Registrar")
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun startAnimations() {
        val fadeSlideUp = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        val fadeSlideDown = AnimationUtils.loadAnimation(this, R.anim.fade_slide_down)
        
        binding.imageLogo.startAnimation(fadeSlideDown)
        binding.textTitle.startAnimation(fadeSlideDown)
        binding.layoutForm.startAnimation(fadeSlideUp)
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val errorColor = ContextCompat.getColor(this, R.color.env_error)
        
        val email = binding.editEmail.text.toString()
        val password = binding.editPassword.text.toString()
        val minPasswordLength = resources.getString(R.string.env_min_password_length).toInt()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = "E-mail inválido"
            binding.editEmail.setTextColor(errorColor)
            isValid = false
        }

        if (password.isEmpty() || password.length < minPasswordLength) {
            binding.editPassword.error = "Senha deve ter pelo menos $minPasswordLength caracteres"
            binding.editPassword.setTextColor(errorColor)
            isValid = false
        }

        return isValid
    }
} 