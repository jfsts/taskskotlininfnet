package com.example.taskmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.taskmanager.databinding.FragmentButtonBinding

class ButtonFragment : Fragment() {
    private lateinit var binding: FragmentButtonBinding
    private var buttonText: String? = null
    private var clickListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            buttonText = it.getString(ARG_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentButtonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonText?.let {
            binding.button.text = it
        }
        binding.button.setOnClickListener {
            clickListener?.invoke()
        }
    }

    fun setOnClickListener(listener: () -> Unit) {
        clickListener = listener
    }

    companion object {
        private const val ARG_BUTTON_TEXT = "button_text"

        fun newInstance(buttonText: String) =
            ButtonFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BUTTON_TEXT, buttonText)
                }
            }
    }
} 