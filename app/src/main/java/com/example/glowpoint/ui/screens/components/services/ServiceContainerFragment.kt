package com.example.glowpoint.ui.screens.components.services

import android.app.AlertDialog
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
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentServiceContainerBinding
import com.example.glowpoint.data.models.ServiceItem
import com.example.glowpoint.data.models.User
import com.example.glowpoint.ui.adapter.RecyclerViewItemAdapter
import com.example.glowpoint.ui.screens.home.HomeFragmentDirections
import com.example.glowpoint.ui.sharedviewmodel.SharedForSearchShopsViewModel
import com.example.glowpoint.util.EmptyListException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ServiceContainerFragment : Fragment() {

    private var _binding: FragmentServiceContainerBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemAdapter: RecyclerViewItemAdapter
    private val sharedForSearchShopsViewModel: SharedForSearchShopsViewModel by activityViewModels()

    companion object {
        fun newInstance() = ServiceContainerFragment()
        const val TAG = "ServiceContainerFragment"
    }

    private val viewModel: ServiceContainerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        itemAdapter = RecyclerViewItemAdapter(viewModel::toggleServiceSelection)
        binding.categoryRecycler.adapter = itemAdapter
    }

    private fun setupClickListeners() {

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    val checkedId = binding.genderToggleGroup.checkedChipId
                    searchEvent(checkedId, query)
                }
                binding.searchView.clearFocus() // hides keyboard
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val checkedId = binding.genderToggleGroup.checkedChipId
                searchEvent(checkedId, newText)
                return true
            }
        })

        binding.genderToggleGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                updateServiceList()
            }
        }

        binding.searchShopsButton.setOnClickListener {
            sharedForSearchShopsViewModel.setListOfServiceId(viewModel.getSelectedServices())
            val action = HomeFragmentDirections.actionHomeFragmentToShopsFragment()
            findNavController().navigate(action)
        }
    }

    private fun searchEvent(checkedId: Int, newText: String?) {
        when (checkedId) {
            binding.menChip.id -> {
                val list = viewModel.menServices.value
                if (list.isNotEmpty()) {
                    val filteredList = list.filter { it.name.contains(newText ?: "", ignoreCase = true) || it.description.contains(newText ?: "", ignoreCase = true) }
                    updateItemAdapterList(filteredList)
                }
            }

            binding.womenChip.id -> {
                val list = viewModel.womenServices.value
                if (list.isNotEmpty()) {
                    val filteredList = list.filter { it.name.contains(newText ?: "", ignoreCase = true) || it.description.contains(newText ?: "", ignoreCase = true) }
                    updateItemAdapterList(filteredList)
                }
            }
        }
    }

    private fun genderToggle(user: User?) {
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

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { user ->
                   genderToggle(user)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        ServiceContainerUIState.Idle -> {
                            onSetLoading(false)
                        }

                        ServiceContainerUIState.Loading -> {
                            onSetLoading(true)
                        }

                        is ServiceContainerUIState.Failure -> {
                            when (val exception = it.exception) {
                                is EmptyListException -> {
                                    binding.emptyStateGroup.isVisible = true
                                    binding.emptyStateText.text = exception.message
                                }

                                else -> Toast.makeText(
                                    requireContext(),
                                    exception.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            onSetLoading(false)
                        }

                        ServiceContainerUIState.IsNetworkAvailable -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }

                        is ServiceContainerUIState.Success -> {
                            val genderCategory = it.genderCategory
                            val services = it.services
                            if (services.isNotEmpty()) {
                                if (genderCategory) {
                                    if (viewModel.menServices.value.isEmpty()) {
                                        if (viewModel.womenServices.value.isEmpty()) {
                                            viewModel.loadServices(false)
                                        }
                                        viewModel.updateLiveData(it.services, true)
                                    }
                                } else {
                                    if (viewModel.womenServices.value.isEmpty()) {
                                        if (viewModel.menServices.value.isEmpty()) {
                                            viewModel.loadServices(true)
                                        }
                                        viewModel.updateLiveData(it.services, false)
                                    }
                                }
                            } else {
                                binding.categoryRecycler.isVisible = false
                                binding.emptyStateGroup.isVisible = true
                            }
                            onSetLoading(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.menServices.collect { if (it.isNotEmpty() && binding.genderToggleGroup.checkedChipId == binding.menChip.id) updateServiceList(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.womenServices.collect { if (it.isNotEmpty() && binding.genderToggleGroup.checkedChipId == binding.womenChip.id) updateServiceList(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isVisibleSearchForShopButton.collect {
                    binding.searchShopsButton.isVisible = it
                }
            }
        }
    }

    private fun updateServiceList(items: List<ServiceItem>? = null) {
        if (items != null) {
            updateItemAdapterList(items)
            return
        }

        when (binding.genderToggleGroup.checkedChipId) {
            binding.menChip.id -> {
                val list = viewModel.menServices.value
                if (list.isNotEmpty()) {
                    updateItemAdapterList(list)
                } else {
                    viewModel.loadServices(true)
                }
            }

            binding.womenChip.id -> {
                val list = viewModel.womenServices.value
                if (list.isNotEmpty()) {
                    updateItemAdapterList(list)
                } else {
                    viewModel.loadServices(false)
                }
            }
        }
    }

    private fun updateItemAdapterList(list: List<ServiceItem>) {
        val showRecycler = list.isNotEmpty()
        binding.categoryRecycler.isVisible = showRecycler
        binding.emptyStateGroup.isVisible = !showRecycler
        itemAdapter.submitList(list)
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.loadingIndicator.isVisible = isLoading
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
