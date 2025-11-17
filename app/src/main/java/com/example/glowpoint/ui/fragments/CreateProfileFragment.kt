package com.example.glowpoint.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentCreateProfileBinding
import com.example.glowpoint.ui.viewmodel.CreateProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateProfileFragment : Fragment() {

    private var _binding: FragmentCreateProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createProfileButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.createProfile(name, email)
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.profileCreated.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.GONE
            if (it) {
                Toast.makeText(requireContext(), "Profile Created Successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_createProfileFragment_to_homeFragment)
            } else {
                Toast.makeText(requireContext(), "Profile Creation Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}