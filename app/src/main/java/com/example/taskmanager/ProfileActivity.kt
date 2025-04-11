package com.example.taskmanager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.taskmanager.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    
    private var currentPhotoPath: String = ""
    private var photoUri: Uri? = null

    // Lançadores para resultados de atividades
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                // A foto foi salva no URI que criamos
                photoUri = Uri.fromFile(File(currentPhotoPath))
                loadImageIntoView(photoUri)
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Erro ao processar imagem da câmera: ${e.message}")
                Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                photoUri = result.data?.data
                // Copiar a imagem da galeria para armazenamento local
                photoUri?.let { uri ->
                    val localFile = createImageFile()
                    copyImageToFile(uri, localFile)
                    photoUri = Uri.fromFile(localFile)
                }
                loadImageIntoView(photoUri)
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Erro ao processar imagem da galeria: ${e.message}")
                Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Lançadores para permissões
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Log para debugar o idioma atual
        val currentLocale = resources.configuration.locales.get(0)
        Log.d("ProfileActivity", "Locale: ${currentLocale.language}-${currentLocale.country}")
        Log.d("ProfileActivity", "Botão Salvar: ${getString(R.string.profile_save)}")
        
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.profile_title)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Configurar botões
        setupLogoutButton()
        setupButtons()

        // Carregar dados do usuário
        loadUserData()
    }

    private fun setupButtons() {
        binding.btnCamera.setOnClickListener {
            if (hasCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoUri = FileProvider.getUriForFile(
            this,
            "com.example.taskmanager.fileprovider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(null)
        val image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
        currentPhotoPath = image.absolutePath
        return image
    }

    private fun copyImageToFile(uri: Uri, destinationFile: File) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun loadImageIntoView(uri: Uri?) {
        uri?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.imageProfile)
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        if (user != null) {
            // Mostrar o email (não editável, pois é a chave de autenticação)
            binding.editEmail.setText(user.email)
            binding.editEmail.isEnabled = false

            // Carregar dados do Realtime Database
            val userRef = database.reference.child("users").child(user.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                        val photoPath = snapshot.child("photoPath").getValue(String::class.java)
                        
                        binding.editName.setText(name)
                        binding.editPhone.setText(phone)
                        
                        // Carregar foto de perfil do armazenamento local
                        if (photoPath != null && photoPath.isNotEmpty()) {
                            val photoFile = File(photoPath)
                            if (photoFile.exists()) {
                                photoUri = Uri.fromFile(photoFile)
                                loadImageIntoView(photoUri)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileActivity", "Erro ao carregar dados: ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileActivity", "Erro ao buscar dados: ${error.message}")
                    Toast.makeText(this@ProfileActivity, "Erro ao carregar dados do perfil", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun saveUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val name = binding.editName.text.toString().trim()
            val phone = binding.editPhone.text.toString().trim()
            
            binding.progressBar.visibility = View.VISIBLE
            
            // Salvar dados no Realtime Database
            val userRef = database.reference.child("users").child(user.uid)
            val userData = HashMap<String, Any>()
            userData["name"] = name
            userData["phone"] = phone
            userData["email"] = user.email ?: ""
            
            // Salvar caminho da foto se existir
            photoUri?.let { uri ->
                try {
                    val photoPath = uri.path ?: ""
                    userData["photoPath"] = photoPath
                } catch (e: Exception) {
                    Log.e("ProfileActivity", "Erro ao salvar caminho da foto: ${e.message}")
                }
            }
            
            userRef.updateChildren(userData).addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro ao salvar dados do perfil", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            try {
                Log.d("ProfileActivity", "Iniciando processo de logout")
                
                // Fazer logout no Firebase
                FirebaseAuth.getInstance().signOut()
                Log.d("ProfileActivity", "Firebase signOut executado")
                
                // Redirecionar para a tela de login
                val intent = Intent(this, LoginActivity::class.java)
                // Limpar a pilha de atividades para que o usuário não possa voltar
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                Log.d("ProfileActivity", "Redirecionando para LoginActivity")
                startActivity(intent)
                
                // Finalizar a atividade atual
                finish()
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Erro durante logout: ${e.message}")
                Toast.makeText(this, "Erro ao fazer logout: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 