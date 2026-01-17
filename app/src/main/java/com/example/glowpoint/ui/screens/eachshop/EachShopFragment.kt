package com.example.glowpoint.ui.screens.eachshop

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.glowpoint.R
import com.example.glowpoint.data.models.ShopDetails
import com.example.glowpoint.databinding.FragmentEachShopBinding
import com.example.glowpoint.ui.adapter.RecyclerViewItemAdapter
import com.example.glowpoint.ui.adapter.RecyclerViewTimeSlotAdapter
import com.example.glowpoint.ui.screens.components.ChildNavigationListener
import com.example.glowpoint.ui.screens.components.bookingsummary.BookingSummaryFragment
import com.example.glowpoint.ui.sharedviewmodel.SharedBBSViewModel
import com.example.glowpoint.ui.sharedviewmodel.SharedESToBSViewModel
import com.example.glowpoint.ui.sharedviewmodel.SharedForEachSalonViewModel
import com.google.firebase.database.ChildEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@AndroidEntryPoint
class EachShopFragment : Fragment() {
    private var _binding: FragmentEachShopBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EachShopViewModel by viewModels()
    private val sharedViewModel: SharedForEachSalonViewModel by activityViewModels()
    private val sharedESToBSViewModel: SharedESToBSViewModel by activityViewModels()
    private val sharedBBSViewModel: SharedBBSViewModel by activityViewModels()
    private lateinit var itemAdapter: RecyclerViewItemAdapter
    private lateinit var timeSlotAdapter: RecyclerViewTimeSlotAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observeSharedViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEachShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupObservers()
        setupClickListeners()
    }

    private fun observeSharedViewModel() {
        val salonWithSelection = sharedViewModel.salonWithSelection.value
        if (salonWithSelection != null) {
            viewModel.loadSalonData(salonWithSelection.salon, salonWithSelection.selectedIds)
        }
    }

    private fun setupRecyclerViews() {
        itemAdapter = RecyclerViewItemAdapter(viewModel::toggleServiceSelection)
        binding.servicesRecycler.adapter = itemAdapter

        timeSlotAdapter = RecyclerViewTimeSlotAdapter(viewModel::toggleTimeSlotSelection)
        binding.timeSlotsRecycler.adapter = timeSlotAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { 
                    when (it) {
                        EachShopUIState.Idle -> {
                            onSetLoading(false)
                        }

                        EachShopUIState.Loading -> {
                            onSetLoading(true)
                        }

                        is EachShopUIState.Failure -> {
                            Toast.makeText(requireContext(), it.exception.message, Toast.LENGTH_LONG).show()
                            onSetLoading(false)
                        }

                        is EachShopUIState.Success -> {
                            viewModel.onSetTimeSlot(it.timeSlots)
                            onSetLoading(false)
                        }

                        is EachShopUIState.Error -> {
                            onSetLoading(false)
                        }

                        is EachShopUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.salon.collect {
                    if (it != null)
                    setupShopDetails(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.salonServices.collect { 
                    itemAdapter.submitList(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredSlots.collect {
                    if (it.isNotEmpty()) {
                        timeSlotAdapter.submitList(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isVisibleBookSlotsButton.collect { 
                    binding.bookSlotsButton.isVisible = it
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isTimeSlotRecyclerViewVisible.collect { 
                    binding.timeSlotsRecycler.isVisible = it
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.bookSlotsButton.setOnClickListener {
            val selectedTimeSlots = viewModel.getSelectedTimeSlots()
            val selectedServices = viewModel.getSelectedSalonServices()
            if(selectedServices.size == selectedTimeSlots.size) {
                sharedESToBSViewModel.setBookingDetails(selectedServices, viewModel.salon.value, selectedTimeSlots, viewModel.user.value)

                val bottomSheet = BookingSummaryFragment()
                bottomSheet.show(parentFragmentManager, "BookingSummaryFragment")
            } else {
                Toast.makeText(requireContext(), "Please make sure selected time slots and services are same.", Toast.LENGTH_SHORT).show()
            }
        }

        parentFragmentManager.setFragmentResultListener(
            "booking_successful",
            viewLifecycleOwner
        ) { _, result ->
            val booking = result.getString("booking")
            if (booking != null) {
                Toast.makeText(requireContext(), "Booking Successful", Toast.LENGTH_LONG).show()
                sharedBBSViewModel.onSetFetchedBooking(booking)
                sharedViewModel.reset()
                val action = EachShopFragmentDirections.actionEachShopFragmentToBookingStatusFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun setupShopDetails(salon: ShopDetails) {
        binding.shopName.text = salon.name
        binding.shopAddress.text = salon.address
        binding.shopRatting.text = salon.rating.toString()
        binding.shopDistance.text = 
            (salon.distance.div(1000)).let { "%.2f km away".format(it) }

        val isOpen = isShopOpenNow(salon.openTime, salon.closeTime)
        val context = binding.root.context
        if (isOpen) {
            binding.shopStatus.text = "Open Now"
            binding.shopStatus.setTextColor(ContextCompat.getColor(context, R.color.green))
        } else {
            binding.shopStatus.text = "Closed Now"
            binding.shopStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
        }
    }

    private fun isShopOpenNow(openTimeStr: String?, closeTimeStr: String?): Boolean {
        if (openTimeStr.isNullOrBlank() || closeTimeStr.isNullOrBlank()) return false
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            val openTime = LocalTime.parse(openTimeStr, formatter)
            val closeTime = LocalTime.parse(closeTimeStr, formatter)
            val currentTime = LocalTime.now()
            !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime)
        } catch (e: DateTimeParseException) {
            Log.e("EachShopFragment", "Invalid time format", e)
            false
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
        binding.loadingIndicator.isVisible = isLoading
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
