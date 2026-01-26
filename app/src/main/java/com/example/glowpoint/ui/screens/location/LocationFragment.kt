package com.example.glowpoint.ui.screens.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.data.models.LocationSuggestion
import com.example.glowpoint.databinding.FragmentLocationBinding
import com.example.glowpoint.domain.model.failure.firestore.GetReqDomainFailure
import com.example.glowpoint.ui.adapter.LocationSuggestionAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationFragment : Fragment() {
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var suggestionAdapter: LocationSuggestionAdapter
    private val viewModel: LocationViewModel by viewModels()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getCurrentLocation()
        } else {
            Toast.makeText(
                requireContext(),
                "Location permission is required to continue.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//        binding.viewModel = viewModel
//        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupSearchView()

        binding.currentLocationButton.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun setupRecyclerView() {
        suggestionAdapter = LocationSuggestionAdapter { suggestion ->
            // User clicked a location
            viewModel.onLocationSelected(suggestion)
        }

        binding.rvLocationSuggestions.apply {
            adapter = suggestionAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus() // hides keyboard
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.length >= 3) {
                    viewModel.searchLocation(newText)
                }
                return true
            }
        })

        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // User clicked on SearchView
                binding.orDivider.isVisible = false
                binding.currentLocationButton.isVisible = false
            } else {
                val query = viewModel.searchQuery.value
                if (query.isNullOrEmpty()) {
                    // SearchView lost focus
                    binding.orDivider.isVisible = true
                    binding.currentLocationButton.isVisible = true
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is LocationUIState.Idle -> {
                            binding.orDivider.isVisible = true
                            binding.currentLocationButton.isVisible = true
                            binding.nestedServicesContainer.isVisible = false
                            binding.tvEmptyState.isAllCaps = false
                            onSetLoading(false)
                        }

                        is LocationUIState.Failure -> {
                            when (val failure = it.failure) {
                                GetReqDomainFailure.Cancelled -> Unit
                                GetReqDomainFailure.DataNotFound -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Data not found",
                                        Toast.LENGTH_SHORT).show()
                                }
                                GetReqDomainFailure.InvalidRequest -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Invalid Request",
                                        Toast.LENGTH_SHORT).show()
                                }
                                GetReqDomainFailure.NoInternet -> Unit // No Internet Dialog
                                is GetReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_SHORT).show()
                                }
                                is GetReqDomainFailure.Unknown -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.cause.message,
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                            onSetLoading(false)
                        }

                        is LocationUIState.Success -> {
                            if (it.isSuccess) navigateToHome()
                            onSetLoading(false)
                        }

                        is LocationUIState.Loading -> {
                            onSetLoading(true)
                        }

                        is LocationUIState.FetchedLocationSuccess -> {
                            viewModel.loadNearBySalons(it.suggestion)
                            onSetLoading(false)
                        }

                        is LocationUIState.FetchedLocationsSuccess -> {
                            viewModel.onChangeLocationSuggestions(it.suggestions)
                            onSetLoading(false)
                        }

                        is LocationUIState.GetNearBySalonSuccess -> {
                            val suggestion = it.suggestion
                            if (suggestion.name.isBlank() && suggestion.details.isBlank()) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    viewModel.saveLocation(
                                        suggestion.latitude,
                                        suggestion.longitude,
                                        requireContext()
                                    )
                                }
                            } else {
                                viewModel.saveLocation(it.suggestion)
                            }
                            onSetLoading(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.locationSuggestions.collect {
                    if (it != null && it.isNotEmpty()) {
                        binding.nestedServicesContainer.isVisible = true
                        binding.tvEmptyState.isVisible = false
                        binding.orDivider.isVisible = false
                        binding.currentLocationButton.isVisible = false
                        suggestionAdapter.submitList(it)
                    } else if(it != null){
                        binding.nestedServicesContainer.isVisible = false
                        binding.tvEmptyState.isVisible = true
                    }
                }
            }
        }
    }

    private fun requestLocationPermission() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    viewModel.loadNearBySalons(
                        LocationSuggestion(
                            name = "",
                            details = "",
                            longitude = longitude,
                            latitude = latitude
                        )
                    )

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Could not get location. Please ensure location is enabled.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to get location: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun navigateToHome() {
        val action = LocationFragmentDirections.actionLocationFragmentToHomeFragment()
        findNavController().navigate(action)
        viewModel.reset()
    }

    fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}