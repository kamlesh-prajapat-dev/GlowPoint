package com.example.glowpoint.ui.screens.bookings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.example.glowpoint.databinding.FragmentBookingsBinding
import com.example.glowpoint.domain.model.failure.realtime.GetReqDomainFailure
import com.example.glowpoint.ui.adapter.BookingHistoryAdapter
import com.example.glowpoint.ui.sharedviewmodel.SharedBBSViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

@AndroidEntryPoint
class BookingsFragment : Fragment() {

    private var _binding: FragmentBookingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookingsViewModel by viewModels()
    private val sharedViewModel: SharedBBSViewModel by activityViewModels()
    private lateinit var bookingHistoryAdapter: BookingHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupListener()
    }

    private fun setupListener() {

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
               applyFilter(tab.position)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                // optional
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                applyFilter(tab.position)
            }
        })
    }

    private fun applyFilter(position: Int) {
        val list = viewModel.bookings.value

        // 🔥 RESET UI STATE EVERY TIME
        binding.bookingsRecyclerView.isVisible = true
        binding.emptyStateText.isVisible = false

        if (list.isEmpty()) {
            bookingHistoryAdapter.submitList(emptyList())
            binding.bookingsRecyclerView.isVisible = false
            binding.emptyStateText.text = "No bookings found."
            binding.emptyStateText.isVisible = true
            return
        }

        when (position) {
            0 -> { // Current
                val currentBookings = list.filter { isSameDay(it.createdAt) }
                if (currentBookings.isNotEmpty()) {
                    bookingHistoryAdapter.submitList(currentBookings)
                } else {
                    showEmpty("Current Order is Empty.")
                }
            }

            1 -> { // Previous
                val previousBookings = list.filter { !isSameDay(it.createdAt) }
                if (previousBookings.isNotEmpty()) {
                    bookingHistoryAdapter.submitList(previousBookings)
                } else {
                    showEmpty("Previous Order is Empty.")
                }
            }
        }
    }

    private fun showEmpty(message: String) {
        bookingHistoryAdapter.submitList(emptyList())
        binding.bookingsRecyclerView.isVisible = false
        binding.emptyStateText.text = message
        binding.emptyStateText.isVisible = true
    }

    private fun isSameDay(time: Long): Boolean {
        val bookingDate = Instant.ofEpochMilli(time)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val today = LocalDate.now()
        return bookingDate == today
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when(it) {
                        is BookingsUIState.Idle -> {
                            onSetLoading(false)
                        }
                        is BookingsUIState.Failure -> {
                            when(val failure = it.failure) {
                                is GetReqDomainFailure.InvalidData -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                GetReqDomainFailure.Network -> {
                                    showNoInternetDialog()
                                }
                                is GetReqDomainFailure.NotFound -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                is GetReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                is GetReqDomainFailure.Unknown -> {
                                    Toast.makeText(requireContext(), failure.cause.message, Toast.LENGTH_LONG).show()
                                }
                            }
                            onSetLoading(false)
                        }
                        is BookingsUIState.Loading -> {
                            onSetLoading(true)
                        }
                        is BookingsUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }
                        is BookingsUIState.GetSuccess -> {
                            val bookings = it.bookings
                            if (bookings.isNotEmpty()) {
                                viewModel.onSetBookings(it.bookings)
                                binding.tabLayout.getTabAt(0)?.select()
                            } else {
                                binding.bookingsRecyclerView.isVisible = false
                                binding.emptyStateText.isVisible = true
                            }
                            onSetLoading(false)
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        bookingHistoryAdapter = BookingHistoryAdapter {
            sharedViewModel.onSetFetchedBooking(it.bookingId)
            val action = BookingsFragmentDirections.actionBookingsFragmentToBookingStatusFragment()
            findNavController().navigate(action)
        }
        binding.bookingsRecyclerView.apply {
            adapter = bookingHistoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
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
    }
}
