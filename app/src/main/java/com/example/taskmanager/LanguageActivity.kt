package com.example.taskmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Button
import android.widget.Toast

class LanguageActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)
        
        val radioGroup = findViewById<RadioGroup>(R.id.languageRadioGroup)
        val btnApply = findViewById<Button>(R.id.btnApplyLanguage)
        
        // Selecionar o idioma atual
        val currentLanguage = getCurrentLanguage()
        when (currentLanguage) {
            "pt" -> findViewById<RadioButton>(R.id.radioPt).isChecked = true
            "en" -> findViewById<RadioButton>(R.id.radioEn).isChecked = true
            "es" -> findViewById<RadioButton>(R.id.radioEs).isChecked = true
        }
        
        btnApply.setOnClickListener {
            val selectedLanguage = when (radioGroup.checkedRadioButtonId) {
                R.id.radioPt -> "pt"
                R.id.radioEn -> "en"
                R.id.radioEs -> "es"
                else -> "pt" // Default to Portuguese
            }
            
            saveLanguage(selectedLanguage)
            restartApp()
        }
    }
    
    private fun getCurrentLanguage(): String {
        val prefs = getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        return prefs.getString("selected_language", "pt") ?: "pt"
    }
    
    private fun saveLanguage(languageCode: String) {
        val prefs = getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_language", languageCode).apply()
        Toast.makeText(this, R.string.language_changed, Toast.LENGTH_SHORT).show()
    }
    
    private fun restartApp() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAffinity()
    }
    
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, LanguageActivity::class.java))
        }
    }
} 