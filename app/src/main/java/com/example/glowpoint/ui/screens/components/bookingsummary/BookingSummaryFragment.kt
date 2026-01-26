package com.example.glowpoint.ui.screens.components.bookingsummary

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.glowpoint.R
import com.example.glowpoint.databinding.FragmentBookingSummaryBinding
import com.example.glowpoint.domain.model.failure.realtime.WriteReqDomainFailure
import com.example.glowpoint.ui.adapter.SelectedServicesAdapter
import com.example.glowpoint.ui.screens.components.ChildNavigationListener
import com.example.glowpoint.ui.sharedviewmodel.SharedESToBSViewModel
import com.example.glowpoint.util.PaymentMethod
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingSummaryFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentBookingSummaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var selectedServicesAdapter: SelectedServicesAdapter
    private val viewmodel: BookingSummaryViewModel by viewModels()
    private val sharedViewModel: SharedESToBSViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userDetails = sharedViewModel.userDetails.value
        val selectedServices = sharedViewModel.selectedServices.value
        val shopDetails = sharedViewModel.shopDetails.value
        val selectedTimeSlot = sharedViewModel.selectedTimeSlot.value
        if (userDetails != null && selectedServices.isNotEmpty() && shopDetails != null && selectedTimeSlot.isNotEmpty()) {
            viewmodel.setBookingDetails(
                user = userDetails,
                selectedServices = selectedServices,
                shopDetails = shopDetails,
                selectedTimeSlots = selectedTimeSlot
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListener()
        setupObserve()
    }

    private fun setupObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.uiState.collect {
                    when(it) {
                        is BookingSummaryUIState.Idle -> {
                            onSetLoading(false)
                        }
                        is BookingSummaryUIState.Failure -> {
                            when(val failure = it.failure) {
                                is WriteReqDomainFailure.Cancelled -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.NoInternet -> {
                                    showNoInternetDialog()
                                }
                                is WriteReqDomainFailure.NotFound -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                is WriteReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                is WriteReqDomainFailure.Unknown -> {
                                    Toast.makeText(requireContext(), failure.cause.message, Toast.LENGTH_LONG).show()
                                }
                                is WriteReqDomainFailure.ValidationError -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                            }
                            onSetLoading(false)
                        }
                        is BookingSummaryUIState.Success -> {
                            parentFragmentManager.setFragmentResult(
                                "booking_successful",
                                bundleOf("booking" to it.bookingSummary.bookingId)
                            )
                            dismiss()
                            onSetLoading(false)
                        }
                        is BookingSummaryUIState.Loading -> {
                            onSetLoading(true)
                        }
                        is BookingSummaryUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.userDetails.collect {
                    if (it != null) {
                        binding.customerName.text = it.name
                        binding.customerPhone.text = it.phoneNumber
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.shopDetails.collect {
                    if(it != null) {
                        binding.shopName.text = it.name
                        binding.shopAddress.text = it.address
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.selectedServices.collect {
                    if(it.isNotEmpty()) {
                        selectedServicesAdapter.submitList(it)

                        var totalAmount = 0.0
                        it.forEach { service ->
                            totalAmount += service.price
                        }
                        binding.totalAmount.text = "₹ $totalAmount"
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
                viewmodel.selectedTimeSlots.collect {
                    if(it.isNotEmpty()) {
                        it.forEach { time ->
                            val chip = Chip(requireContext())
                            chip.text = time.time
                            chip.isClickable = false
                            binding.timeSlotChipGroup.addView(chip)
                        }
                    }

            }
        }
    }

    private fun setupListener() {
        binding.confirmBooking.setOnClickListener {
            val paymentMethod = when (binding.paymentMethod.checkedRadioButtonId) {
                binding.payAfterService.id -> PaymentMethod.PAYMENT_AFTER_SERVICE
                binding.payOnline.id -> PaymentMethod.PAYMENT_BEFORE_SERVICE
                else -> PaymentMethod.PAYMENT_AFTER_SERVICE
            }

            viewmodel.bookService(paymentMethod)
        }
    }

    private fun setupRecyclerView() {
        selectedServicesAdapter = SelectedServicesAdapter()
        binding.rvSelectedServices.apply {
            adapter = selectedServicesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}