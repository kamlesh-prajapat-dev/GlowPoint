package com.example.glowpoint.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentOtpBinding
import com.example.glowpoint.domain.model.AuthResult
import com.example.glowpoint.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    private var timer: CountDownTimer? = null

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

        setupClickListeners()
        observeViewModel()
        startTimer()
    }

    private fun setupClickListeners() {
        binding.verifyButton.setOnClickListener {
            val code = binding.verificationCodeEditText.text.toString().trim()
            if (code.length == 6) {
                binding.progressBar.isVisible = true
                viewModel.verificationId.value?.let { verificationId ->
                    val credential = PhoneAuthProvider.getCredential(verificationId, code)
                    // The ViewModel will handle the result. This fragment just initiates the sign-in.
                    viewModel.signInWithPhoneAuthCredential(credential)
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a valid 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }

        binding.resendCodeTextView.setOnClickListener {
            // Let the ViewModel handle the resend logic
            viewModel.resendVerificationCode(requireActivity())
            startTimer() // Restart the UI timer
        }
    }

    private fun startTimer() {
        // Cancel any existing timer
        timer?.cancel()

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
        // Observer for the result of the phone authentication
        viewModel.authResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe // Event has been handled

            binding.progressBar.isVisible = false

            when (result) {
                is AuthResult.Success -> {
                    if (viewModel.isRegistered.value == true) {
                        // REGISTRATION FLOW: Auth is successful, now create the user profile.
                        viewModel.createProfile()
                    } else {
                        // LOGIN FLOW: Auth is successful, navigate to next screen.
                        Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                        handleNavigation()
                    }
                }
                is AuthResult.Failure -> {
                    Toast.makeText(requireContext(), "Authentication Failed: ${result.exception.message}", Toast.LENGTH_LONG).show()
                }
            }
            // Reset the event so it doesn't re-fire on config change
            viewModel.onAuthEventHandled()
        }

        // Observer for the result of creating the user profile in Firestore
        viewModel.userCreated.observe(viewLifecycleOwner) { userCreated ->
            if (userCreated == null) return@observe // Event has been handled

            if (userCreated) {
                Toast.makeText(requireContext(), "Registration Successfully", Toast.LENGTH_SHORT).show()
                // After successful registration, always go to location screen.
                findNavController().navigate(R.id.action_otpFragment_to_locationFragment)
            } else {
                Toast.makeText(requireContext(), "Registration Failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
            // Reset the event
            viewModel.onAuthEventHandled()
        }
    }

    private fun handleNavigation() {
        if (viewModel.isLocationAvailable()) {
            findNavController().navigate(R.id.action_otpFragment_to_homeFragment, null, navOptions {
                popUpTo(R.id.loginFragment) { inclusive = true }
            })
        } else {
            findNavController().navigate(R.id.action_otpFragment_to_locationFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
        // Reset any leftover events to prevent leaks or unwanted navigation
        viewModel.onAuthEventHandled()
    }
}
