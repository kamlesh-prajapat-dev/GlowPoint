package com.example.glowpoint.ui.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentHomeBinding
import com.example.glowpoint.ui.screens.components.ChildNavigationListener
import com.example.glowpoint.ui.screens.components.services.ServiceContainerFragment
import com.example.glowpoint.ui.screens.components.shops.ShopsContainerFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), ChildNavigationListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

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
        setupObservers()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_bookings -> {
                    viewModel.onSetUIState(HomeUIState.BookingsState)
                    true
                }
                else -> false
            }
        }
    }
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.location.collect {
                if (it.isNotEmpty()) {
                    binding.locationTitle.text = it
                } else {
                    binding.locationTitle.text = "Location"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.areaOfUser.collect {
                if (it.isNotEmpty()) {
                    binding.locationSubtitle.text = it
                } else {
                    binding.locationSubtitle.text = "Area of User"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect {
                setBottomNavChecked(it)

                when (it) {
                    HomeUIState.AccountState -> {

                    }

                    HomeUIState.BookingsState -> {
                        val action = HomeFragmentDirections.actionHomeFragmentToBookingsFragment()
                        findNavController().navigate(action)
                        viewModel.onSetUIState(HomeUIState.Idle)
                    }

                    HomeUIState.Idle -> {

                    }

                    HomeUIState.LocationState -> {

                    }

                    HomeUIState.ServiceState -> {
                        replaceFragmentSafely(ServiceContainerFragment.newInstance(), ServiceContainerFragment.TAG)
                    }

                    HomeUIState.ShopState -> {
                        replaceFragmentSafely(ShopsContainerFragment(), ShopsContainerFragment.TAG)
                    }
                }
            }
        }
    }

    private fun setBottomNavChecked(state: HomeUIState) {
        val menuId = when (state) {
            HomeUIState.ServiceState -> R.id.action_services
            HomeUIState.ShopState -> R.id.action_shops
            HomeUIState.AccountState -> R.id.action_account
            else -> return
        }

        binding.bottomNavigation.menu.findItem(menuId)?.isChecked = true
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_shops -> {
                    viewModel.onSetUIState(HomeUIState.ShopState)
                    true
                }

                R.id.action_account -> {
                    // To be implemented
                    viewModel.onSetUIState(HomeUIState.AccountState)
                    true
                }

                R.id.action_services -> {
                    viewModel.onSetUIState(HomeUIState.ServiceState)
                    true
                }

                else -> false
            }
        }
    }

    private fun replaceFragmentSafely(fragment: Fragment, tag: String) {
        val existingFragment = childFragmentManager.findFragmentByTag(tag)
        if (existingFragment == null) {
            childFragmentManager.beginTransaction()
                .replace(binding.childFragmentContainer.id, fragment, tag)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onNavigationRequest() {
        val action = HomeFragmentDirections.actionShopsContainerFragmentToEachShopFragment()
        findNavController().navigate(action)
    }
}