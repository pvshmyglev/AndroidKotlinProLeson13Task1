package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentAuthBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class FragmentAuth : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAuthBinding.inflate(inflater, container, false)

        val viewModel: AuthViewModel by viewModels(ownerProducer = ::requireParentFragment)

        binding.buttonLogin.setOnClickListener{
            val login = binding.fieldLogin.text.toString()
            val password = binding.fieldPassword.text.toString()
            viewModel.loginAsUser(login, password)
            findNavController().navigateUp()
        }

        return binding.root

    }

}