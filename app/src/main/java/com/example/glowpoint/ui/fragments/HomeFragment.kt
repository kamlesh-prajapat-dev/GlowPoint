package com.example.glowpoint.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentHomeBinding
import com.example.glowpoint.ui.viewmodel.HomeViewModel
import com.example.glowpoint.ui.viewmodel.ParentChildForShopsContainerViewModel
import com.example.glowpoint.ui.viewmodel.SharedForEachSalonViewModel
import com.example.glowpoint.ui.viewmodel.SharedForSearchShopsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val sharedForSearchShopsViewModel: SharedForSearchShopsViewModel by activityViewModels()
    private val sharedForEachSalonViewModel: SharedForEachSalonViewModel by activityViewModels()
    private val parentChildForShopsContainerViewModel: ParentChildForShopsContainerViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomNavigation()

        // Set the default fragment
            binding.bottomNavigation.selectedItemId = R.id.action_services

        setupObservers()
        setUpSharedViewModelObservers()
    }

    override fun onResume() {
        super.onResume()
        // Explicitly tell the ViewModel to load data every time the fragment is resumed.
        viewModel.loadLocationData()
    }

    private fun setupObservers() {
        viewModel.location.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.locationTitle.text = it
            } else {
                binding.locationTitle.text = "Location"
            }
        }

        viewModel.areaOfUser.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.locationSubtitle.text = it
            } else {
                binding.locationSubtitle.text = "Area of User"
            }
        }
    }

    private fun setUpSharedViewModelObservers() {
        sharedForEachSalonViewModel.isNavigate.observe(viewLifecycleOwner) {
            if (it) {
                val action = HomeFragmentDirections.actionShopsContainerFragmentToEachShopFragment()
                findNavController().navigate(action)
            }
        }

        sharedForSearchShopsViewModel.isNavigate.observe(viewLifecycleOwner) {
            if (it) {
                val action = HomeFragmentDirections.actionHomeFragmentToShopsFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_shops -> {
                    parentChildForShopsContainerViewModel.setParentHomeNavigate()
                    replaceFragment(ShopsContainerFragment())
                    true
                }
                R.id.action_account -> {
                    // To be implemented
                    true
                }
                R.id.action_services -> {
                    replaceFragment(ServiceContainerFragment.newInstance())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager
            .beginTransaction()
            .replace(binding.childFragmentContainer.id, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}