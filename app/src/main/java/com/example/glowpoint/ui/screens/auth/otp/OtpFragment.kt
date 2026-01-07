package com.example.glowpoint.ui.screens.auth.otp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentOtpBinding
import com.example.glowpoint.ui.screens.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
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
            val otp = binding.verificationCodeEditText.text.toString().trim()
            viewModel.verifyOtp(otp)
        }

        binding.resendCodeTextView.setOnClickListener {
            // Let the ViewModel handle the resend logic
            viewModel.resendVerificationCode(requireActivity())
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
                binding.resendCodeTextView.text = "Resend in 00:${String.format("%02d", seconds)}"
                binding.resendCodeTextView.setTypeface(null, Typeface.NORMAL)
                binding.resendCodeTextView.isClickable = false
            }

            override fun onFinish() {
                binding.resendCodeTextView.text = "Resend Code"
                binding.resendCodeTextView.setTypeface(null, Typeface.BOLD)
                binding.resendCodeTextView.isClickable = true
            }
        }.start()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect {
                when (it) {
                    is OtpUISate.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Authentication Failed: ${it.e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        onSetLoading(false)
                    }

                    OtpUISate.Idle -> {
                        onSetLoading(false)
                    }

                    OtpUISate.NoInternet -> {
                        onSetLoading(false)
                        showNoInternetDialog()
                    }

                    OtpUISate.Loading -> {
                        onSetLoading(true)
                    }

                    is OtpUISate.Success -> {
                        if (viewModel.isRegistered.value == true) {
                            // REGISTRATION FLOW: Auth is successful, now create the user profile.
                            viewModel.createProfile()
                        } else {
                            // LOGIN FLOW: Auth is successful, navigate to next screen.
                            Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT)
                                .show()
                            handleNavigation(viewModel.isLocationSet.value)
                        }
                        onSetLoading(false)
                    }

                    is OtpUISate.ValidationError -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        onSetLoading(false)
                    }

                    is OtpUISate.Verification -> {
                        onSetLoading(false)
                        val verificationId = it.verificationId
                        val token = it.token
                        viewModel.onSetVerificationIdAndToken(verificationId, token)
                        startTimer() // Restart the UI timer
                    }

                    is OtpUISate.CreateUserSuccess -> {
                        val user = it.user
                        if (user != null) {
                            Toast.makeText(
                                requireContext(),
                                "Registration Successful",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().navigate(R.id.action_otpFragment_to_locationFragment)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Registration Failed. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        onSetLoading(false)
                    }
                }
            }
        }
    }

    private fun handleNavigation(isLocationSet: Boolean) {
        if (isLocationSet) {
            findNavController().navigate(R.id.action_otpFragment_to_homeFragment, null, navOptions {
                popUpTo(R.id.loginFragment) { inclusive = true }
            })
        } else {
            findNavController().navigate(R.id.action_otpFragment_to_locationFragment)
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        if (isLoading) {
            binding.verifyButton.isEnabled = false
            binding.resendCodeTextView.isEnabled = false
        } else {
            binding.verifyButton.isEnabled = true
            binding.resendCodeTextView.isEnabled = true
        }
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.no_internet_connection)
            .setMessage(R.string.check_internet_connection)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
        // Reset any leftover events to prevent leaks or unwanted navigation
        viewModel.onAuthEventHandled()
    }
}