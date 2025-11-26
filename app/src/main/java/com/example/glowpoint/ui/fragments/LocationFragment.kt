package com.example.glowpoint.ui.fragments

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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentLocationBinding
import com.example.glowpoint.ui.viewmodel.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationFragment : Fragment() {

    private lateinit var binding: FragmentLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: LocationViewModel by activityViewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getCurrentLocation()
        } else {
            viewModel.onChange(false) // Hide loader if permission is denied
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
        binding = FragmentLocationBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.currentLocationButton.setOnClickListener {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        viewModel.onChange(true) // Start loading
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
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    viewModel.storeLatitude(latitude)
                    viewModel.storeLongitude(longitude)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        viewModel.setLocationName(requireContext(), latitude, longitude)
                    }
                    viewModel.onChange(false) // Stop loading on success
                    navigateToHome()
                } else {
                    Toast.makeText(requireContext(), "Could not get location. Please ensure location is enabled.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                viewModel.onChange(false) // Stop loading on failure
                Toast.makeText(requireContext(), "Failed to get location: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_locationFragment_to_homeFragment)
    }
}
