package com.example.taskmanager

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import android.util.Log
import com.google.firebase.FirebaseApp

class App : Application() {
    override fun attachBaseContext(base: Context) {
        // Definir português do Brasil como idioma padrão
        val deviceLocale = Locale.getDefault()
        val supportedLocales = listOf("pt", "es", "en")
        
        // Se o idioma do dispositivo não for suportado, usar pt-BR como padrão
        val locale = if (supportedLocales.contains(deviceLocale.language)) {
            deviceLocale
        } else {
            Locale("pt", "BR")
        }
        
        Locale.setDefault(locale)
        
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        
        super.attachBaseContext(base.createConfigurationContext(configuration))
    }
    
    override fun onCreate() {
        Log.d("App", "onCreate iniciado")
        super.onCreate()
        
        // Tratamento global de exceções não capturadas
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("App", "Exceção não capturada: ${throwable.message}")
            throwable.printStackTrace()
        }
        
        try {
            // Inicialize o Firebase
            FirebaseApp.initializeApp(this)
            Log.d("App", "Firebase inicializado com sucesso")
        } catch (e: Exception) {
            Log.e("App", "Erro ao inicializar Firebase: ${e.message}")
            e.printStackTrace()
        }
        
        // Registrar o idioma atual para depuração
        val defaultLocale = Locale.getDefault()
        val configLocale = resources.configuration.locales[0]
        
        android.util.Log.d("AppLocale", "Default Locale: ${defaultLocale.language}-${defaultLocale.country}")
        android.util.Log.d("AppLocale", "Config Locale: ${configLocale.language}-${configLocale.country}")
    }
} 