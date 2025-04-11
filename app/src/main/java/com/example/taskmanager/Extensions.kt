package com.example.taskmanager

import android.content.Intent
import android.os.Build
import android.os.Parcelable

// Função de extensão para simplificar o uso de putExtra com Parcelable
fun <T : Parcelable> Intent.putParcelableExtra(key: String, value: T): Intent {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        putExtra(key, value as Parcelable) 
    } else {
        @Suppress("DEPRECATION")
        putExtra(key, value)
    }
    return this
} 