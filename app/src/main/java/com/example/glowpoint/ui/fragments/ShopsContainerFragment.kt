package com.example.glowpoint.ui.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.databinding.FragmentShopsContainerBinding
import com.example.glowpoint.ui.adapter.RecyclerViewShopAdapter
import com.example.glowpoint.ui.viewmodel.ParentChildForShopsContainerViewModel
import com.example.glowpoint.ui.viewmodel.SharedForEachSalonViewModel
import com.example.glowpoint.ui.viewmodel.SharedForSearchShopsViewModel
import com.example.glowpoint.ui.viewmodel.ShopsContainerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopsContainerFragment : Fragment() {

    private lateinit var binding: FragmentShopsContainerBinding
    private lateinit var itemAdapter: RecyclerViewShopAdapter
    private val viewModel: ShopsContainerViewModel by viewModels()
    private val sharedForEachSalonViewModel: SharedForEachSalonViewModel by activityViewModels()
    private val sharedForParentViewModel: ParentChildForShopsContainerViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShopsContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupParentChildObservers()
    }

    private fun setupParentChildObservers() {
        sharedForParentViewModel.isParentHomeNavigate.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.loadNearBySalons()
                sharedForParentViewModel.reset()
            }
        }

        sharedForParentViewModel.isParentShopsNavigate.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.loadNearBySalons(observeListOfServiceId())
                sharedForParentViewModel.reset()
            }
        }
    }

    private fun observeListOfServiceId(): List<Map<String?, Boolean>>? {
       var listOfServiceId: List<Map<String?, Boolean>>? = emptyList()
        sharedForParentViewModel.listOfServiceId.observe(viewLifecycleOwner) {
            listOfServiceId = it
        }
        return listOfServiceId
    }

    private fun setupRecyclerView() {
        itemAdapter = RecyclerViewShopAdapter {
            // Handle shop click -> Navigate to EachShopFragment
            sharedForEachSalonViewModel.onSetSalon(it)
        }
        binding.shopsRecycler.adapter = itemAdapter
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.isVisible = isLoading
            if (isLoading) {
                binding.shopsRecycler.isVisible = false
                binding.emptyStateGroup.isVisible = false
                binding.errorStateGroup.isVisible = false
            }
        }

        viewModel.showEmptyState.observe(viewLifecycleOwner) { showEmpty ->
            binding.emptyStateGroup.isVisible = showEmpty && viewModel.isLoading.value == false
        }

        viewModel.errorState.observe(viewLifecycleOwner) { error ->
            val showError = error != null && viewModel.isLoading.value == false
            binding.errorStateGroup.isVisible = showError
            if (showError) {
                binding.errorStateText.text = error
            }
        }

        viewModel.nearBySalons.observe(viewLifecycleOwner) { salons ->
            itemAdapter.submitList(salons)
            // Also, update the visibility of the recycler based on the list
            binding.shopsRecycler.isVisible = salons.isNotEmpty()
        }
    }
}