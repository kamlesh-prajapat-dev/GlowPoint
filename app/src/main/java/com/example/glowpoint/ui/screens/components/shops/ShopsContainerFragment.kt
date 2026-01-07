package com.example.glowpoint.ui.screens.components.shops

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.databinding.FragmentHomeBinding
import com.example.glowpoint.databinding.FragmentShopsContainerBinding
import com.example.glowpoint.ui.adapter.RecyclerViewShopAdapter
import com.example.glowpoint.ui.screens.components.ChildNavigationListener
import com.example.glowpoint.ui.screens.home.HomeFragment
import com.example.glowpoint.ui.screens.home.HomeFragmentDirections
import com.example.glowpoint.ui.screens.shop.ShopsFragment
import com.example.glowpoint.ui.screens.shop.ShopsFragmentDirections
import com.example.glowpoint.ui.sharedviewmodel.ParentChildForShopsContainerViewModel
import com.example.glowpoint.ui.sharedviewmodel.SharedForEachSalonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShopsContainerFragment : Fragment() {
    companion object {
        const val TAG = "ShopsContainerFragment"
    }

    private val navigationListener: ChildNavigationListener? get() = parentFragment as? ChildNavigationListener

    private var _binding: FragmentShopsContainerBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemAdapter: RecyclerViewShopAdapter
    private val viewModel: ShopsContainerViewModel by viewModels()
    private val sharedForEachSalonViewModel: SharedForEachSalonViewModel by activityViewModels()
    private val sharedForParentViewModel: ParentChildForShopsContainerViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupParentChildObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopsContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupListener()
    }

    private fun setupListener() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchEvent(query)
                }
                binding.searchView.clearFocus() // hides keyboard
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchEvent(newText)
                return true
            }
        })
    }

    private fun searchEvent(newText: String?) {
        val list = viewModel.nearBySalons.value
        if (list.isNotEmpty()) {
            val filteredList = list.filter { it.name.contains(newText ?: "", ignoreCase = true) || it.address.contains(newText ?: "", ignoreCase = true) }
            itemAdapter.submitList(filteredList)
        }
    }

    private fun setupParentChildObservers() {
        val selectedServices = sharedForParentViewModel.listOfServiceId.value
        if (selectedServices != null) {
            viewModel.loadNearBySalons(selectedServices)
        } else {
            viewModel.loadNearBySalons()
        }
    }

    private fun setupRecyclerView() {
        itemAdapter = RecyclerViewShopAdapter {
            // Handle shop click -> Navigate to EachShopFragment
            sharedForEachSalonViewModel.onSetInitialData(it)
            val selectedServices = sharedForEachSalonViewModel.listOfServiceId.value
            if (selectedServices != null) {
                navigationListener?.onNavigationRequest()
            } else {
                navigationListener?.onNavigationRequest()
            }
        }
        binding.shopsRecycler.adapter = itemAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is ShopContainerUIState.Idle -> {
                            onSetLoading(false)
                        }

                        is ShopContainerUIState.Failure -> {
                            Toast.makeText(
                                requireContext(),
                                it.exception.message,
                                Toast.LENGTH_LONG
                            ).show()
                            onSetLoading(false)
                        }

                        is ShopContainerUIState.Success -> {
                            val salons = it.salons
                            if (salons.isNotEmpty()) {
                                viewModel.onSetNearBySalons(it.salons)
                            }
                            onSetLoading(false)
                        }

                        is ShopContainerUIState.Loading -> {
                            onSetLoading(true)
                        }

                        is ShopContainerUIState.NoInternet -> {
                            onSetLoading(false)
                        }

                        is ShopContainerUIState.NotServiceable -> {
                            binding.shopsRecycler.isVisible = false
                            binding.emptyStateGroup.isVisible = true
                            onSetLoading(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.nearBySalons.collect {
                    itemAdapter.submitList(it)
                }
            }
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.loadingIndicator.isVisible = isLoading
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

        sharedForEachSalonViewModel.salonReset()
    }
}