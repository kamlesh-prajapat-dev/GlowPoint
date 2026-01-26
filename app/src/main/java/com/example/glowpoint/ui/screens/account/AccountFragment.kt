package com.example.glowpoint.ui.screens.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.data.models.User
import com.example.glowpoint.databinding.FragmentAccountBinding
import com.example.glowpoint.ui.screens.components.language.LanguageSelectionDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       setupListener()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect {
                    if (it != null) {
                        setUpUI(it)
                    } else {
                        navigateToLogin()
                    }
                }
            }
        }
    }

    private fun setUpUI(user: User) {
        binding.userNameTextView.text = user.name
        binding.userPhoneTextView.text = user.phoneNumber
        binding.userGenderTextView.text = user.gender
    }

    private fun setupListener() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.editProfileButton.setOnClickListener {
            // Navigate to the edit profile screen
        }

        binding.bookingHistoryButton.setOnClickListener {
            // Navigate to the booking history screen
        }

        binding.sampleDataButton.setOnClickListener {
            val action = AccountFragmentDirections.actionAccountFragmentToSampleDataFragment()
            findNavController().navigate(action)
        }

        binding.languageButton.setOnClickListener {
            // Handle language selection
            LanguageSelectionDialogFragment().show(
                childFragmentManager,
                "LanguageSelectionDialogFragment"
            )
        }

        binding.logoutButton.setOnClickListener {
            // Handle logout
            viewModel.logout()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val action = AccountFragmentDirections.actionAccountFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}