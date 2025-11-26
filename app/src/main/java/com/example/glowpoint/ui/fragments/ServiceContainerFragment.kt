package com.example.glowpoint.ui.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentServiceContainerBinding
import com.example.glowpoint.ui.adapter.RecyclerViewItemAdapter
import com.example.glowpoint.ui.viewmodel.ServiceContainerViewModel
import com.example.glowpoint.ui.viewmodel.SharedForSearchShopsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Added this annotation to fix the crash
class ServiceContainerFragment : Fragment() {

    private lateinit var itemAdapter: RecyclerViewItemAdapter
    private lateinit var binding: FragmentServiceContainerBinding

    private val sharedForSearchShopsViewModel: SharedForSearchShopsViewModel by activityViewModels()

    companion object {
        fun newInstance() = ServiceContainerFragment()
    }

    private val viewModel: ServiceContainerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServiceContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The logic to set the default gender is now correctly handled 
        // by the LiveData observer in setupObservers().
        // The redundant call to viewModel.loadCurrentUser() has been removed.

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        itemAdapter = RecyclerViewItemAdapter(viewModel::toggleServiceSelection)
        binding.categoryRecycler.adapter = itemAdapter
    }

    private fun setupClickListeners() {
        binding.genderToggleGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                updateServiceList()
            }
        }

        binding.searchShopsButton.setOnClickListener {
            sharedForSearchShopsViewModel.setListOfServiceId(viewModel.getSelectedServices())
        }
    }
	
    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.isVisible = isLoading
            if (isLoading) {
                binding.categoryRecycler.isVisible = false
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

        viewModel.menServices.observe(viewLifecycleOwner) { updateServiceList() }
        viewModel.womenServices.observe(viewLifecycleOwner) { updateServiceList() }

        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                when (user.gender) {
                    "Male" -> binding.genderToggleGroup.check(R.id.menChip)
                    "Female" -> binding.genderToggleGroup.check(R.id.womenChip)
                }
            } else {
                // Default to women if user is null for some reason
                binding.genderToggleGroup.check(R.id.womenChip)
            }
        }

        viewModel.isVisibleSearchForShopButton.observe(viewLifecycleOwner) {
            binding.searchShopsButton.isVisible = it
        }
    }

    private fun updateServiceList() {
        if (viewModel.isLoading.value == true) return

        viewModel.onSetLoading(true)
        val listToShow = when (binding.genderToggleGroup.checkedChipId) {
            binding.menChip.id -> viewModel.menServices.value
            binding.womenChip.id -> viewModel.womenServices.value
            else -> emptyList()
        }

        itemAdapter.submitList(listToShow)

        val showRecycler = !listToShow.isNullOrEmpty()
        binding.categoryRecycler.isVisible = showRecycler
        if (showRecycler) {
            binding.emptyStateGroup.isVisible = false
        }
        viewModel.onSetLoading(false)
    }
}
