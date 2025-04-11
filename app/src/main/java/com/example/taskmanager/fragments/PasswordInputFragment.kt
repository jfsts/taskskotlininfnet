package com.example.taskmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.taskmanager.databinding.FragmentPasswordInputBinding

class PasswordInputFragment : Fragment() {
    private lateinit var binding: FragmentPasswordInputBinding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPasswordInputBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    fun getPassword(): String = binding.editPassword.text.toString()
    
    fun setError(error: String?) {
        binding.editPassword.error = error
    }

    fun setTextColor(color: Int) {
        binding.editPassword.setTextColor(color)
    }
} 