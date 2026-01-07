package com.example.glowpoint.ui.screens.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.databinding.FragmentShopsBinding
import com.example.glowpoint.ui.screens.components.ChildNavigationListener
import com.example.glowpoint.ui.screens.components.shops.ShopsContainerFragment
import com.example.glowpoint.ui.sharedviewmodel.ParentChildForShopsContainerViewModel
import com.example.glowpoint.ui.sharedviewmodel.SharedForEachSalonViewModel
import com.example.glowpoint.ui.sharedviewmodel.SharedForSearchShopsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShopsFragment : Fragment(), ChildNavigationListener {

    private lateinit var binding: FragmentShopsBinding

    private val viewModel: ShopsViewModel by viewModels()
    private val sharedForSearchShopsViewModel: SharedForSearchShopsViewModel by activityViewModels()
    private val parentChildForShopsContainerViewModel: ParentChildForShopsContainerViewModel by activityViewModels()
    private val sharedForEachSalonViewModel: SharedForEachSalonViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupObserves()
        replaceFragment(ShopsContainerFragment())
    }

    private fun setupObserves() {
        viewLifecycleOwner.lifecycleScope.launch {
            sharedForSearchShopsViewModel.listOfServiceId.collect {
                sharedForEachSalonViewModel.onSetInitialData(selectedServices = it)
                parentChildForShopsContainerViewModel.setListOfServiceId(it)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager
            .beginTransaction()
            .replace(binding.childFragmentContainer.id, fragment)
            .commit()
    }

    override fun onNavigationRequest() {
        val action = ShopsFragmentDirections.actionShopsFragmentToEachShopFragment()
        findNavController().navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()

        parentChildForShopsContainerViewModel.reset()
        sharedForSearchShopsViewModel.reset()
    }
}