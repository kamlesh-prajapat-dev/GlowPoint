package com.example.glowpoint.ui.screens.auth.register

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentRegisterBinding
import com.example.glowpoint.ui.screens.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.viewModel = registerViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupOnClickListener()
        observeViewModel()
    }

    private fun setupAdapter() {
        // 1) create adapter from string-array
        val genders = resources.getStringArray(R.array.gender_options)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.genderAutoCompleteTextView.setAdapter(adapter)

        // 2) show dropdown when clicked (good UX)
        binding.genderAutoCompleteTextView.setOnClickListener {
            binding.genderAutoCompleteTextView.showDropDown()
        }

        // 3) also show dropdown on focus (optional)
        binding.genderAutoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.genderAutoCompleteTextView.showDropDown()
        }

        // 4) handle selection: update ViewModel or local state
        binding.genderAutoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as String
            registerViewModel.onSetGender(selected)
        }
    }

    private fun setupOnClickListener() {
        binding.registerButton.setOnClickListener {
            val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
            val userName = binding.nameEditText.text.toString().trim()
            registerViewModel.registerUser(requireActivity(), phoneNumber, userName)
        }

        binding.topAppBar.setNavigationOnClickListener {
            // Handle navigation icon press
            registerViewModel.reset()
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            registerViewModel.uiState.collect {
                when (it) {
                    RegisterUIState.Idle -> {
                        onSetLoading(false)
                    }

                    RegisterUIState.Loading -> {
                        onSetLoading(true)
                    }

                    RegisterUIState.Success -> {
                        handleNavigation(viewModel.isLocationSet.value)
                        onSetLoading(false)
                    }

                    RegisterUIState.NotInternet -> {
                        showNoInternetDialog()
                        onSetLoading(false)
                    }

                    is RegisterUIState.ValidationError -> {
                        Toast.makeText(
                            requireContext(),
                            "Authentication Failed: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        onSetLoading(false)
                    }

                    is RegisterUIState.Verification -> {
                        val verificationId = it.verificationId
                        val token = it.token
                        val user = it.user
                        viewModel.setRegisterData(
                            user = user,
                            isRegistered = true,
                            verificationId = verificationId,
                            token = token
                        )
                        findNavController().navigate(R.id.action_registerFragment_to_otpFragment)
                        onSetLoading(false)
                    }

                    is RegisterUIState.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Authentication Failed: ${it.exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        onSetLoading(false)
                    }

                    else -> {
                        onSetLoading(false)
                    }
                }
            }
        }

        // 5) observe viewModel.gender (optional) — keep UI in sync if changed elsewhere
        viewLifecycleOwner.lifecycleScope.launch {
            registerViewModel.gender.collect { value ->
                if (binding.genderAutoCompleteTextView.text.toString() != value) {
                    binding.genderAutoCompleteTextView.setText(
                        value,
                        false
                    ) // false -> don't filter again
                }
            }
        }
    }

    private fun handleNavigation(isLocationSet: Boolean) {
        if (isLocationSet) {
            findNavController().navigate(
                R.id.action_registerFragment_to_homeFragment,
                null,
                navOptions {
                    popUpTo(R.id.loginFragment) { inclusive = true }
                })
        } else {
            findNavController().navigate(R.id.action_registerFragment_to_locationFragment)
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.registerButton.isEnabled = !isLoading
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
        _binding = null

        registerViewModel.reset()
    }
}