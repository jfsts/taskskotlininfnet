package com.example.taskmanager

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.taskmanager.databinding.ActivityProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { binding.imageProfile.setImageURI(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
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
        binding.imageProfile.setOnClickListener { pickImage() }
        binding.fabChangePhoto.setOnClickListener { pickImage() }

        binding.buttonSave.setOnClickListener {
            if (validateForm()) {
                // Aqui será implementada a lógica de salvar
                finish()
            }
        }
    }

    private fun pickImage() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.profile_photo_title)
            .setItems(R.array.profile_photo_options) { _, which ->
                when (which) {
                    0 -> pickImage.launch(
                        resources.getString(R.string.env_supported_image_types)
                    )
                    1 -> {} // Implementação futura da câmera
                }
            }
            .show()
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val email = binding.editEmail.text.toString()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = "E-mail inválido"
            isValid = false
        }

        val username = binding.editUsername.text.toString()
        if (username.isEmpty()) {
            binding.editUsername.error = "Nome de usuário é obrigatório"
            isValid = false
        }

        val name = binding.editName.text.toString()
        if (name.isEmpty()) {
            binding.editName.error = "Nome é obrigatório"
            isValid = false
        }

        return isValid
    }
} 