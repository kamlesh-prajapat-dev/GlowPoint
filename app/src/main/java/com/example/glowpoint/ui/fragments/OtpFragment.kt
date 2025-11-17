package com.example.glowpoint.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentOtpBinding
import com.example.glowpoint.domain.repository.AuthResult
import com.example.glowpoint.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    private lateinit var timer: CountDownTimer

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

        startTimer()

        binding.verifyButton.setOnClickListener {
            val code = binding.verificationCodeEditText.text.toString().trim()
            if (code.isNotEmpty() && code.length == 6) {
                binding.progressBar.visibility = View.VISIBLE
                viewModel.verificationId.value?.let {
                    val credential = PhoneAuthProvider.getCredential(it, code)
                    viewModel.signInWithPhoneAuthCredential(credential)
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }

        binding.resendCodeTextView.setOnClickListener {
            viewModel.resendVerificationCode(requireActivity())
            startTimer()
        }

        observeViewModel()
    }

    private fun startTimer() {
        binding.resendCodeTextView.visibility = View.GONE
        binding.timerTextView.visibility = View.VISIBLE
        timer = object : CountDownTimer(60000, 1000) {
            @SuppressLint("DefaultLocale", "SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.timerTextView.text = "Resend in 00:${String.format("%02d", seconds)}"
            }

            override fun onFinish() {
                binding.timerTextView.visibility = View.GONE
                binding.resendCodeTextView.visibility = View.VISIBLE
            }
        }.start()
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
                        findNavController().navigate(R.id.action_otpFragment_to_homeFragment, null, navOptions {
                            popUpTo(R.id.loginFragment) { inclusive = true }
                        })
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
        timer.cancel()
        _binding = null
        viewModel.onNavigationComplete()
    }
}