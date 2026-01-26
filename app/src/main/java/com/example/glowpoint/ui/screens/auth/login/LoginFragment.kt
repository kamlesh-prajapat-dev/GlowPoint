package com.example.glowpoint.ui.screens.auth.login

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentLoginBinding
import com.example.glowpoint.ui.screens.components.language.LanguageSelectionDialogFragment
import com.example.glowpoint.ui.screens.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loginViewModel.setLanguage()
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.viewModel = loginViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        setupOnClickListeners()
        observeViewModel()
    }

    private fun setupUi() {
        binding.phoneNumberEditText.addTextChangedListener {
            binding.phoneNumberLayout.error = null
        }
    }

    private fun setupOnClickListeners() {
        binding.sendCodeButton.setOnClickListener {
            val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
            loginViewModel.loginUser(requireActivity(), phoneNumber)
        }

        binding.registerButton.setOnClickListener {
            // Navigate to Register Fragment
            loginViewModel.reset()
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeViewModel() {
        // Observe Login View model state
        viewLifecycleOwner.lifecycleScope.launch {
            loginViewModel.language.collect {
                if (it == null) {
                    LanguageSelectionDialogFragment().show(
                        childFragmentManager,
                        "LanguageSelectionDialogFragment"
                    )
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            loginViewModel.uiState.collect {
                when (it) {
                    LoginUIState.Idle -> {
                        onSetLoading(false)
                    }

                    LoginUIState.Loading -> {
                        onSetLoading(true)
                    }

                    is LoginUIState.Success -> {
                        handleNavigation()
                        onSetLoading(false)
                    }

                    is LoginUIState.ValidationError -> {
                        binding.phoneNumberLayout.error = it.message
                        onSetLoading(false)
                    }

                    is LoginUIState.Verification -> {
                        val verificationId = it.verificationId
                        val token = it.token
                        val phoneNumber = it.phoneNumber
                        viewModel.setLoginData(phoneNumber = phoneNumber, verificationId = verificationId, token = token, isRegistered = false)
                        findNavController().navigate(R.id.action_loginFragment_to_otpFragment)
                        onSetLoading(false)
                    }

                    is LoginUIState.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Authentication Failed: ${it.e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        onSetLoading(false)
                    }

                    LoginUIState.NotInternet -> {
                        onSetLoading(false)
                        showNoInternetDialog()
                    }

                    is LoginUIState.UserGetSuccess -> {
                        onSetLoading(false)
                    }
                }
            }
        }
    }

    private fun handleNavigation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLocationSet.collect {
                if (it) {
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment, null, navOptions {
                        popUpTo(R.id.loginFragment) { inclusive = true }
                    })
                } else {
                    findNavController().navigate(R.id.action_loginFragment_to_locationFragment)
                }
            }
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

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        if (isLoading) {
            binding.sendCodeButton.isEnabled = false
            binding.registerButton.isEnabled = false
        } else {
            binding.sendCodeButton.isEnabled = true
            binding.registerButton.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        loginViewModel.reset()
    }
}