package com.example.taskmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.taskmanager.databinding.FragmentEmailInputBinding

class EmailInputFragment : Fragment() {
    private lateinit var binding: FragmentEmailInputBinding
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEmailInputBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    fun getEmail(): String = binding.editEmail.text.toString()
    
    fun setError(error: String?) {
        binding.editEmail.error = error
    }
    
    fun setText(text: String) {
        binding.editEmail.setText(text)
    }
    
    fun setTextColor(color: Int) {
        binding.editEmail.setTextColor(color)
    }
} 