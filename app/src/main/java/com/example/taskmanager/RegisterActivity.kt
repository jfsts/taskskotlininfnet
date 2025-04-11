package com.example.taskmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.taskmanager.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        binding.buttonRegister.setOnClickListener {
            val name = binding.editName.text.toString()
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()
            val confirmPassword = binding.editConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, R.string.passwords_dont_match, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            registerUser(name, email, password)
        }

        binding.textLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Registro bem-sucedido, agora salve os dados do usuário no database
                    val user = auth.currentUser
                    saveUserData(user, name, email)
                } else {
                    // Registro falhou
                    Toast.makeText(baseContext, "Falha no registro: ${task.exception?.message}", 
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserData(user: FirebaseUser?, name: String, email: String) {
        if (user == null) return

        val database = FirebaseDatabase.getInstance().reference
        val userRef = database.child("users").child(user.uid)

        val userData = HashMap<String, Any>()
        userData["name"] = name
        userData["email"] = email

        userRef.setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show()
                updateUI(user)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao salvar dados: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Ir para a tela principal
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
} 