package com.example.glowpoint.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentRegisterBinding
import com.example.glowpoint.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerButton.setOnClickListener {
            if (viewModel.isInternetAvailable()) {
                val phoneNumber = viewModel.phoneNumber.value?.trim()
                if (!phoneNumber.isNullOrEmpty() && phoneNumber.length == 10) {
                    viewModel.registerUser(requireActivity())
                } else {
                    Toast.makeText(requireContext(), "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                }
            } else {
                showNoInternetDialog()
            }
        }

        // 1) create adapter from string-array
        val genders = resources.getStringArray(R.array.gender_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.genderAutoCompleteTextView.setAdapter(adapter)

        // 2) show dropdown when clicked (good UX)
        binding.genderAutoCompleteTextView.setOnClickListener {
            binding.genderAutoCompleteTextView.showDropDown()
        }

        // 3) also show dropdown on focus (optional)
        binding.genderAutoCompleteTextView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) binding.genderAutoCompleteTextView.showDropDown()
        }

        // 4) handle selection: update ViewModel or local state
        binding.genderAutoCompleteTextView.setOnItemClickListener { parent, view1, position, id ->
            val selected = parent.getItemAtPosition(position) as String
            viewModel.gender.value = selected // if using MutableLiveData
            // OR do whatever you need with selection
        }

        binding.topAppBar.setNavigationOnClickListener {
            viewModel.reset()
            findNavController().navigateUp()
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.verificationId.observe(viewLifecycleOwner) { verificationId ->
            verificationId?.let {
                findNavController().navigate(R.id.action_registerFragment_to_otpFragment)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            if (it != null) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        // 5) observe viewModel.gender (optional) — keep UI in sync if changed elsewhere
        viewModel.gender.observe(viewLifecycleOwner) { value ->
            if (binding.genderAutoCompleteTextView.text.toString() != value) {
                binding.genderAutoCompleteTextView.setText(value, false) // false -> don't filter again
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}
