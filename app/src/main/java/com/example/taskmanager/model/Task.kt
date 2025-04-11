package com.example.taskmanager.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Task(
    @get:Exclude
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var date: String = "",
    var time: String = "",
    var userId: String = "",
    var completed: Boolean = false
) {
    // Construtor vazio obrigatório para Firebase
    constructor() : this("", "", "", "", "", "", false)
} 