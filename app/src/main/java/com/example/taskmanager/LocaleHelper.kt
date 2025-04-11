package com.example.taskmanager

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, language: String? = null): Context {
        // Se language for null, use o idioma do sistema
        val locale = if (language.isNullOrEmpty()) {
            Locale.getDefault()
        } else {
            Locale(language)
        }
        
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            configuration.locale = locale
        }

        return context.createConfigurationContext(configuration)
    }
} 