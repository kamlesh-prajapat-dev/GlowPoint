package com.example.glowpoint.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentLoginBinding
import com.example.glowpoint.ui.viewmodel.AuthViewModel
import com.example.glowpoint.util.LocaleHelper
import com.example.glowpoint.util.NetworkUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (LocaleHelper.getLanguage(requireContext()) == null) {
            LanguageSelectionDialogFragment().show(childFragmentManager, "LanguageSelectionDialogFragment")
        }

        binding.sendCodeButton.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(requireContext())) {
                val phoneNumber = viewModel.phoneNumber.value?.trim()
                if (!phoneNumber.isNullOrEmpty() && phoneNumber.length == 10) {
                    viewModel.sendVerificationCode(requireActivity())
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                }
            } else {
                showNoInternetDialog()
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.verificationId.observe(viewLifecycleOwner) { verificationId ->
            verificationId?.let {
                findNavController().navigate(R.id.action_loginFragment_to_otpFragment)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
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
        _binding = null
    }
}