package com.example.glowpoint.ui.screens.bookingStatus

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.semantics.text
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.glowpoint.R
import com.example.glowpoint.data.models.TimeSlot
import com.example.glowpoint.databinding.FragmentBookingStatusBinding
import com.example.glowpoint.ui.adapter.SelectedServicesAdapter
import com.example.glowpoint.ui.sharedviewmodel.SharedBBSViewModel
import com.example.glowpoint.util.BookingStatus
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.collections.forEach

@AndroidEntryPoint
class BookingStatusFragment : Fragment() {

    private var _binding: FragmentBookingStatusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookingStatusViewModel by viewModels()
    private val sharedViewModel: SharedBBSViewModel by activityViewModels()
    private lateinit var selectedServicesAdapter: SelectedServicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fetchedBooking = sharedViewModel.fetchedBooking.value
        if (fetchedBooking != null) {
            viewModel.observeFetchedBooking(fetchedBooking)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListener()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchedBooking.collect { 
                    if (it != null) {
                        binding.shopNameTextView.text = it.shopName
                        binding.shopAddressTextView.text = it.shopAddress
                        binding.distanceTextView.text = (it.distance.div(1000)).let { args-> "%.2f km away".format(args) }
                        var totalAmount = 0.0
                        it.selectedServices.forEach { service -> totalAmount += service.price }
                        binding.totalAmountTextView.text = "₹ $totalAmount"
                        binding.paymentStatusTextView.text = it.paymentDetails.paymentStatus

                        selectedServicesAdapter.submitList(it.selectedServices)

                        val bookingStatus = it.bookingStatus
                        updateStatusTracker(bookingStatus, it.previousStatus)

                        it.selectedTimeSlot.forEach { time ->
                            val chip = Chip(requireContext())
                            chip.text = time
                            chip.isClickable = false
                            binding.timeSlotChipGroup.addView(chip)
                        }

                        val is40MinuteBefore = isSlotBefore40Minutes(it.selectedTimeSlot[0])
                        binding.screenBtn.isVisible = (bookingStatus == BookingStatus.PENDING || bookingStatus == BookingStatus.CONFIRMED) && is40MinuteBefore
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { 
                    when(it) {
                        is BookingStatusUIState.Idle -> { onSetLoading(false) }
                        is BookingStatusUIState.Failure -> {
                            Toast.makeText(requireContext(), it.exception.message, Toast.LENGTH_LONG).show()
                            onSetLoading(false)
                        }
                        is BookingStatusUIState.Success -> {
                            viewModel.onSetFetchedBooking(it.fetchedBooking)
                            onSetLoading(false)
                        }
                        is BookingStatusUIState.Loading -> {
                            onSetLoading(true)
                        }
                        is BookingStatusUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }
                        is BookingStatusUIState.CancelSuccess -> {
                            Toast.makeText(requireContext(), "Booking Cancelled", Toast.LENGTH_LONG).show()
                            onSetLoading(false)
                        }
                    }
                }
            }
        }
    }

    private fun setupListener() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.screenBtn.setOnClickListener {
            val booking = viewModel.fetchedBooking.value
            if (booking != null) {
                showCancelConfirmation(previousStatus = booking.bookingStatus, bookingId = booking.bookingId, salonId = booking.shopId, selectedTimeSlot = booking.selectedTimeSlot)
            }
        }
    }

    private fun showCancelConfirmation(
        bookingId: String,
        previousStatus: String,
        salonId: String,
        selectedTimeSlot: List<String>
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Booking?")
            .setMessage("Are you sure you want to cancel this booking? This action cannot be undone.")
            .setCancelable(false)
            .setPositiveButton("Yes, Cancel") { _, _ ->
                viewModel.cancelBooking(bookingId = bookingId, previousStatus = previousStatus, salonId = salonId, selectedTimeSlot = selectedTimeSlot)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupRecyclerView() {
        selectedServicesAdapter = SelectedServicesAdapter()
        binding.servicesRecyclerView.apply {
            adapter = selectedServicesAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun resetStatusTracker() {
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.grey)
        binding.pendingIcon.setColorFilter(inactiveColor)
        binding.confirmedIcon.setColorFilter(inactiveColor)
        binding.inProgressIcon.setColorFilter(inactiveColor)
        binding.completedIcon.setColorFilter(inactiveColor)
        binding.pendingConnector.setBackgroundColor(inactiveColor)
        binding.confirmedConnector.setBackgroundColor(inactiveColor)
        binding.inProgressConnector.setBackgroundColor(inactiveColor)


        // Reset text and visibility
        binding.pendingTextView.text = "Pending"
        binding.confirmedTextView.text = "Confirmed"
        binding.pendingTextView.setTextColor(inactiveColor)
        binding.confirmedTextView.setTextColor(inactiveColor)
        binding.inProgressTextView.isVisible = true
        binding.completeTextView.isVisible = true
    }

    private fun updateStatusTracker(status: String, previousStatus: String) {
        resetStatusTracker()
        val activeColor = ContextCompat.getColor(requireContext(), R.color.green)
        val cancelledColor = ContextCompat.getColor(requireContext(), R.color.red)

        when (status) {
            BookingStatus.PENDING -> {
                binding.pendingIcon.setColorFilter(activeColor)
            }
            BookingStatus.CONFIRMED -> {
                binding.pendingIcon.setColorFilter(activeColor)
                binding.pendingConnector.setBackgroundColor(activeColor)
                binding.confirmedIcon.setColorFilter(activeColor)
            }
            BookingStatus.IN_PROGRESS -> {
                binding.pendingIcon.setColorFilter(activeColor)
                binding.pendingConnector.setBackgroundColor(activeColor)
                binding.confirmedIcon.setColorFilter(activeColor)
                binding.confirmedConnector.setBackgroundColor(activeColor)
                binding.inProgressIcon.setColorFilter(activeColor)
            }
            BookingStatus.COMPLETE -> {
                binding.pendingIcon.setColorFilter(activeColor)
                binding.pendingConnector.setBackgroundColor(activeColor)
                binding.confirmedIcon.setColorFilter(activeColor)
                binding.confirmedConnector.setBackgroundColor(activeColor)
                binding.inProgressIcon.setColorFilter(activeColor)
                binding.inProgressConnector.setBackgroundColor(activeColor)
                binding.completedIcon.setColorFilter(activeColor)
            }
            BookingStatus.CANCELLED -> {
                // This logic assumes your `BookingDetails` model has a `previousStatus` field.
                // You will need to add this field to your data model for this to work correctly.
                // Replace with booking.previousStatus

                if (previousStatus == BookingStatus.CONFIRMED) {
                    binding.pendingIcon.setColorFilter(cancelledColor)
                    binding.pendingConnector.setBackgroundColor(cancelledColor)
                    binding.pendingTextView.text = BookingStatus.PENDING
                    binding.pendingTextView.setTextColor(cancelledColor)

                    binding.confirmedIcon.setColorFilter(cancelledColor)
                    binding.confirmedTextView.text = BookingStatus.CONFIRMED
                    binding.confirmedTextView.setTextColor(cancelledColor)
                    binding.confirmedConnector.setBackgroundColor(cancelledColor)

                    binding.inProgressTextView.visibility = View.INVISIBLE
                    binding.completeTextView.isVisible = true
                    binding.inProgressIcon.setColorFilter(cancelledColor)

                    binding.completeTextView.text = BookingStatus.CANCELLED
                    binding.completedIcon.setColorFilter(cancelledColor)
                    binding.inProgressConnector.setBackgroundColor(cancelledColor)
                    binding.completeTextView.setTextColor(cancelledColor)

                } else { // Assuming previous status was PENDING
                    binding.pendingIcon.setColorFilter(cancelledColor)
                    binding.pendingTextView.text = BookingStatus.PENDING
                    binding.pendingTextView.setTextColor(cancelledColor)
                    binding.pendingConnector.setBackgroundColor(cancelledColor)

                    binding.confirmedTextView.visibility = View.INVISIBLE
                    binding.inProgressTextView.visibility = View.INVISIBLE
                    binding.completeTextView.isVisible = true
                    binding.completeTextView.text = BookingStatus.CANCELLED
                    binding.confirmedIcon.setColorFilter(cancelledColor)
                    binding.inProgressIcon.setColorFilter(cancelledColor)
                    binding.completedIcon.setColorFilter(cancelledColor)
                    binding.confirmedConnector.setBackgroundColor(cancelledColor)
                    binding.inProgressConnector.setBackgroundColor(cancelledColor)
                    binding.completeTextView.setTextColor(cancelledColor)
                }
            }
        }
    }

    private fun isSlotBefore40Minutes(slotTime: String): Boolean {
        val slotLocalTime = LocalTime.parse(slotTime) // "HH:mm"

        val slotDateTime = LocalDate.now()
            .atTime(slotLocalTime)
            .minusMinutes(40)

        val now = LocalDateTime.now()

        return now.isBefore(slotDateTime) || now.isEqual(slotDateTime)
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

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        sharedViewModel.reset()
    }
}
