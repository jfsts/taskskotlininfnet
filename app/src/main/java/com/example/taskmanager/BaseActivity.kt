package com.example.taskmanager

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

open class BaseActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        val deviceLocale = Locale.getDefault()
        val supportedLocales = listOf("pt", "es", "en")
        
        // Se o idioma do dispositivo não for suportado, usar pt-BR como padrão
        val locale = if (supportedLocales.contains(deviceLocale.language)) {
            deviceLocale
        } else {
            Locale("pt", "BR")
        }
        
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Registrar o idioma atual para depuração
        val currentLocale = resources.configuration.locales.get(0)
        android.util.Log.d("Locale", "Activity Locale: ${currentLocale.language}, ${currentLocale.country}")
    }
} 