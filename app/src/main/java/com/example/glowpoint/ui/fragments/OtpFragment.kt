package com.example.glowpoint.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentOtpBinding
import com.example.glowpoint.domain.repository.AuthResult
import com.example.glowpoint.ui.viewmodel.OtpViewModel
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OtpViewModel by viewModels()
    private val args: OtpFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.verifyButton.setOnClickListener {
            val code = binding.verificationCodeEditText.text.toString().trim()
            if (code.isNotEmpty() || code.length == 6) {
                binding.progressBar.visibility = View.VISIBLE
                val credential = PhoneAuthProvider.getCredential(args.verificationId, code)
                viewModel.signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(requireContext(), "Please enter the verification code", Toast.LENGTH_SHORT).show()
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            when (result) {
                is AuthResult.Success -> {
                    Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                    if (result.isNewUser) {
                        findNavController().navigate(R.id.action_otpFragment_to_createProfileFragment)
                    } else {
                        findNavController().navigate(R.id.action_otpFragment_to_homeFragment)
                    }
                }
                is AuthResult.Failure -> {
                    Toast.makeText(requireContext(), "Login Failed: ${result.exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}